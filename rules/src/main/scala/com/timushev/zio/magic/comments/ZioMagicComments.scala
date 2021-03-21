package com.timushev.zio.magic.comments

import _root_.scalafix.v1._
import zio.magic.macros.graph.{Eq, Graph, Node}
import zio.magic.macros.utils.RenderGraph

import scala.annotation.tailrec
import scala.meta._

class ZioMagicComments extends SemanticRule("ZioMagicComments") {

  type GraphNode = Node[Symbol, RenderGraph]

  private val provideMagicLayer: SymbolMatcher =
    SymbolMatcher.exact("zio/magic/package.ZioProvideMagicOps#provideMagicLayer().") +
      SymbolMatcher.exact("zio/magic/package.ZSpecProvideMagicOps#provideMagicLayer().") +
      SymbolMatcher.exact("zio/magic/package.ZSpecProvideMagicOps#provideMagicLayerShared().")

  private val provideSomeMagicLayer: SymbolMatcher =
    SymbolMatcher.exact("zio/magic/package.ZioProvideMagicOps#provideSomeMagicLayer().") +
      SymbolMatcher.exact("zio/magic/package.ZSpecProvideMagicOps#provideSomeMagicLayer().") +
      SymbolMatcher.exact("zio/magic/package.ZSpecProvideMagicOps#provideSomeMagicLayerShared().")

  private val provideCustomMagicLayer: SymbolMatcher =
    SymbolMatcher.exact("zio/magic/package.ZioProvideMagicOps#provideCustomMagicLayer().") +
      SymbolMatcher.exact("zio/magic/package.ZSpecProvideMagicOps#provideCustomMagicLayer().") +
      SymbolMatcher.exact("zio/magic/package.ZSpecProvideMagicOps#provideCustomMagicLayerShared().")

  private val fromMagic: SymbolMatcher =
    SymbolMatcher.exact("zio/magic/package.ZLayerCompanionOps#fromMagic().")

  private val fromSomeMagic: SymbolMatcher =
    SymbolMatcher.exact("zio/magic/package.ZLayerCompanionOps#fromSomeMagic().")

  private val zlayer: SymbolMatcher    = SymbolMatcher.exact("zio/ZLayer#")
  private val rlayer: SymbolMatcher    = SymbolMatcher.exact("zio/package.RLayer#")
  private val urlayer: SymbolMatcher   = SymbolMatcher.exact("zio/package.URLayer#")
  private val layer: SymbolMatcher     = SymbolMatcher.exact("zio/package.Layer#")
  private val ulayer: SymbolMatcher    = SymbolMatcher.exact("zio/package.ULayer#")
  private val tasklayer: SymbolMatcher = SymbolMatcher.exact("zio/package.TaskLayer#")

  private val zio: SymbolMatcher  = SymbolMatcher.exact("zio/ZIO#")
  private val rio: SymbolMatcher  = SymbolMatcher.exact("zio/package.RIO#")
  private val urio: SymbolMatcher = SymbolMatcher.exact("zio/package.URIO#")

  private val zspec: SymbolMatcher = SymbolMatcher.exact("zio/test/package.ZSpec#")
  private val spec: SymbolMatcher  = SymbolMatcher.exact("zio/test/Spec#")

  private val nothing: SymbolMatcher = SymbolMatcher.exact("scala/Nothing#")

  private val has: SymbolMatcher = SymbolMatcher.exact("zio/Has#")

  @tailrec
  private def findDeclarationRoot(t: Tree): Option[Tree] = {
    t.parent match {
      case Some(_: Template) => Some(t)
      case Some(other)       => findDeclarationRoot(other)
      case None              => None
    }
  }

  private def treeComment(t: Tree): Option[Tokens] = {
    for {
      parent <- t.parent
      comment = parent.tokens
        .takeWhile(_.pos.start < t.pos.start)
        .takeRightWhile {
          case _: Token.Space   => true
          case _: Token.CR      => true
          case _: Token.LF      => true
          case _: Token.Tab     => true
          case _: Token.Comment => true
          case _                => false
        }
        .dropWhile {
          case Token.Comment(_) => false
          case _                => true
        }
    } yield comment
  }

