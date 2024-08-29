package me.astynax.sqlparser

sealed interface Result<R, E> {
  fun <A> map(f: (R) -> A): Result<A, E> = when (this) {
    is Success -> Success(f(this.value))
    is Failure -> Failure(this.error)
  }

  fun <A> mapError(f: (E) -> A): Result<R, A> = when (this) {
    is Success -> Success(this.value)
    is Failure -> Failure(f(this.error))
  }

  fun <A> andThen(f: (R) -> Result<A, E>): Result<A, E> = when (this) {
    is Success -> f(this.value)
    is Failure -> Failure(this.error)
  }
}
data class Success<R, E>(val value: R) : Result<R, E>
data class Failure<R, E>(val error: E) : Result<R, E>
