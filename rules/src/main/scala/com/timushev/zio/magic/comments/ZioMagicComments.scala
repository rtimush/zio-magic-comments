package com.timushev.zio.magic.comments

import _root_.scalafix.v1._
import zio.magic.macros.graph.{Eq, Graph, Node}
import zio.magic.macros.utils.RenderGraph

import scala.annotation.tailrec
import scala.meta._

class ZioMagicComments extends SemanticRule("ZioMagicComments") {

  private val provideMagicLayer: SymbolMatcher =
    SymbolMatcher.exact("zio/magic/package.ZioProvideMagicOps#provideMagicLayer().")

  private val zlayer: SymbolMatcher    = SymbolMatcher.exact("zio/ZLayer#")
  private val rlayer: SymbolMatcher    = SymbolMatcher.exact("zio/package.RLayer#")
  private val urlayer: SymbolMatcher   = SymbolMatcher.exact("zio/package.URLayer#")
  private val layer: SymbolMatcher     = SymbolMatcher.exact("zio/package.Layer#")
  private val ulayer: SymbolMatcher    = SymbolMatcher.exact("zio/package.ULayer#")
  private val tasklayer: SymbolMatcher = SymbolMatcher.exact("zio/package.TaskLayer#")

  private val zio: SymbolMatcher  = SymbolMatcher.exact("zio/ZIO#")
  private val rio: SymbolMatcher  = SymbolMatcher.exact("zio/package.RIO#")
  private val urio: SymbolMatcher = SymbolMatcher.exact("zio/package.URIO#")

  private val nothing: SymbolMatcher = SymbolMatcher.exact("scala/Nothing#")

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

  private def symbolType(symbol: Symbol)(implicit doc: SemanticDocument): Option[SemanticType] = {
    symbol.info.map(_.signature).collect { case MethodSignature(_, _, tpe) =>
      tpe
    }
  }

  private def layerTermToNode(
      term: Term
  )(implicit doc: SemanticDocument): Option[Node[Symbol, RenderGraph]] = {
    symbolType(term.symbol)
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

  private def zioTermToInputs(
      term: Term
  )(implicit doc: SemanticDocument): Option[List[Symbol]] = {
    symbolType(term.symbol)
      .flatMap {
        case TypeRef(_, zio(_), List(in, _, _))        => Some(in)
        case TypeRef(_, rio(_) | urio(_), List(in, _)) => Some(in)
        case _                                         => None
      }
      .map(typeMembers)
  }

  private def sequence[A](listOptions: List[Option[A]]): Option[List[A]] = {
    listOptions match {
      case Nil             => Some(Nil)
      case Some(a) :: tail => sequence(tail).map(a :: _)
      case None :: _       => None
    }
  }

  private def renderGraphLines(requirements: Term, provided: List[Term])(implicit
      doc: SemanticDocument
  ): Option[Seq[String]] = {
    for {
      requirements <- zioTermToInputs(requirements)
      nodes        <- sequence(provided.map(layerTermToNode(_)))
      renderGraph  <- Graph(nodes).buildComplete(requirements).toOption
    } yield fansi
      .Str(renderGraph.render)
      .plainText
      .linesIterator
      .map(_.replaceAll(" *$", ""))
      .toVector
  }

  override def fix(implicit doc: SemanticDocument): Patch = {
    doc.tree.collect { case t @ Term.Apply(provideMagicLayer(Term.Select(qual, _)), args) =>
      val maybePatch = for {
        declaration <- findDeclarationRoot(t)
        existingComment = treeComment(declaration)
        newline         = treeNewLineIndentation(declaration).getOrElse("\n")
        graph <- renderGraphLines(qual, args)
        newComment = graph.mkString("//", newline + "//", newline)
      } yield existingComment.map(Patch.removeTokens).asPatch +
        Patch.addLeft(declaration, newComment)
      maybePatch.asPatch
    }.asPatch
  }
}
