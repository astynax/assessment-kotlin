package me.astynax.sqlparser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class QueryTest {
  @Test
  fun `column name parsing`() {
    assertEquals(
      listOf("foo1_1", "bar baz", "_q2ux"),
      Query.column.parseOrNull("foo1_1.\"bar baz\"._q2ux")
    )
    assertNull(Query.column.parseOrNull("1oo"))
  }
}
