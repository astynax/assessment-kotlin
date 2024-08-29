package me.astynax.sqlparser

import javax.swing.JPopupMenu.Separator

data class State(val input: String, val pos: Int)

typealias R<T> = Result<Pair<T, State>, String>
private fun <T> done(value: T, offset: Int, state: State): R<T> =
  Success(value to state.copy(pos = state.pos + offset))

data class Parser<T>(val apply: (state: State) -> R<T>) {
  fun parse(input: String): Result<T, String> =
    apply(State(input, 0)).map { it.first }

  fun parseOrNull(input: String): T? =
    when (val result = parse(input)) {
      is Success -> result.value
      is Failure -> null
    }

  fun <G> map(f: (T) -> G): Parser<G> =
    Parser { s -> apply(s).map { it.mapFirst(f) } }

  infix fun or(other: Parser<T>): Parser<T> = Parser { s ->
    when (val result = apply(s)) {
      is Success -> result
      is Failure -> other.apply(s)
        .mapError { e -> "${result.error} | $e" }
    }
  }

  val optional: Parser<T?>
    get() = this.map { x -> x as T? } or pure(null)

  fun repeat(min: Int = 0, max: Int = Int.MAX_VALUE): Parser<List<T>> {
    val p = this.optional
    return build {
      buildList {
        for (i in 1 .. max) {
          when (val x = p.call()) {
            null -> break
            else -> add(x)
          }
        }
      }.also {
        if (it.size < min)
          fail("Not enough repeats: ${it.size} instead of $min")
      }
    }
  }

  fun <G> andThen(p: (T) -> Parser<G>): Parser<G> = Parser { s ->
    apply(s).andThen { (x, ss) -> p(x).apply(ss) }
  }

  fun <G> then(p: Parser<G>): Parser<G> = Parser { s ->
    apply(s).andThen { (_, ss) -> p.apply(ss) }
  }

  fun <G> with(p: Parser<G>): Parser<T> = Parser { s ->
    apply(s).andThen { (x, ss) -> p.apply(ss).map { it.mapFirst { x } } }
  }

  fun separatedBy(separator: Parser<*>): Parser<List<T>> =
    this.andThen { head ->
      separator.then(this).repeat().map { tail ->
        listOf(head) + tail
      }
    }

  fun between(p1: Parser<*>, p2: Parser<*>): Parser<T> =
    p1.then(this).with(p2)

  companion object {
    fun <T> pure(value: T) = Parser { s -> Success(value to s) }

    val eof = Parser { s ->
      if (s.pos == s.input.length) Success(Unit to s)
      else Failure("Expected EOF at ${s.pos}")
    }

    fun satisfy(description: String, test: (Char) -> Boolean) = satisfy({ description }, test)
    fun satisfy(description: () -> String, test: (Char) -> Boolean) = Parser { s ->
      if (s.pos >= s.input.length)
        return@Parser Failure("Input underflow")
      val c = s.input[s.pos]
      if (test(c)) done(c, 1, s)
      else Failure("Expected ${description()} at ${s.pos}")
    }

    fun char(char: Char) = satisfy({ "char '$char'" }) { it == char }

    val space = satisfy({ "whitespace" }) { it.isWhitespace() }

    fun string(example: String, ignoreCase: Boolean = false) = Parser { s ->
      if (s.pos + example.length >= s.input.length)
        return@Parser Failure("Input underflow")

      val c = s.input.substring(s.pos, s.pos + example.length)
      val equal =
        if (ignoreCase) c.lowercase() == example.lowercase()
        else c == example

      if (equal) done(c, example.length, s)
      else Failure("Expected '$example' at ${s.pos}")
    }

    fun <T> build(body: Builder.() -> T): Parser<T> =
      Parser { s -> Builder(s).tryToRun(body) }
  }
}

data class Builder(private var state: State) {
  private data class Abort(val error: String): Exception(error)

  fun <T> Parser<T>.call() =
    when (val result = apply(state)) {
      is Success -> result.value.let { (v, s) -> v.also { state = s } }
      is Failure -> throw Abort(result.error)
    }

  fun fail(message: String) { throw Abort(message) }

  internal fun <T> tryToRun(body: Builder.() -> T): R<T> =
    try {
      Success(run(body) to state)
    } catch (e: Abort) {
      Failure(e.error)
    }
}

inline fun <A, B, C> Pair<A, B>.mapFirst(f: (A) -> C): Pair<C, B> = f(first) to second
