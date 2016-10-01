package manager

import java.util.concurrent.TimeUnit

import connectors.{Configuration, DeviceConfigIO, K8055Board}
import model.Device._
import model.{Device, DeviceCollection, DeviceState}
import ActorGlobals._
import akka.actor.Cancellable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object DeviceCollectionManager extends DeviceCollectionManager with DeviceManager{
  override val deviceConfigIO = DeviceConfigIO
  override val deviceController = DeviceManager
  override val monitorManager = MonitorManager
  override val configuration = Configuration
  override val k8055Board = K8055Board
}

trait DeviceCollectionManager{

  val deviceConfigIO:DeviceConfigIO
  val deviceController:DeviceManager
  val monitorManager:MonitorManager
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
      case (Device.STROBE, Some(aState)) => {
        monitorManager.setAnalogueOut(device.id, aState)
      }
      case _ => false
    }
  }

  def updateTransientAnalogueOutData2(device: Device):Boolean = {
    println("########## updateTransientAnalogueOutData2 " + device)
    (device.deviceType, device.analogueState2) match{
      case (Device.STROBE, Some(aState)) => {
        monitorManager.setAnalogueOut2(device.id, aState)
      }
      case _ => false
    }
  }


  def getMonitorOrStrobeAnalogueOut(delta: Boolean, monitorOrStrobe: Device, monitorOrStrobeState: DeviceState) : Int = {
    if (delta) {
      val aRawState: Int = monitorManager.getAnalogueOut(monitorOrStrobe.id)
      aRawState + monitorOrStrobeState.analogueState.getOrElse(0)
    }
    else
      monitorOrStrobeState.analogueState.getOrElse(0)
  }

  def getMonitorOrStrobeAnalogueOut2(delta: Boolean, monitorOrStrobe: Device, monitorOrStrobeState: DeviceState) : Int = {
    if (delta) {
      val aRawState: Int = monitorManager.getAnalogueOut2(monitorOrStrobe.id)
      aRawState + monitorOrStrobeState.analogueState2.getOrElse(0)
    }
    else
      monitorOrStrobeState.analogueState2.getOrElse(0)
  }


  def patchDevice(deviceState: DeviceState, delta:Boolean):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices

    devices.find(d => d.id == deviceState.id).exists( device => {

      val MINIMUM_TIMEOUT = 1
      device.deviceType match {
        case STROBE => {

          val aState:Int = Math.max(MINIMUM_TIMEOUT, getMonitorOrStrobeAnalogueOut(delta, device, deviceState))
          val aState2:Int = Math.max(MINIMUM_TIMEOUT, getMonitorOrStrobeAnalogueOut2(delta, device, deviceState))



          deviceState.digitalState.map { enableStrobe =>
            if(enableStrobe && !strobeMessagesInQueue.contains(device.id)){

              val onSeconds = MonitorManager.getAnalogueOut(device.id)
              val tickInterval = new FiniteDuration(onSeconds, TimeUnit.SECONDS)
              system.scheduler.scheduleOnce(tickInterval, strobeActorRef, Start(device.id))

              //Add the message to the Map
              strobeMessagesInQueue += (device.id -> true)
            }
          }

          updateTransientAnalogueOutData(device.copy(analogueState = Some(aState)))
          updateTransientAnalogueOutData2(device.copy(analogueState2 = Some(aState2)))
          updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))

        }
        case MONITOR => {
          val aState:Int = getMonitorOrStrobeAnalogueOut(delta, device, deviceState)

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
