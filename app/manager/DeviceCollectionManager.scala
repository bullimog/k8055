package manager

import java.util.concurrent.TimeUnit

import connectors.{Configuration, DeviceConfigIO, K8055Board}
import model.Device._
import model.{Device, DeviceCollection, DeviceState}
import ActorGlobals._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object DeviceCollectionManager extends DeviceCollectionManager with DeviceManager{
  override val deviceConfigIO = DeviceConfigIO
  override val deviceController = DeviceManager
  override val monitorManager = MonitorAndStrobeManager
  override val configuration = Configuration
  override val k8055Board = K8055Board
}

trait DeviceCollectionManager{

  val deviceConfigIO:DeviceConfigIO
  val deviceController:DeviceManager
  val monitorManager:MonitorAndStrobeManager
  val configuration:Configuration
  val k8055Board:K8055Board

  def getDeviceCollection:DeviceCollection = {
    val oDeviceCollection: Option[DeviceCollection] = deviceConfigIO.readDeviceCollectionFromFile(configuration.filename)
    oDeviceCollection.fold(DeviceCollection("No Devices", "Error", List()))({
      deviceCollection => deviceCollection
    })
  }

  def getDevice(deviceId:String):Option[Device]={
    val deviceCollection:DeviceCollection = getDeviceCollection
    deviceCollection.devices.find(device => device.id == deviceId)
  }

  
  def readAndPopulateDevices(deviceCollection: DeviceCollection):DeviceCollection = {
    val populatedDevices = deviceCollection.devices.map(device =>
      if(device.deviceType == MONITOR)
        deviceController.readAndPopulateMonitor(device)
      else if(device.deviceType == STROBE)
        deviceController.readAndPopulateStrobe(device)
      else
        deviceController.readAndPopulateDevice(device)
    )
    deviceCollection.copy(devices = populatedDevices)
  }


  def upsertDevice(device: Device):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device.id)
    val deviceAdded = deviceRemoved ::: List(device)
    val dc = deviceCollection.copy(devices = deviceAdded)

    updateTransientDigitalOutData(device)
    updateTransientAnalogueOutData(device)
    //updateTransientStrobeOnTime(device)
    //updateTransientStrobeOffTime(device)
    putDeviceCollection(dc)
  }


  def updateTransientDigitalOutData(device: Device):Boolean = {
    (device.deviceType, device.digitalState) match{
      case (Device.DIGITAL_OUT, Some(dState)) => {
        k8055Board.setDigitalOut(device.channel, dState)
        true
      }
      case (Device.MONITOR, Some(dState)) => {
        monitorManager.setDigitalOut(device.id, dState)
        true
      }
      case (Device.MONITOR, None) => true
      case (Device.STROBE, Some(dState)) => {
        monitorManager.setDigitalOut(device.id, dState)
        true
      }
      case (Device.STROBE, None) => true
      case _ => false
    }
  }

  def updateTransientAnalogueOutData(device: Device):Boolean = {
    (device.deviceType, device.analogueState) match{
      case (Device.ANALOGUE_OUT, Some(aState)) =>
        k8055Board.setAnalogueOut(device.channel, aState)
      case (Device.MONITOR, Some(aState)) => {
        monitorManager.setAnalogueOut(device.id, aState)
      }
      case _ => false
    }
  }

  def updateTransientStrobeOnTime(device: Device):Boolean = {
    (device.deviceType, device.strobeOnTime) match{
      case (Device.STROBE, Some(aState)) => {
        monitorManager.setStrobeOnTime(device.id, aState)
      }
      case _ => false
    }
  }

  def updateTransientStrobeOffTime(device: Device):Boolean = {
    (device.deviceType, device.strobeOffTime) match{
      case (Device.STROBE, Some(aState)) => {
        monitorManager.setStrobeOffTime(device.id, aState)
      }
      case _ => false
    }
  }


  def getMonitorAnalogueOut(delta: Boolean, monitorOrStrobe: Device, monitorOrStrobeState: DeviceState) : Int = {
    val aRawState: Int = monitorManager.getAnalogueOut(monitorOrStrobe.id)
    if (delta)
      aRawState + monitorOrStrobeState.analogueState.getOrElse(0)
    else
      monitorOrStrobeState.analogueState.getOrElse(aRawState)
  }


  def getStrobeOnTime(delta: Boolean, strobe: Device, strobeState: DeviceState) : Int = {
    val aRawState: Int = monitorManager.getStrobeOnTime(strobe.id)
    if (delta)
      aRawState + strobeState.strobeOnTime.getOrElse(0)
    else
      strobeState.strobeOnTime.getOrElse(aRawState)
  }

  def getStrobeOffTime(delta: Boolean, strobe: Device, strobeState: DeviceState) : Int = {
    val aRawState: Int = monitorManager.getStrobeOffTime(strobe.id)
    if (delta)
      aRawState + strobeState.strobeOffTime.getOrElse(0)
    else
      strobeState.strobeOffTime.getOrElse(aRawState)
  }


  def patchDevice(deviceState: DeviceState, delta:Boolean):Boolean = {

    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices

    devices.find(d => d.id == deviceState.id).exists( device => {

      device.deviceType match {
        case STROBE => {
          deviceState.digitalState.map { enableStrobe =>
            if(enableStrobe && !strobeMessagesInQueue.contains(device.id)){

              val onSeconds = MonitorAndStrobeManager.getStrobeOnTime(device.id)
              val tickInterval = new FiniteDuration(onSeconds, TimeUnit.SECONDS)
              system.scheduler.scheduleOnce(tickInterval, strobeActorRef, Start(device.id))

              strobeMessagesInQueue += (device.id -> true)
            }
          }
          val MINIMUM_TIMEOUT = 1

          val strobeOnTime:Int = Math.max(MINIMUM_TIMEOUT, getStrobeOnTime(delta, device, deviceState))
          val strobeOffTime:Int = Math.max(MINIMUM_TIMEOUT, getStrobeOffTime(delta, device, deviceState))

          updateTransientStrobeOnTime(device.copy(strobeOnTime = Some(strobeOnTime)))
          updateTransientStrobeOffTime(device.copy(strobeOffTime = Some(strobeOffTime)))
          updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))

        }
        case MONITOR => {
          val aState:Int = getMonitorAnalogueOut(delta, device, deviceState)
          updateTransientAnalogueOutData(device.copy(analogueState = Some(aState)))
          updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
        }
        case ANALOGUE_OUT => {
          val aState = if (delta) {
            val aRawState = k8055Board.getAnalogueOut(device.channel)
            aRawState + deviceState.analogueState.getOrElse(0)
          }
          else
            deviceState.analogueState.getOrElse(0)

          updateTransientAnalogueOutData(device.copy(analogueState = Some(aState)))
        }
        case DIGITAL_OUT => updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
        case _ => false
      }
    })
  }


  def deleteDevice(device: Device):Boolean = {deleteDevice(device.id)}
  def deleteDevice(device: String):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device)
    val dc = deviceCollection.copy(devices = deviceRemoved)
    putDeviceCollection(dc)
  }

  def putDeviceCollection(deviceCollection: DeviceCollection):Boolean = {
    deviceConfigIO.writeDeviceCollectionToFile(configuration.filename, deviceCollection)
  }

}
