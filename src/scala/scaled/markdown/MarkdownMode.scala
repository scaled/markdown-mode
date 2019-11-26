//
// Scaled Markdown Mode - a Scaled major mode for editing Markdown files
// http://github.com/scaled/todo-mode/blob/master/LICENSE

package scaled.markdown

import scaled._
import scaled.grammar._
import scaled.Matcher
import scaled.util.{Filler, Paragrapher}

@Plugin(tag="textmate-grammar")
class MarkdownGrammarPlugin extends GrammarPlugin {
  import EditorConfig._
  import scaled.major.TextConfig._

  override def grammars = Map("text.html.markdown" -> "Markdown.ndf")

  override def effacers = List(
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
}

@Major(name="markdown",
       tags=Array("text", "project", "markdown"),
       pats=Array(".*\\.md", ".*\\.markdown"),
       desc="A major mode for editing Markdown files.")
class MarkdownMode (env :Env) extends GrammarTextMode(env) {

  override def dispose () :Unit = {} // nada for now

  override def langScope = "text.html.markdown"
  // override def stylesheets = stylesheetURL("/todo.css") :: super.stylesheets

  override def mkParagrapher (syntax :Syntax) = new Paragrapher(syntax, buffer) {
    // don't extend paragraph upwards if the current top is a list item or header
    override def canPrepend (row :Int) =
      super.canPrepend(row) && !isListItemOrHeader(line(row+1))
    // don't extend paragraph downwards if the new line is a list item or header
    override def canAppend (row :Int) =
      super.canAppend(row) && !isListItemOrHeader(line(row))

    private def isListItemOrHeader (line :LineV) = {
      var start = line.firstNonWS
       line.charAt(start) == '#' || line.matches(ListItemM, start)
    }
  }

  override def refillLinesIn (start :Loc, end :Loc) :Unit = {
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
