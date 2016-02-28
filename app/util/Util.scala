package util

object Util {

  val MIN_BYTE_VALUE = 0
  val MAX_BYTE_VALUE = 255


  def boundByteValue(input:Int):Int = {
    if(input > MAX_BYTE_VALUE) MAX_BYTE_VALUE
    else
    if(input < MIN_BYTE_VALUE) MIN_BYTE_VALUE
    else input
  }
}
