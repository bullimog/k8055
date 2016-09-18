package connectors


import play.Logger
import util.Util._

object K8055Board extends K8055Board

trait K8055Board{

  // can't read output settings from card, so need to cache state here...
  var digitalOut:Byte = 0
  var analogueOut1:Int = 0  // 0 to 25,500  (which is 100*255, so we can convert Integers without loss)
  var analogueOut2:Int = 0

  val percentToStoreFactor:Int = 255   // 1% = 255 in the (0 to 25,500) store
  val byteToStoreFactor:Int = 100      // 1 bit = 100 in the store

  val K8055_TIMESTAMP = 0
  val K8055_DIGITAL = 1
  val K8055_ANALOG_1 = 2
  val K8055_ANALOG_2 = 3
  val K8055_COUNTER_1 = 4
  val K8055_COUNTER_2 = 5

  val LOWEST_BIT = 1
  val HIGHEST_BIT = 8

  val retryLimit = 3
  val defaultValues = "0;0;0;0;0;0"



  /** *******************************************************
    * Analogue Out
    **********************************************************/
  def getAnalogueOut(channel:Int): Int ={getAnAnalogueOut(channel, byteToStoreFactor)}
  def getAnAnalogueOut(channel:Int, factor: Double): Int ={

    channel match {
      case 1 => (analogueOut1 / factor).toInt
      case 2 => (analogueOut2 / factor).toInt
      case _ => 0
    }
  }

  def setAnalogueOut(channel:Int, value:Int): Boolean ={setAnAnalogueOut(channel, value, byteToStoreFactor)}
  def setAnAnalogueOut(channel:Int, value:Int, factor:Double): Boolean ={
    channel match{
      case 1 => analogueOut1 = (boundByteValue(value) * factor).toInt
      case 2 => analogueOut2 = (boundByteValue(value) * factor).toInt
      case _ =>
    }

    setStatus()
  }

  /** *******************************************************
    * Analogue In  0 - 255
    **********************************************************/
  def getAnalogueIn(channel:Int):Int = {
    (channel, readStatus()) match{
      case (1, Some(status)) => status(K8055_ANALOG_1)
      case (2, Some(status)) => status(K8055_ANALOG_2)
      case _ => 0
    }
  }

  /** ********************************************************
    * Digital Out
    **********************************************************/
  def getDigitalOut(channel:Int): Boolean ={
    if(channel >= LOWEST_BIT && channel <= HIGHEST_BIT){
      (digitalOut & byteMask(channel)) > 0
    }
    else{false}
  }

  def setDigitalOut(channel:Int, isOn:Boolean): Boolean ={
    if(channel >= LOWEST_BIT && channel <= HIGHEST_BIT) {
      if (isOn) setDigitalChannel(channel)
      else clearDigitalChannel(channel)
    } else false
  }

  //converts 1-8 to a binary digit mask
  def byteMask(i:Int): Int = {
    math.pow(2,i-1).toInt
  }


  def setDigitalChannel(channel:Int):Boolean = {
    if(channel >= LOWEST_BIT && channel <= HIGHEST_BIT) {
      digitalOut = (digitalOut | byteMask(channel)).toByte
      setStatus()
    }
    else false
  }

  def clearDigitalChannel(channel:Int):Boolean = {
    if(channel >= LOWEST_BIT && channel <= HIGHEST_BIT) {
      digitalOut = (digitalOut & (255 - byteMask(channel))).toByte
      setStatus()
    }
    else false
  }


  /** ********************************************************
    * Digital In
    **********************************************************/
  private def andBitsTogether(source:Int, mask:Int): Boolean = {
    if((source & mask) > 0) true
    else false
  }

  def getDigitalIn(channel:Int): Boolean ={
    if(channel >= LOWEST_BIT && channel <= HIGHEST_BIT) {
      readStatus().fold(false)(status => andBitsTogether(status(K8055_DIGITAL).toByte, byteMask(channel)))
    }
    else false
  }

  def getCount(channel: Int): Int = {
    (channel, readStatus()) match{
      case (1, Some(status)) => status(K8055_COUNTER_1)
      case (2, Some(status)) => status(K8055_COUNTER_2)
      case _ => {
        Logger.warn("Can only get count from channels 1 & 2, not "+channel)
        0
      }
    }
  }

  def resetCount(channel: Int): Boolean = {
    executeCommand(s"k8055 -reset$channel") != defaultValues
  }

  def getDigitalInLatch(channel: Int):Boolean = {
    val pressed:Boolean = getCount(channel) > 0
    resetCount(channel)
    pressed
  }


  /** ********************************************************
    * k8055 Communication
    **********************************************************/
  /**
   * Reads the status of the K8055, returning all read values.
   * @return Option[ Array[Int] ] representing the read values from the board
   */
  def readStatus():Option[Array[Int]] = {
    val result = executeCommand(s"k8055")
    if(result != defaultValues) {
      val retValues = result.replaceAll("\n", "").split(';')
      try {
        val expectedValCount = 6
        if (retValues.length == expectedValCount) {
          Some(for (strValue <- retValues)
            yield {
              strValue.toInt
            }
          )
        }
        else None
      }
      catch {
        case e: NumberFormatException => None
      }
    }
    else None
  }

  def resetStatus():Boolean = {
    executeCommand(s"k8055 -d:0 -a1:0 -a2:0 -reset1 -reset2") != defaultValues
  }

  private def setStatus():Boolean = {
    val byteVal1:Int = analogueOut1/byteToStoreFactor
    val byteVal2:Int = analogueOut2/byteToStoreFactor
    executeCommand(s"k8055 -d:$digitalOut -a1:$byteVal1 -a2:$byteVal2") != defaultValues
  }

  def executeCommand(command:String): String = {
    tryCommand(command, retryLimit)
  }

  def tryCommand(command:String, tries:Int): String = {
    if (tries > 0) {
      import sys.process.Process
      try {
        val result = Process("" + command + "")
        val output = result.!!
        if (output.indexOf("Could not open the k8055") >= 0) {
          Logger.error("#### Communication with k8055 failed: " + output)
          tryCommand(command, tries - 1)
        }
        else output
      }
      catch {
        case e: Exception => {
          Logger.error("#### Communication with k8055 failed. Is k8055 command installed? " + e)
          tryCommand(command, tries - 1)
        }
      }
    }
    else {
      Logger.error(s"#### Tried $retryLimit times, but couldn't get a good response from k8055")
      defaultValues
    }
  }
}
