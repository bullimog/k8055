/**
Copyright © 2016 Graeme Bullimore

This file is part of BulliBrew.
BulliBrew is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

BulliBrew is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
  along with BulliBrew.  If not, see <http://www.gnu.org/licenses/>.
  */
package controllers

import connectors.FakeK8055Board
import manager.DeviceManager
import model.Device._
import model.Device
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication


@RunWith(classOf[JUnitRunner])
class DeviceControllerSpec extends Specification{

  object TestDeviceManager extends DeviceManager{
    override val k8055Board = FakeK8055Board
  }

  "DeviceController" should {

    "populate the analogue in for the device" in new WithApplication {
      val thermometer = Device("TEST-AI-1", "test-thermometer", ANALOGUE_IN, 1, digitalState = None)
      val changedThermometer:Device = TestDeviceManager.readAndPopulateAnalogueIn(thermometer)
      changedThermometer.analogueState must equalTo(Some(30))
    }

    "populate the analogue out for the device" in new WithApplication {
      FakeK8055Board.analogueOut1 = 2600
      val heater = Device("TEST-AO-1", "test-heater", ANALOGUE_OUT, 1, digitalState = None)
      val changedHeater:Device = TestDeviceManager.readAndPopulateAnalogueOut(heater)
      changedHeater.analogueState must equalTo(Some(26))
    }

    "populate the digital out for the device" in new WithApplication {
      FakeK8055Board.digitalOut = 3
      val pump = Device("TEST-DO-1", "test-pump", DIGITAL_OUT, channel = 1, analogueState = None, digitalState = Some(false))
      val changedPump:Device = TestDeviceManager.readAndPopulateDigitalOut(pump)
      changedPump.digitalState must equalTo(Some(true))

      FakeK8055Board.digitalOut = 6
      val pump2 = Device("TEST-DO-1", "test-pump", DIGITAL_OUT, channel = 1, analogueState = None, digitalState = Some(true))
      val changedPump2:Device = TestDeviceManager.readAndPopulateDigitalOut(pump)
      changedPump2.digitalState must equalTo(Some(false))
    }

    "populate the digital in for the device" in new WithApplication {
      val switch = Device("TEST-DI-1", "test-switch", DIGITAL_IN, 1, digitalState = Some(true))
      val changedSwitch:Device = TestDeviceManager.readAndPopulateDigitalIn(switch)
      changedSwitch.digitalState must equalTo(Some(false))
    }

  }
}
