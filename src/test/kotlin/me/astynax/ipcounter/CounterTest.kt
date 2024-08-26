package me.astynax.ipcounter

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class CounterTest {

  private lateinit var counter: Counter

  @BeforeEach
  fun setUp() {
    counter = Counter()
  }

  @Test
  fun `store and count`() {
    assertEquals(0, counter.count())
    counter.store("127.0.0.1")
    counter.store("255.255.255.255")
    assertEquals(2, counter.count())
  }

  @Test
  fun `store the same IP many times`() {
    assertEquals(0, counter.count())
    counter.store("127.0.0.1")
    counter.store("127.0.0.1")
    assertEquals(1, counter.count())
  }

  @Test
  fun `IPv4 validation`() {
    assertThrows<IllegalArgumentException> {
      counter.store("-1.0.0.2")
    }
    assertThrows<IllegalArgumentException> {
      counter.store("127.0.0")
    }
    assertThrows<IllegalArgumentException> {
      counter.store("127.0.0.0.1")
    }
    assertThrows<IllegalArgumentException> {
      counter.store("127.0.0.256")
    }
  }
}
