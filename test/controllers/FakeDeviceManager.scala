package controllers

import connectors.FakeK8055Board
import manager.DeviceManager
import model.Device


object FakeDeviceManager extends DeviceManager{

  override val k8055Board = FakeK8055Board
  var fakeAnalogueState:Int = 0
  var fakeDigitalState:Boolean = false

  override def readAndPopulateAnalogueIn (device: Device):Device = {
    device.copy(analogueState = Some(fakeAnalogueState))
  }

  override def readAndPopulateAnalogueOut(device: Device):Device = {
    device.copy(analogueState = Some(fakeAnalogueState))
  }

  override def readAndPopulateDigitalIn(device: Device):Device = {
    device.copy(digitalState = Some(fakeDigitalState))
  }

  override def readAndPopulateDigitalOut(device: Device):Device = {
    device.copy(digitalState = Some(fakeDigitalState))
  }

  override def readAndPopulateMonitor(device: Device):Device = {
    device.copy(digitalState = Some(fakeDigitalState),
      analogueState = Some(fakeAnalogueState))
  }
}
