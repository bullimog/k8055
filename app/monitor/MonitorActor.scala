package monitor

import akka.actor.Actor
import controllers.DeviceCollectionController
import model.{DeviceState, Device, DeviceCollection}
import play.api.Logger

class MonitorActor extends MonitorActorTrait with Actor{
  override val deviceCollectionController = DeviceCollectionController

  def receive = {
    case "tick" => processActiveMonitors()
    case "stop" => context.stop(self)
    case _ => Logger.error("unknown message in MonitorActor")
  }
}

trait MonitorActorTrait{
  val deviceCollectionController:DeviceCollectionController

  def processActiveMonitors() = {
    activeMonitors.foreach(monitor => {
      performMonitor(monitor)
    })
  }

  def activeMonitors:List[Device]={
    val dc:DeviceCollection = deviceCollectionController.readAndPopulateDevices(deviceCollectionController.getDeviceCollection)
    val monitors = dc.devices.filter(device=> device.deviceType==Device.MONITOR)
    monitors.filter(device=>device.digitalState.fold(false)(digitalState=>digitalState))
  }


  def performMonitor(monitor: Device):Unit= {
    for {
      sensor <- monitor.monitorSensor.flatMap(id => deviceCollectionController.getDevice(id))
      increaser <- monitor.monitorIncreaser.flatMap(id => deviceCollectionController.getDevice(id))
      //decreaser <- monitor.monitorDecreaser.flatMap(id => deviceCollectionController.getDevice(id))
      target <- monitor.analogueState
    } yield {
      val sensorTargetDiff:Int = sensor.analogueState.fold(0)(sensorState => target - sensorState)

      if (sensorTargetDiff > 0) //switch on increaser only
        updateOutputDevice(increaser, calculateOutputSetting(sensorTargetDiff))
      else
        updateOutputDevice(increaser, 0)
    }
  }

  def updateOutputDevice(outputDevice:Device, outputVal:Int) = {
    val outputDeviceState = DeviceState(outputDevice.id, None, Some(outputVal))
    deviceCollectionController.patchDevice(outputDeviceState, delta = false)
  }


  def calculateOutputSetting(measurementDiff: Int): Int ={
    val maxPermittedDiff = 7
    val maxOutput = 255
    val outputFactor = 40

    if(measurementDiff > maxPermittedDiff) maxOutput
    else if(measurementDiff < 0) 0
    else measurementDiff * outputFactor
  }

}
