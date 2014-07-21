//
// Scaled Markdown Mode - a Scaled major mode for editing Markdown files
// http://github.com/scaled/todo-mode/blob/master/LICENSE

package scaled.markdown

import scaled._
import scaled.grammar._
import scaled.major.TextConfig

object MarkdownConfig extends Config.Defs {
  import EditorConfig._
  import TextConfig._
  import GrammarConfig._

  // maps TextMate grammar scopes to Scaled style definitions
  val effacers = List(
    effacer("markup.heading.1", headerStyle),
    effacer("markup.heading.2", subHeaderStyle),
    effacer("markup.heading.3", sectionStyle),
    effacer("markup.heading.4", sectionStyle),
    effacer("markup.heading.5", sectionStyle),
    effacer("markup.heading.6", sectionStyle),
    effacer("markup.list", listStyle),

    effacer("markup.uri", linkStyle),
    effacer("markup.link.text", linkStyle),
    effacer("markup.link.target", refStyle),
    effacer("markup.link.ref", refStyle),
    effacer("markup.link.title", sectionStyle),
    effacer("markup.email", refStyle),

    effacer("markup.rule", subHeaderStyle),
    effacer("markup.list", listStyle),
    effacer("markup.pre", listStyle),
    effacer("markup.blockquote", listStyle),
    effacer("markup.bold", boldStyle),
    effacer("markup.italic", italicStyle),

    effacer("markup.code.inline", listStyle),
    effacer("markup.punctuation.code.block", refStyle),
    effacer("markup.code.block", listStyle),
    effacer("markup.code.lang", subHeaderStyle)
  )

  def mdGrammar = Grammar.parseNDF(stream("Markdown.ndf"))
  lazy val grammars = Seq(mdGrammar)
}

@Major(name="markdown",
       tags=Array("text", "project", "markdown"),
       pats=Array(".*\\.md", ".*\\.markdown"),
       desc="A major mode for editing Markdown files.")
class MarkdownMode (env :Env) extends GrammarTextMode(env) {

  override def dispose () {} // nada for now

  override def configDefs = MarkdownConfig :: super.configDefs
  // override def stylesheets = stylesheetURL("/todo.css") :: super.stylesheets
  override protected def grammars = MarkdownConfig.grammars
  override protected def effacers = MarkdownConfig.effacers
}
