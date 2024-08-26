package me.astynax.ipcounter

import java.io.File

class Counter {

  @OptIn(ExperimentalUnsignedTypes::class)
  private class BitSet {
    private val data = UByteArray(16384)

    fun addPair(b1: UByte, b2: UByte) {
      val mask = 1.toUByte().rotateLeft(b2.toInt() and 7)
      val index = b1.toInt() * 32 + b2.toInt().div(8)
      data[index] = data[index] or mask
    }

    fun count(): Int = data.sumOf { it.countOneBits() }
  }

  // Each key of this Map is a pair of first two bytes of IPv4 and each value
  // is a bitset that stores the rest two bytes of the address as a single bit.
  private val sets = mutableMapOf<Int, BitSet>()

  private fun store(b1: UByte, b2: UByte, b3: UByte, b4: UByte) {
    sets.getOrPut(b1.toInt() * 256 + b2.toInt()) { BitSet() }
      .addPair(b3, b4)
  }

  fun store(address: String) {
    val parts = requireNotNull(
      address.split('.').mapNotNull { chunk ->
        chunk.toIntOrNull()
          ?.takeIf { it in 0..255 }
          ?.toUByte()
      }.takeIf { it.size == 4 }
    ) {
      IllegalArgumentException("Bad IPv4 Address: $address")
    }
    store(parts[0], parts[1], parts[2], parts[3])
  }

  fun count(): Int = sets.values.sumOf { it.count() }
}

private fun File.countIPv4Addresses(): Int {
  val counter = Counter()
  useLines { lines ->
    lines.forEach { counter.store(it) }
  }
  return counter.count()
}

fun main() {
  println(File("ip_addresses.txt").countIPv4Addresses())
}
