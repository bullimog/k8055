package monitor


import model.DeviceState

import scala.collection.mutable


trait MonitorManager {

  var monitors:mutable.MutableList[DeviceState] = mutable.MutableList()

  def setAnalogueOut(deviceId:String, analogueState:Int)
  def setDigitalOut(deviceId:String, digitalState:Boolean)
  def getAnalogueOut(deviceId:String):Int
  def getDigitalOut(deviceId:String):Boolean


}

object MonitorManager extends  MonitorManager{

  override def setAnalogueOut(deviceId:String, analogueStateIn:Int):Unit={
    monitors.find(deviceState => deviceState.id == deviceId).fold({
      monitors += new DeviceState(deviceId,None, Some(analogueStateIn))
    })(deviceState => {
      val newDeviceState = deviceState.copy(analogueState = Some(analogueStateIn))
      monitors = monitors.filter(deviceState => deviceState.id != deviceId)
      monitors += newDeviceState
    })
  }

  override def setDigitalOut(deviceId:String, digitalStateIn:Boolean)={
    monitors.find(deviceState => deviceState.id == deviceId).map(deviceState => {
      val newDeviceState = deviceState.copy(digitalState = Some(digitalStateIn))
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
