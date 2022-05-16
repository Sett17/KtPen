package me.sett.common

enum class PenEvent(val id: Byte) {
  HOVER_MOVE(0x03),
  HOVER_EXIT(0x02),
  CONTACT_MOVE(0x13),
  CONTACT_DOWN(0x11),
  CONTACT_UP(0x12),
  DUMMY(0x00);

  companion object {
    fun find(value: Byte): PenEvent? = values().find { it.id == value }
  }
}

data class PenPacket(val event: PenEvent, val arg1: Float = 0f, val arg2: Float = 0f, val arg3: Float = 0f, val buttonPressed: Boolean = false) {
  companion object {
    val size = PenPacket(PenEvent.DUMMY).encode().size
  };
  override fun toString(): String {
    return "${this.event.name}, ${this.arg1}, ${this.arg2}, ${this.arg3}, ${this.buttonPressed}"
  }
}

fun PenPacket.encode(): ByteArray {
  return byteArrayOf(this.event.id) + arg1.toBytes() + arg2.toBytes() + arg3.toBytes() + buttonPressed.toByte()
}

fun PenPacket.Companion.decode(data: ByteArray): PenPacket {
  if (data.size != size) throw IllegalArgumentException("Invalid packet size")
  return when (val event = PenEvent.find(data[0])!!) {
    PenEvent.HOVER_MOVE, PenEvent.CONTACT_MOVE -> {
      PenPacket(event, byteArrayOf(data[1], data[2], data[3], data[4]).toFloat(), byteArrayOf(data[5], data[6], data[7], data[8]).toFloat(), byteArrayOf(data[9], data[10], data[11], data[12]).toFloat(), data[13] == 0x01.toByte())
    }
    else -> {
      PenPacket(event)
    }
  }
}

fun Float.toBytes(): ByteArray {
  val buffer = ByteArray(4)
  for (i in 0..3) buffer[i] = (this.toRawBits() shr (i * 8)).toByte()
  return buffer
}

fun ByteArray.toFloat(): Float {
  return Float.fromBits(
    (this[3].toInt() shl 24) or
        (this[2].toInt() and 0xff shl 16) or
        (this[1].toInt() and 0xff shl 8) or
        (this[0].toInt() and 0xff)
  )
}

fun Boolean.toByte(): Byte = if (this) 0x01 else 0x00