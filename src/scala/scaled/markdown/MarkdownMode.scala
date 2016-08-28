//
// Scaled Markdown Mode - a Scaled major mode for editing Markdown files
// http://github.com/scaled/todo-mode/blob/master/LICENSE

package scaled.markdown

import scaled._
import scaled.grammar._
import scaled.major.TextConfig
import scaled.Matcher
import scaled.util.Filler

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

  val grammars = resource("Markdown.ndf")(Grammar.parseNDFs)
}

@Major(name="markdown",
       tags=Array("text", "project", "markdown"),
       pats=Array(".*\\.md", ".*\\.markdown"),
       desc="A major mode for editing Markdown files.")
class MarkdownMode (env :Env) extends GrammarTextMode(env) {

  override def dispose () {} // nada for now

  override def configDefs = MarkdownConfig :: super.configDefs
  // override def stylesheets = stylesheetURL("/todo.css") :: super.stylesheets
  override protected def grammars = MarkdownConfig.grammars.get
  override protected def effacers = MarkdownConfig.effacers

  override def refillLinesIn (start :Loc, end :Loc) {
    // determine whether the first line starts with a list item char (+-#*) followed by space
    var first = buffer.line(start)
    var firstStart = first.firstNonWS
    if (!first.matches(ListItemM, firstStart)) super.refillLinesIn(start, end)
    else {
      val r = trimRegion(start, end)
      val orig = buffer.region(r)

      // add the text to a filler configured with the appropriate width
      val prefixLen = firstStart+ListItemM.matchLength
      val filler = new Filler(fillColumn-prefixLen)
      filler.append(first.view(prefixLen, first.length))
      orig.drop(1) foreach(filler.append)

      // now prepend the appropriate prefix back onto each filled line and create lines
      val result = Seq.builder[Line]
      val filled = filler.filled
      result += first.view(0, prefixLen) merge Line(filled.head)
      // subsequent lines get whitespace as a prefix
      var repeatPre = " " * prefixLen
      filled.drop(1) foreach { f => result += Line(f.insert(0, repeatPre)) }
      var flines = result.build()

      if (flines != orig) buffer.replace(r, flines)
      else window.emitStatus("Region already filled.")
    }
  }

  private val ListItemM = Matcher.regexp("""(-|\+|\*|\d+\.) """)
}
