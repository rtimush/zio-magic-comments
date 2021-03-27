package zio.magic.macros.comments

import zio.magic.macros.utils.{RenderedGraph => ZRenderedGraph}

class RenderedGraph private (val delegate: ZRenderedGraph) {
  def ++(that: RenderedGraph): RenderedGraph =
    new RenderedGraph(delegate ++ that.delegate)
  def >>>(that: RenderedGraph): RenderedGraph =
    new RenderedGraph(delegate >>> that.delegate)
  def render: String =
    delegate.render
}

object RenderedGraph {
  def apply(value: String): RenderedGraph = new RenderedGraph(ZRenderedGraph(value))
  def empty: RenderedGraph                = new RenderedGraph(ZRenderedGraph.Row(Nil))
}
