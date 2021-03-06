package controllers

import manager.DeviceCollectionManager
import model.Device._
import model.{DeviceState, Device, DeviceCollection}

object FakeDeviceCollectionManager extends DeviceCollectionManager{
  override val deviceConfigIO = null
  override val deviceController = null
  override val monitorManager = null
  override val configuration = null
  override val k8055Board = null

  var thermostatDigitalState = false
  var thermostatAnalogueState:Int = 0
  var heaterAnalogueState:Int = 0
  var coolerAnalogueState:Int = 0
  var thermometerAnalogueState:Int = 0

  var overflowMonitorDigitalState = false
  var switchState:Boolean = false
  var pumpState:Boolean = false
  var flipPumpState = false

  var lastDeviceState: DeviceState = _

  override def getDeviceCollection:DeviceCollection ={
    val pump = Device("TEST-DO-1", "test-pump", DIGITAL_OUT, 1, digitalState = Some(pumpState))
    val heater = Device("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0),
      analogueState = Some(heaterAnalogueState))
    val switch = Device("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(switchState))
    val thermometer = Device("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0),
      analogueState = Some(thermometerAnalogueState))
    val thermostat = Device("TEST-MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"),
      Some("TEST-AO-1"), Some("TEST-AO-2"), Some(thermostatDigitalState), None, Some(thermostatAnalogueState) )
    val overflowMonitor = Device("TEST-MO-2", "Overflow Monitor", MONITOR, 1, None, None, None, None, Some("TEST-DI-1"),
      Some("TEST-DO-1"), Some("TEST-DO-1"), Some(overflowMonitorDigitalState), Some(flipPumpState), None )
    val fakeDevices:List[Device] = List(pump, heater, switch, thermometer, thermostat, overflowMonitor)
    DeviceCollection("Fake Name", "Fake Description", fakeDevices)
  }

  override def readAndPopulateDevices(deviceCollection: DeviceCollection):DeviceCollection ={
    getDeviceCollection
  }

  override def patchDevice(deviceState: DeviceState, delta:Boolean):Boolean = {
    lastDeviceState = deviceState
    true
  }
}
