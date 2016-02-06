package model

import connector.K8055Board
import connectors.{Configuration, DeviceConfigIO}
import model.Device._
import monitor.MonitorManager
import play.api.Play
import play.api.libs.json.{Json, JsPath, Reads}
import play.api.libs.functional.syntax._

import scala.concurrent.Future


case class DeviceCollection(name: String, description: String, devices: List[Device])

object DeviceCollection{
  implicit val deviceCollectionReads: Reads[DeviceCollection] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "devices").read[List[Device]]
    )(DeviceCollection.apply _)

  implicit val deviceCollectionWrites = Json.writes[DeviceCollection]

  def getDeviceCollection:DeviceCollection = {
    val oDeviceCollection: Option[DeviceCollection] = DeviceConfigIO.readDeviceCollectionFromFile(Configuration.filename)
    oDeviceCollection.fold(DeviceCollection("No Devices", "Error", List()))({
      deviceCollection => deviceCollection
    })
  }

  def populateDevices(deviceCollection: DeviceCollection):DeviceCollection = {
    val populatedDevices = deviceCollection.devices.map(device =>
      device.deviceType match {
        case ANALOGUE_IN => Device.populateAnalogueIn(device)
        case ANALOGUE_OUT => Device.populateAnalogueOut(device)
        case DIGITAL_IN => Device.populateDigitalIn(device)
        case DIGITAL_OUT => Device.populateDigitalOut(device)
        case MONITOR => Device.populateMonitor(device)
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


  //TOO much repetition here... need refactoring....
  def updateTransientDigitalOutData(device: Device):Boolean = {
    println("updateTransientDigitalOutData "+ device)

    (device.deviceType, device.digitalState) match{
      case (Device.DIGITAL_OUT, Some(dState)) => {
        K8055Board.setDigitalOut(device.channel, dState)
        true
      }
      case (Device.MONITOR, Some(dState)) => {
        MonitorManager.setDigitalOut(device.id, dState)
        true
      }
      case _ => false
    }

//    if ((device.deviceType == Device.DIGITAL_OUT) && device.digitalState.isDefined) {
//      K8055Board.setDigitalOut(device.channel, device.digitalState.getOrElse(false))
//      true
//    }else false
  }

  def updateTransientAnalogueOutData(device: Device):Boolean = {
    println("updateTransientAnalogueOutData "+ device)

    (device.deviceType, device.analogueState) match{
      case (Device.ANALOGUE_OUT, Some(aState)) => {
        K8055Board.setAnalogueOut(device.channel, aState)
        true
      }
      case (Device.MONITOR, Some(dState)) => {
        MonitorManager.setAnalogueOut(device.id, dState)
        true
      }
      case _ => false
    }
//    if(device.deviceType == Device.ANALOGUE_OUT && device.analogueState.isDefined){
//      K8055Board.setAnalogueOut(device.channel, device.analogueState.getOrElse(0))
//      true
//    }else false
  }



  def patchDevice(deviceState: DeviceState, delta:Boolean):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices

    devices.find(d => d.id == deviceState.id).exists( device => {

      device.deviceType match {
        case MONITOR => {
          val aRawState = MonitorManager.getAnalogueOut(device.id)
          val aState = if (delta)
            aRawState + deviceState.analogueState.getOrElse(0)
          else
            deviceState.analogueState.getOrElse(0)

          updateTransientAnalogueOutData(device.copy(analogueState = Some(aState)))
          updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
        }
        case ANALOGUE_OUT => {
          val aRawState = K8055Board.getAnalogueOut(device.channel)
          val aState = if (delta)
            aRawState + deviceState.analogueState.getOrElse(0)
          else
            deviceState.analogueState.getOrElse(0)

          updateTransientAnalogueOutData(device.copy(analogueState = Some(aState)))
        }
        case DIGITAL_OUT => updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
        case _ => false
      }
    })
  }

//      val aRawState =
//      if(device.deviceType==MONITOR)
//        MonitorManager.getAnalogueOut(device.id)
//      else
//        K8055Board.getAnalogueOut(device.channel)
//
//
//      val aState:Int =
//      if(delta)
//        aRawState + deviceState.analogueState.getOrElse(0)
//      else
//        deviceState.analogueState.getOrElse(0)
//
//      if(device.deviceType==DIGITAL_OUT)
//        updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
//      else
//        updateTransientAnalogueOutData(device.copy(analogueState = Some(aState)))
//    })



//    devices.find(d => d.id == deviceState.id).exists(device =>
//      device.deviceType match {
//        case Device.ANALOGUE_OUT =>
//          val aState:Int = if(delta) {K8055Board.getAnalogueOut(device.channel) + deviceState.analogueState.getOrElse(-123)}
//                       else deviceState.analogueState.getOrElse(0)
//          updateTransientDigitalOutData(device.copy(analogueState = Some(aState)))
//          true
//        case Device.DIGITAL_OUT =>
//          updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
//          true
//        case Device.MONITOR =>
//
//          updateTransientDigitalOutData(device.copy(digitalState = deviceState.digitalState))
//          true
//        case _ => false
//      }
//    )
//  }

//  def addDevice(device: Device):Boolean = {
//    val deviceCollection = getDeviceCollection
//    val devices:List[Device] = deviceCollection.devices
//    if(devices.exists(d => d.id == device.id)) false
//    else {
//      val deviceAdded = devices ::: List(device)
//      val dc = deviceCollection.copy(devices = deviceAdded)
//      putDeviceCollection(dc)
//    }
//  }

  def deleteDevice(device: Device):Boolean = {deleteDevice(device.id)}
  def deleteDevice(device: String):Boolean = {
    val deviceCollection = getDeviceCollection
    val devices:List[Device] = deviceCollection.devices
    val deviceRemoved = devices.filter(d => d.id != device)
    val dc = deviceCollection.copy(devices = deviceRemoved)
    putDeviceCollection(dc)
  }

  def putDeviceCollection(deviceCollection: DeviceCollection):Boolean = {
    DeviceConfigIO.writeDeviceCollectionToFile(Configuration.filename, deviceCollection)
  }

}
