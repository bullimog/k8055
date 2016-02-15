package controllers

import connectors.K8055Board
import connectors.{Configuration, DeviceConfigIO}
import model.Device._
import monitor.MonitorManager
import model.{DeviceCollection,Device,DeviceState}

object DeviceCollectionController extends DeviceCollectionController with DeviceController{
  override val deviceConfigIO = DeviceConfigIO
  override val deviceController = DeviceController
  override val monitorManager = MonitorManager
  override val configuration = Configuration
  override val k8055Board = K8055Board
}

trait DeviceCollectionController{

  val deviceConfigIO:DeviceConfigIO
  val deviceController:DeviceController
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
      device.deviceType match {
        case ANALOGUE_IN => deviceController.readAndPopulateAnalogueIn(device)
        case ANALOGUE_OUT => deviceController.readAndPopulateAnalogueOut(device)
        case DIGITAL_IN => deviceController.readAndPopulateDigitalIn(device)
        case DIGITAL_OUT => deviceController.readAndPopulateDigitalOut(device)
        case MONITOR => deviceController.readAndPopulateMonitor(device)
        case _ => device
      }
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
      case _ => false
    }
  }

  def updateTransientAnalogueOutData(device: Device):Boolean = {
    (device.deviceType, device.analogueState) match{
      case (Device.ANALOGUE_OUT, Some(aState)) => {
        k8055Board.setAnalogueOut(device.channel, aState)
        true
      }
      case (Device.MONITOR, Some(aState)) => {
        monitorManager.setAnalogueOut(device.id, aState)
        true
      }
      case _ => false
    }
  }



  def patchDevice(deviceState: DeviceState, delta:Boolean):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices

    devices.find(d => d.id == deviceState.id).exists( device => {

      device.deviceType match {
        case MONITOR => {
          val aState:Int = if (delta) {
            val aRawState: Int = monitorManager.getAnalogueOut(device.id)
            aRawState + deviceState.analogueState.getOrElse(0)
          }
          else
            deviceState.analogueState.getOrElse(0)

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
