package controllers

import connectors.FakeK8055Board
import manager.DeviceManager
import model.Device


object FakeDeviceManager extends DeviceManager{

  override val k8055Board = FakeK8055Board

  override def readAndPopulateAnalogueIn (device: Device):Device = {
    device.copy(analogueState = Some(0))
  }

  override def readAndPopulateAnalogueOut(device: Device):Device = {
    device.copy(analogueState = Some(0))
  }

  override def readAndPopulateDigitalIn(device: Device):Device = {
    device.copy(digitalState = Some(false))
  }

  override def readAndPopulateDigitalOut(device: Device):Device = {
    device.copy(digitalState = Some(false))
  }

  override def readAndPopulateMonitor(device: Device):Device = {
    device.copy(digitalState = Some(false),
      analogueState = Some(0))
  }
}