  private def treeNewLineIndentation(t: Tree): Option[String] = {
    for {
      parent <- t.parent
      precedingTokens = parent.tokens.takeWhile(_.pos.start < t.pos.start)
      indentationTokens = precedingTokens
        .takeRightWhile {
          case _: Token.Space => true
          case _: Token.Tab   => true
          case _              => false
        }
      newLine = precedingTokens
        .dropRight(indentationTokens.length)
        .takeRight(2) match {
        case Seq(_: Token.CR, _: Token.LF) => "\r\n"
        case Seq(_, _: Token.CR)           => "\r"
        case Seq(_, _: Token.LF)           => "\n"
        case _                             => "\n"
      }
    } yield newLine + indentationTokens.syntax
  }

  implicit private object SymbolEq extends Eq[Symbol] {
    override def eq(a1: Symbol, a2: Symbol): Boolean = a1.equals(a2)
  }

  private def typeMembers(
      tpe: SemanticType
  )(implicit doc: SemanticDocument): List[Symbol] = {
    tpe match {
      case StructuralType(tpe2, _)   => typeMembers(tpe2)
      case TypeRef(_, nothing(_), _) => Nil
      case TypeRef(_, symbol, _)     => List(symbol)
      case WithType(types)           => types.flatMap(typeMembers)
      case _                         => Nil
    }
  }

  private def termSymbolType(symbol: Symbol)(implicit doc: SemanticDocument): Option[SemanticType] =
    symbol.normalized.info.map(_.signature).orElse(symbol.info.map(_.signature)).collect {
      case ValueSignature(tpe)        => tpe
      case MethodSignature(_, _, tpe) => tpe
    }

  private def termType(term: Term)(implicit doc: SemanticDocument): Option[SemanticType] =
    termSymbolType(term.symbol)

  private def layerTermToNode(
      term: Term
  )(implicit doc: SemanticDocument): Option[GraphNode] = {
    termType(term)
      .flatMap {
        case TypeRef(_, zlayer(_), List(in, _, out))           => Some(Some(in) -> out)
        case TypeRef(_, rlayer(_) | urlayer(_), List(in, out)) => Some(Some(in) -> out)
        case TypeRef(_, layer(_), List(_, out))                => Some(None -> out)
        case TypeRef(_, ulayer(_) | tasklayer(_), List(out))   => Some(None -> out)
        case _                                                 => None
      }
      .map { case (in, out) =>
        Node(in.fold(List.empty[Symbol])(typeMembers), typeMembers(out), RenderGraph(term.syntax))
      }
  }

  private def semanticTypeToInputs(
      tpe: SemanticType
  )(implicit doc: SemanticDocument): Option[List[Symbol]] = {
    tpe match {
      case TypeRef(_, zio(_), List(in, _, _))        => Some(in)
      case TypeRef(_, rio(_) | urio(_), List(in, _)) => Some(in)
      case TypeRef(_, zspec(_), List(in, _))         => Some(in)
      case TypeRef(_, spec(_), List(in, _, _))       => Some(in)
      case _                                         => None
    }
  }.map(typeMembers)

  private def zioTermToInputs(
      term: Term
  )(implicit doc: SemanticDocument): Option[List[Symbol]] =
    termType(term).flatMap(semanticTypeToInputs)

  private def typeToSymbols(tpe: Type)(implicit doc: SemanticDocument): Option[List[Symbol]] =
    tpe match {
      case Type.Name(_) | Type.Select(_, _) =>
        tpe.symbol.info.flatMap { info =>
          info.signature match {
            case TypeSignature(_, TypeRef(_, has(_), _), _) => Some(List(tpe.symbol))
            case TypeSignature(_, lowerBound, _)            => Some(typeMembers(lowerBound))
            case _                                          => None
          }
        }
      case Type.With(ltpe, rtpe) =>
        sequence(List(typeToSymbols(ltpe), typeToSymbols(rtpe))).map(_.flatten)
      case _ =>
        None
    }

