package controllers

import model.Device


object FakeDeviceController extends DeviceController{

  override def populateAnalogueIn (device: Device):Device = {
    device.copy(analogueState = Some(0))
  }

  override def populateAnalogueOut(device: Device):Device = {
    device.copy(analogueState = Some(0))
  }

  override def populateDigitalIn(device: Device):Device = {
    device.copy(digitalState = Some(false))
  }

  override def populateDigitalOut(device: Device):Device = {
    device.copy(digitalState = Some(false))
  }

  override def populateMonitor(device: Device):Device = {
    device.copy(digitalState = Some(false),
      analogueState = Some(0))
  }
}
