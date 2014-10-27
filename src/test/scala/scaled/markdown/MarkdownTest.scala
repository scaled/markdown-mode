//
// Scaled Markdown Mode - a Scaled major mode for editing Markdown files
// http://github.com/scaled/markdown-mode/blob/master/LICENSE

package scaled.markdown

import org.junit.Assert._
import org.junit._
import scaled._
import scaled.grammar._
import scaled.impl.BufferImpl

class MarkdownTest {

  val testMD = Seq(
    //                1         2         3
    //      0123456789012345678901234567890
    /* 0*/ "# Title",
    /* 1*/ "",
    /* 2*/ "## Subtitle",
    /* 3*/ "This is a nice [link](target) with inline target. Here's a [link] with external",
    /* 4*/ "target. And a [link][ref] with reference.",
    /* 7*/ "[link]: target").mkString("\n")

  val markdown = Grammar.parseNDF(getClass.getClassLoader.getResourceAsStream("Markdown.ndf"))
  val grammars = Grammar.Set(markdown)

  @Test def debugGrammar () {
    // markdown.print(System.out)
    // markdown.scopeNames foreach println

    val buffer = BufferImpl(new TextStore("Test.md", "", testMD))
    val scoper = new Scoper(grammars, buffer, Nil)
    // println(scoper.showMatchers(Set("#internalSubset", "#tagStuff", "#entity")))
    0 until buffer.lines.length foreach { ii => println(ii + ": " + scoper.showScopes(ii)) }
  }
}
