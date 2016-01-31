package monitor


import model.DeviceState

import scala.collection.mutable


trait MonitorManager {

  var monitors:mutable.MutableList[DeviceState] = mutable.MutableList()

  def setAnalogueOut(deviceId:String, analogueState:Int)
  def setDigitalOut(deviceId:String, digitalState:Boolean)

}

object MonitorManager extends  MonitorManager{

  override def setAnalogueOut(deviceId:String, analogueStateIn:Int)={
    monitors.find(deviceState => deviceState.id == deviceId).map(deviceState => {
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
}
