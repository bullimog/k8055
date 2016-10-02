package manager


import model.DeviceState
import scala.collection.mutable
import util.Util._


/*
* Contains and controls access the Monitor and Strobe virtual internal state,
* as the real device state is handled by K8055Board
* */
trait MonitorAndStrobeManager {

  val DEFAULT_ONE_SECOND = 1
  var monitorsAndStrobes:mutable.MutableList[DeviceState] = mutable.MutableList()

  def setAnalogueOut(deviceId:String, analogueState:Int):Boolean
  def setStrobeOffTime(deviceId:String, strobeOffTime:Int):Boolean
  def setStrobeOnTime(deviceId:String, strobeOffTime:Int):Boolean
  def getAnalogueOut(deviceId:String):Int
  def getStrobeOffTime(deviceId:String):Int
  def getStrobeOnTime(deviceId:String):Int
  def setDigitalOut(deviceId:String, digitalState:Boolean)
  def getDigitalOut(deviceId:String):Boolean
}

object MonitorAndStrobeManager extends  MonitorAndStrobeManager{

  override def setAnalogueOut(deviceId:String, analogueStateOut:Int):Boolean={

    val byteAnalogueStateOut = boundByteValue(analogueStateOut)

    monitorsAndStrobes.find(deviceState => deviceState.id == deviceId).fold({
      monitorsAndStrobes += new DeviceState(deviceId,None, Some(byteAnalogueStateOut))
    })(deviceState => {
      val newDeviceState = deviceState.copy(analogueState = Some(byteAnalogueStateOut))
      monitorsAndStrobes = monitorsAndStrobes.filter(deviceState => deviceState.id != deviceId)
      monitorsAndStrobes += newDeviceState
    })
    true
  }

  override def setStrobeOffTime(deviceId:String, strobeOffTimeOut:Int):Boolean={
 //   println("######## setStrobeOffTime: " + strobeOffTimeOut)
    val byteStrobeOffTimeOut = boundByteValue(strobeOffTimeOut)

    monitorsAndStrobes.find(deviceState => deviceState.id == deviceId).fold({
      monitorsAndStrobes += new DeviceState(deviceId,None, None, Some(byteStrobeOffTimeOut))
    })(deviceState => {
      val newDeviceState = deviceState.copy(strobeOffTime = Some(byteStrobeOffTimeOut))
      monitorsAndStrobes = monitorsAndStrobes.filter(deviceState => deviceState.id != deviceId)
      monitorsAndStrobes += newDeviceState
    })
    true
  }

  override def setStrobeOnTime(deviceId:String, strobeOnTimeOut:Int):Boolean={
//    println("######## setStrobeOnTime: " + strobeOnTimeOut)
    val byteStrobeOnTimeOut = boundByteValue(strobeOnTimeOut)

    monitorsAndStrobes.find(deviceState => deviceState.id == deviceId).fold({
      monitorsAndStrobes += new DeviceState(deviceId,None, None, Some(byteStrobeOnTimeOut))
    })(deviceState => {
      val newDeviceState = deviceState.copy(strobeOnTime = Some(byteStrobeOnTimeOut))
      monitorsAndStrobes = monitorsAndStrobes.filter(deviceState => deviceState.id != deviceId)
      monitorsAndStrobes += newDeviceState
    })
    true
  }


  override def setDigitalOut(deviceId:String, digitalStateOut:Boolean)={
    monitorsAndStrobes.find(deviceState => deviceState.id == deviceId).fold({
      monitorsAndStrobes += new DeviceState(deviceId, Some(digitalStateOut), None)
    })(deviceState => {
      val newDeviceState = deviceState.copy(digitalState = Some(digitalStateOut))
      monitorsAndStrobes = monitorsAndStrobes.filter(deviceState => deviceState.id != deviceId)
      monitorsAndStrobes += newDeviceState
    })
  }

  override def getAnalogueOut(deviceId:String):Int = {
    monitorsAndStrobes.find(deviceState => deviceState.id == deviceId).fold(0)(deviceState => {
      deviceState.analogueState.getOrElse(0)
    })
  }

  override def getStrobeOffTime(deviceId:String):Int = {
    monitorsAndStrobes.find(deviceState => deviceState.id == deviceId).fold(DEFAULT_ONE_SECOND)(deviceState => {
      deviceState.strobeOffTime.getOrElse(0)
    })
  }

  override def getStrobeOnTime(deviceId:String):Int = {
    monitorsAndStrobes.find(deviceState => deviceState.id == deviceId).fold(DEFAULT_ONE_SECOND)(deviceState => {
      deviceState.strobeOnTime.getOrElse(0)
    })
  }

  override def getDigitalOut(deviceId:String):Boolean = {
    monitorsAndStrobes.find(deviceState => deviceState.id == deviceId).fold(false)(deviceState => {
      deviceState.digitalState.getOrElse(false)
    })
  }
}
