package manager

import akka.actor.Actor
import model.{DeviceState, Device, DeviceCollection}
import play.api.Logger

class MonitorActor extends MonitorActorTrait with Actor{
  override val deviceCollectionController = DeviceCollectionManager
  override val deviceManager = DeviceManager

  def receive = {
    case "tick" => processActiveMonitors()
    case "stop" => context.stop(self)
    case _ => Logger.error("unknown message in MonitorActor")
  }
}

trait MonitorActorTrait{
  val deviceCollectionController:DeviceCollectionManager
  val deviceManager:DeviceManager

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


  def performMonitor(activeMonitor: Device):Unit= {
    for {
      sensor <- activeMonitor.monitorSensor.flatMap(id => deviceCollectionController.getDevice(id))
    } yield {
      val popSensor = deviceManager.readAndPopulateDevice(sensor)

      if (sensor.deviceType == Device.ANALOGUE_IN)
        monitorAnalogueIn(activeMonitor, popSensor)
      else if(sensor.deviceType == Device.DIGITAL_IN)
        monitorDigitalIn(activeMonitor, popSensor)
    }
  }
  

  private[manager] def monitorAnalogueIn(activeMonitor: Device, analogueSensor: Device)  {
    for {
      increaser <- activeMonitor.monitorIncreaser.flatMap(id => deviceCollectionController.getDevice(id))
    } yield {
      if(increaser.deviceType == Device.ANALOGUE_OUT) {
        monitorAnalogueInToAnalogueOut(activeMonitor, analogueSensor, increaser)
      }
    }
  }
  
  private[manager] def monitorAnalogueInToAnalogueOut(activeMonitor:Device, analogueSensor:Device, increaser:Device) = {
    for {
      target <- activeMonitor.analogueState
    }
    yield {
      val sensorTargetDiff: Int = analogueSensor.analogueState.fold(0)(sensorState => target - sensorState)

      if (sensorTargetDiff > 0) //switch on increaser only
        updateAnalogueOutputDevice(increaser, calculateOutputSetting(sensorTargetDiff))
      else
        updateAnalogueOutputDevice(increaser, 0)
    }
  }

  private[manager] def updateAnalogueOutputDevice(outputDevice:Device, outputVal:Int):Boolean = {
    val outputDeviceState = DeviceState(outputDevice.id, None, Some(outputVal))
    deviceCollectionController.patchDevice(outputDeviceState, delta = false)
  }

  private[manager] def monitorDigitalIn(activeMonitor: Device, digitalSensor:Device):Unit = {
    for {
      increaser <- activeMonitor.monitorIncreaser.flatMap(id => deviceCollectionController.getDevice(id))
    } yield {
      if(increaser.deviceType == Device.DIGITAL_OUT) {
        monitorDigitalInToDigitalOut(activeMonitor, digitalSensor, increaser)
      } else if(increaser.deviceType == Device.STROBE) {
        monitorDigitalInToStrobe(activeMonitor, digitalSensor, increaser)
      }
    }
  }

  private[manager] def monitorDigitalInToDigitalOut(activeMonitor:Device, digitalSensor:Device, increaser:Device) = {
    val flipDigital = activeMonitor.flipDigitalState.fold(false)(fd => fd)
    digitalSensor.digitalState.fold() { sensorDigitalState => {
      val digitalState:Boolean = if (flipDigital) !sensorDigitalState else sensorDigitalState
      updateDigitalOutputDevice(increaser, digitalState)
    }}
  }

  private[manager] def updateDigitalOutputDevice(outputDevice:Device, outputState:Boolean) = {
    val outputDeviceState = DeviceState(outputDevice.id, Some(outputState), None)
    deviceCollectionController.patchDevice(outputDeviceState, delta = false)
  }

  private[manager] def monitorDigitalInToStrobe(activeMonitor:Device, digitalSensor:Device, strobe:Device) = {
    val flipDigital = activeMonitor.flipDigitalState.fold(false)(fd => fd)
    digitalSensor.digitalState.fold() { sensorDigitalState => {
      val digitalState:Boolean = if (flipDigital) !sensorDigitalState else sensorDigitalState
      updateStrobeDevice(strobe, digitalState)
    }}
  }

  private[manager] def updateStrobeDevice(strobe:Device, outputState:Boolean) = {
    val outputDeviceState = DeviceState(strobe.id, Some(outputState), None)
    deviceCollectionController.patchDevice(outputDeviceState, delta = true)
  }


  private[manager] def calculateOutputSetting(measurementDiff: Int): Int ={
    //TODO: Add these into config
    val maxPermittedDiff = 7
    val maxOutput = 255
    val outputFactor = 40

    if(measurementDiff > maxPermittedDiff) maxOutput
    else if(measurementDiff < 0) 0
    else measurementDiff * outputFactor
  }

}