  private def typeToNodes(tpe: Type)(implicit
      doc: SemanticDocument
  ): Option[List[GraphNode]] =
    tpe match {
      case Type.Name(_) | Type.Select(_, _) =>
        tpe.symbol.info.flatMap { info =>
          info.signature match {
            case TypeSignature(_, TypeRef(_, has(_), _), _) =>
              Some(
                List(Node(Nil, List(tpe.symbol), RenderGraph(s"ZLayer.requires[${tpe.syntax}]")))
              )
            case TypeSignature(_, lowerBound, _) =>
              Some(
                List(
                  Node(Nil, typeMembers(lowerBound), RenderGraph(s"ZLayer.requires[${tpe.syntax}]"))
                )
              )
            case _ => None
          }
        }
      case Type.With(ltpe, rtpe) =>
        sequence(List(typeToNodes(ltpe), typeToNodes(rtpe))).map(_.flatten)
      case _ =>
        None
    }

  private def zenvNode(implicit doc: SemanticDocument): Option[GraphNode] =
    Symbol("zio/PlatformSpecific#ZEnv#").info.flatMap { info =>
      info.signature match {
        case TypeSignature(_, lowerBound, _) =>
          Some(Node(Nil, typeMembers(lowerBound), RenderGraph(s"ZLayer.requires[ZEnv]")))
        case _ => None
      }
    }

  private def sequence[A](listOptions: List[Option[A]]): Option[List[A]] = {
    listOptions match {
      case Nil             => Some(Nil)
      case Some(a) :: tail => sequence(tail).map(a :: _)
      case None :: _       => None
    }
  }

  private def renderGraphLines(outputs: List[Symbol], inputs: List[GraphNode], layers: List[Term])(
      implicit doc: SemanticDocument
  ): Option[Seq[String]] = {
    for {
      nodes <- sequence(layers.map(layerTermToNode(_)))
      renderGraph <- Graph(inputs ++ nodes)
        .buildComplete(outputs)
        .toOption
    } yield fansi
      .Str(renderGraph.render)
      .plainText
      .linesIterator
      .map(_.replaceAll(" *$", ""))
      .toVector
      .reverse
      .dropWhile(_.isEmpty)
      .reverse
  }

  private def replaceComment(
      tree: Tree,
      outputs: List[Symbol],
      inputs: List[GraphNode],
      layers: List[Term]
  )(implicit
      doc: SemanticDocument
  ): Option[Patch] = {
    for {
      declaration <- findDeclarationRoot(tree)
      existingComment = treeComment(declaration)
      newline         = treeNewLineIndentation(declaration).getOrElse("\n")
      graph <- renderGraphLines(outputs, inputs, layers)
    } yield existingComment.map(Patch.removeTokens).asPatch +
      Patch.addLeft(declaration, graph.mkString("//", newline + "//", newline))
  }

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect {

      case t @ Term.Apply(provideMagicLayer(Term.Select(qual, _)), args) =>
        (for {
          outputs <- zioTermToInputs(qual)
          patch   <- replaceComment(t, outputs, Nil, args)
        } yield patch).asPatch

      case t @ Term.Apply(
            provideSomeMagicLayer(Term.ApplyType(Term.Select(qual, _), List(inTypes))),
            args
          ) =>
        (for {
          outputs <- zioTermToInputs(qual)
          inputs  <- typeToNodes(inTypes)
          patch   <- replaceComment(t, outputs, inputs, args)
        } yield patch).asPatch

      case t @ Term.Apply(provideCustomMagicLayer(Term.Select(qual, _)), args) =>
        (for {
          outputs <- zioTermToInputs(qual)
          zenv    <- zenvNode
          patch   <- replaceComment(t, outputs, List(zenv), args)
        } yield patch).asPatch

      case t @ Term.Apply(fromMagic(Term.ApplyType(_, List(outTypes))), args) =>
        (for {
          outputs <- typeToSymbols(outTypes)
          patch   <- replaceComment(t, outputs, Nil, args)
        } yield patch).asPatch

      case t @ Term.Apply(fromSomeMagic(Term.ApplyType(_, List(inTypes, outTypes))), args) =>
        (for {
          outputs <- typeToSymbols(outTypes)
          inputs  <- typeToNodes(inTypes)
          patch   <- replaceComment(t, outputs, inputs, args)
        } yield patch).asPatch

    }.asPatch
  }
}
