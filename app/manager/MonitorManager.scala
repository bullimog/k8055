package manager


import model.DeviceState
import scala.collection.mutable
import util.Util._

trait MonitorManager {

  var monitors:mutable.MutableList[DeviceState] = mutable.MutableList()

  def setAnalogueOut(deviceId:String, analogueState:Int):Boolean
  def setDigitalOut(deviceId:String, digitalState:Boolean)
  def getAnalogueOut(deviceId:String):Int
  def getDigitalOut(deviceId:String):Boolean


}

object MonitorManager extends  MonitorManager{

  override def setAnalogueOut(deviceId:String, analogueStateOut:Int):Boolean={

    val byteAnalogueStateOut = boundByteValue(analogueStateOut)

    monitors.find(deviceState => deviceState.id == deviceId).fold({
      monitors += new DeviceState(deviceId,None, Some(byteAnalogueStateOut))
    })(deviceState => {
      val newDeviceState = deviceState.copy(analogueState = Some(byteAnalogueStateOut))
      monitors = monitors.filter(deviceState => deviceState.id != deviceId)
      monitors += newDeviceState
    })
    true
  }

  override def setDigitalOut(deviceId:String, digitalStateOut:Boolean)={
    monitors.find(deviceState => deviceState.id == deviceId).fold({
      monitors += new DeviceState(deviceId, Some(digitalStateOut), None)
    })(deviceState => {
      val newDeviceState = deviceState.copy(digitalState = Some(digitalStateOut))
      monitors = monitors.filter(deviceState => deviceState.id != deviceId)
      monitors += newDeviceState
    })
  }

  override def getAnalogueOut(deviceId:String):Int = {
    monitors.find(deviceState => deviceState.id == deviceId).fold(0)(deviceState => {
      deviceState.analogueState.getOrElse(0)
    })
  }

  override def getDigitalOut(deviceId:String):Boolean = {
    monitors.find(deviceState => deviceState.id == deviceId).fold(false)(deviceState => {
      deviceState.digitalState.getOrElse(false)
    })
  }
}
