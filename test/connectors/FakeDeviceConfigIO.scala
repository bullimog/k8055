package connectors

import model.Device._
import model.{Device, DeviceCollection}


object FakeDeviceConfigIO extends DeviceConfigIO{

  val pump = Device("TEST-DO-1", "test-pump", DIGITAL_OUT, 1, digitalState = Some(false))
  val heater = Device("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, Some("%"), Some(0), analogueState = Some(22))
  val switch = Device("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(false))
  val thermometer = Device("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, Some("%"), Some(0), analogueState = Some(0))
  val thermostat = Device("TEST-MO-1", "Thermostat", MONITOR, 1, Some("c"), None, None, None, Some("TEST-AI-1"), Some("TEST-AO-1"), None, Some(false), None, Some(0) )
  val fakeDevices:List[Device] = List(pump, heater, switch, thermometer, thermostat)
  val deviceCollection:DeviceCollection = DeviceCollection("Fake Name", "Fake Description", fakeDevices)


  override def readDeviceCollectionFromFile(fileName:String):Option[DeviceCollection] = {
    Some(deviceCollection)
  }

  override def writeDeviceCollectionToFile(fileName: String, deviceCollection: DeviceCollection):Boolean = {
    true
  }

}
