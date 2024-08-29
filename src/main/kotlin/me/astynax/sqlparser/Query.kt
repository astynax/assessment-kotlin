package me.astynax.sqlparser

data class Query(
  val columns: List<Column>,
  val sources: List<Source>,
  val joins: List<Join>
) {
  companion object {
    val parser: Parser<Query> = Parser.build {
        "SELECT".p.call()
        sp.call()

        Query(listOf(), listOf(), listOf())
      }

    private val String.p get() = Parser.string(this, ignoreCase = true)
    private val sp = Parser.space.repeat(min = 1)

    private val alphanum = Parser
      .satisfy("[_a-z0-9]") { it.isLetterOrDigit() || it == '_' }

    private val identifier: Parser<String> = Parser
      .satisfy("[_a-z]") { it.isLetter() || it == '_'}
      .andThen { c -> alphanum.repeat()
        .map { buildString { append(c); append(it.toCharArray()) } } }

    private val quotedString: Parser<String> = Parser
      .satisfy("[^\"]", { it != '"' })
      .repeat().between(Parser.char('"'), Parser.char('"'))
      .map { buildString { append(it.toCharArray()) } }

    val column: Parser<List<String>> =
      (identifier or quotedString).separatedBy(Parser.char('.'))
  }
}

sealed interface Column {
  val alias: String
}
data class Named(val name: List<String>, override val alias: String): Column

sealed interface Source
data class Table(val name: String): Source

data class Join(
  val type: Type,
  val table: String,
  val on: String,
) {
  enum class Type {
    Left, Right, Inner, Outer
  }
}
