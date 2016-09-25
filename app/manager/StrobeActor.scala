package manager

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import model.{Device, DeviceCollection, DeviceState}
import play.api.Logger
import ActorGlobals._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.FiniteDuration

class StrobeActor extends StrobeActorTrait with Actor{
  override val deviceCollectionManager = DeviceCollectionManager
  override val deviceManager = DeviceManager

  def receive = {
    case ("start", deviceId) => receivedAStart(deviceId.asInstanceOf[String])
    case ("stop", deviceId)  => receivedAStop(deviceId.asInstanceOf[String])
    case _ => Logger.error("unknown message in StrobeActor")
  }
}


trait StrobeActorTrait{
  val deviceCollectionManager:DeviceCollectionManager
  val deviceManager:DeviceManager

  def receivedAStart(strobeDeviceId: String) = {
    println("##received a  start")

    if(MonitorManager.getDigitalOut(strobeDeviceId)) {
      findAndSetDigitalOutDevice(strobeDeviceId, outputState = true)

      deviceCollectionManager.getDevice(strobeDeviceId).map { strobe =>
        val onSeconds = MonitorManager.getAnalogueOut(strobeDeviceId)
        val tickInterval = new FiniteDuration(onSeconds, TimeUnit.SECONDS)
        system.scheduler.scheduleOnce(tickInterval, monitorActorRef, ("stop", strobeDeviceId)) //initialDelay, delay, Actor, Message
      }
    }
  }

  def receivedAStop(strobeDeviceId: String) = {
    println("##received a  stop")

    if(MonitorManager.getDigitalOut(strobeDeviceId)) {
      findAndSetDigitalOutDevice(strobeDeviceId, outputState = false)

      deviceCollectionManager.getDevice(strobeDeviceId).map { strobe =>
        val offSeconds = MonitorManager.getAnalogueOut2(strobeDeviceId)
        val tickInterval = new FiniteDuration(offSeconds, TimeUnit.SECONDS)
        system.scheduler.scheduleOnce(tickInterval, monitorActorRef, ("start", strobeDeviceId)) //initialDelay, delay, Actor, Message
      }
    }
  }


  def findAndSetDigitalOutDevice(strobeDeviceId: String, outputState: Boolean) = {
    deviceCollectionManager.getDevice(strobeDeviceId).map{ strobeDevice =>
      strobeDevice.monitorIncreaser.map { increaserId =>
        deviceCollectionManager.getDevice(increaserId).map { increaser =>
          updateDigitalOutputDevice(increaser, outputState)
        }
      }
    }
  }


  private[manager] def updateDigitalOutputDevice(outputDevice:Device, outputState:Boolean) = {
    val outputDeviceState = DeviceState(outputDevice.id, Some(outputState), None)
    deviceCollectionManager.patchDevice(outputDeviceState, delta = false)
  }

/*
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

  private[manager] def updateAnalogueOutputDevice(outputDevice:Device, outputVal:Int) = {
    val outputDeviceState = DeviceState(outputDevice.id, None, Some(outputVal))
    deviceCollectionController.patchDevice(outputDeviceState, delta = false)
  }


  private[manager] def monitorDigitalIn(activeMonitor: Device, digitalSensor:Device) = {
    for {
      increaser <- activeMonitor.monitorIncreaser.flatMap(id => deviceCollectionController.getDevice(id))
    } yield {
      if(increaser.deviceType == Device.DIGITAL_OUT) {
        monitorDigitalInToDigitalOut(activeMonitor, digitalSensor, increaser)
      }
    }
  }

  private[manager] def monitorDigitalInToDigitalOut(activeMonitor:Device, digitalSensor:Device, increaser:Device) = {
    val flipDigital = activeMonitor.flipDigitalMonitorState.fold(false)(fd => fd)
    digitalSensor.digitalState.fold() { sensorDigitalState => {
      val digitalState:Boolean = if (flipDigital) !sensorDigitalState else sensorDigitalState
      updateDigitalOutputDevice(increaser, digitalState)
    }}
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
*/
}
