package controllers

import connectors.K8055Board
import model.Device
import monitor.MonitorManager


object DeviceController extends DeviceController

trait DeviceController {
  def populateAnalogueIn (device: Device):Device = {
    device.copy(analogueState = Some(K8055Board.getAnalogueIn(device.channel)))
  }

  def populateAnalogueOut(device: Device):Device = {
    device.copy(analogueState = Some(K8055Board.getAnalogueOut(device.channel)))
  }

  def populateDigitalIn(device: Device):Device = {
    device.copy(digitalState = Some(K8055Board.getDigitalIn(device.channel)))
  }

  def populateDigitalOut(device: Device):Device = {
    device.copy(digitalState = Some(K8055Board.getDigitalOut(device.channel)))
  }

  def populateMonitor(device: Device):Device = {
    device.copy(digitalState = Some(MonitorManager.getDigitalOut(device.id)),
      analogueState = Some(MonitorManager.getAnalogueOut(device.id)))
  }
}
