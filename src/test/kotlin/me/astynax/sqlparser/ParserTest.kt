package me.astynax.sqlparser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ParserTest {
  @Test
  fun `complex building`() {
    val p = Parser.build {
      val a = Parser.char('a').call()
      Parser.space.optional.call()
      val b = (Parser.char('b') or Parser.char('B')).call()
      Parser.eof.call()
      a to b
    }

    assertEquals('a' to 'B', p.parseOrNull("aB"))
    assertEquals('a' to 'b', p.parseOrNull("a b"))
    assertNull(p.parseOrNull(""))
    assertNull(p.parseOrNull("a"))
    assertNull(p.parseOrNull("abc"))
  }

  @Test
  fun repetition() {
    assertNotNull(Parser.char('a').repeat().parseOrNull(""))
    assertNotNull(Parser.char('a').repeat().parseOrNull("aaaaaaaaaab"))
    assertNotNull(Parser.char('a').repeat(max=3).parseOrNull("aaaaaaaaaab"))
    assertNull(Parser.char('a').repeat(min=1).parseOrNull(""))

    assertNotNull(Parser.build {
      Parser.char('a').repeat(max=3).call()
      Parser.char('a').call()
    }.parseOrNull("aaaa"))
  }
}
