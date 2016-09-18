package manager

import connectors.K8055Board
import model.Device


object DeviceManager extends DeviceManager{
  override val k8055Board = K8055Board
}

trait DeviceManager {
  val k8055Board:K8055Board

  def readTimer (device: Device):Device = { device }

  def readAndPopulateDevice(device: Device):Device = {
    device.deviceType match {
      case Device.ANALOGUE_IN => readAndPopulateAnalogueIn(device)
      case Device.ANALOGUE_OUT => readAndPopulateAnalogueOut(device)
      case Device.DIGITAL_IN => readAndPopulateDigitalIn(device)
      case Device.DIGITAL_OUT => readAndPopulateDigitalOut(device)
    }
  }

  def readAndPopulateAnalogueIn (device: Device):Device = {
    device.copy(analogueState = Some(k8055Board.getAnalogueIn(device.channel)))
  }

  def readAndPopulateAnalogueOut(device: Device):Device = {
    device.copy(analogueState = Some(k8055Board.getAnalogueOut(device.channel)))
  }

  def readAndPopulateDigitalIn(device: Device):Device = {
    device.copy(digitalState = Some(k8055Board.getDigitalIn(device.channel)))
  }

  def readAndPopulateDigitalOut(device: Device):Device = {
    device.copy(digitalState = Some(k8055Board.getDigitalOut(device.channel)))
  }

  def readAndPopulateMonitor(device: Device):Device = {
    device.copy(digitalState = Some(MonitorManager.getDigitalOut(device.id)),
      analogueState = Some(MonitorManager.getAnalogueOut(device.id)))
  }
}
