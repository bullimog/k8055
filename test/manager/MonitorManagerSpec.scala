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
package manager

import model.DeviceState
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication


@RunWith(classOf[JUnitRunner])
class MonitorManagerSpec extends Specification{

  "MonitorManager" should {

    "adds Device state for a new device and record the analogue out for the device" in new WithApplication {
      val testId = "TEST-AO-1"

      val foundMonitor1 = MonitorAndStrobeManager.monitorsAndStrobes.find(monitor => monitor.id == testId)
      foundMonitor1 must equalTo(None)

      val testDeviceState = DeviceState(testId, None, Some(11))
      MonitorAndStrobeManager.setAnalogueOut(testId, 11)
      val foundMonitor2 = MonitorAndStrobeManager.monitorsAndStrobes.find(monitor => monitor.id == testId)
      foundMonitor2 must equalTo(Some(testDeviceState))
    }


    "updates Device state for an existing device and record the analogue out" in new WithApplication {
      val testId = "TEST-AO-2"
      val testAnalogueVal1 = 11
      val testDeviceState1 = DeviceState(testId, None, Some(testAnalogueVal1))
      MonitorAndStrobeManager.setAnalogueOut(testId, testAnalogueVal1)
      val foundMonitor1 = MonitorAndStrobeManager.monitorsAndStrobes.find(monitor => monitor.id == testId)
      foundMonitor1 must equalTo(Some(testDeviceState1))
      MonitorAndStrobeManager.getAnalogueOut(testId) must equalTo(testAnalogueVal1)

      val testAnalogueVal2 = 37
      val testDeviceState2: DeviceState = DeviceState(testId, None, Some(testAnalogueVal2))
      MonitorAndStrobeManager.setAnalogueOut(testId, testAnalogueVal2)
      val foundMonitor2: Option[DeviceState] = MonitorAndStrobeManager.monitorsAndStrobes.find(monitor => monitor.id == testId)
      foundMonitor2 must equalTo(Some(testDeviceState2))
      MonitorAndStrobeManager.getAnalogueOut(testId) must equalTo(testAnalogueVal2)
    }


    "adds Device state for a new device and record the digital out for the device" in new WithApplication {
      val testId = "TEST-DO-1"

      val foundMonitor1 = MonitorAndStrobeManager.monitorsAndStrobes.find(monitor => monitor.id == testId)
      foundMonitor1 must equalTo(None)

      val testDeviceState = DeviceState(testId, Some(false), None)
      MonitorAndStrobeManager.setDigitalOut(testId, false)
      val foundMonitor2 = MonitorAndStrobeManager.monitorsAndStrobes.find(monitor => monitor.id == testId)
      foundMonitor2 must equalTo(Some(testDeviceState))
    }


    "updates Device state for an existing device and record the digital out" in new WithApplication {
      val testId = "TEST-DO-2"
      val testDigitalVal1 = false
      val testDeviceState1 = DeviceState(testId, Some(testDigitalVal1), None)
      MonitorAndStrobeManager.setDigitalOut(testId, testDigitalVal1)
      val foundMonitor1 = MonitorAndStrobeManager.monitorsAndStrobes.find(monitor => monitor.id == testId)
      foundMonitor1 must equalTo(Some(testDeviceState1))
      MonitorAndStrobeManager.getDigitalOut(testId) must equalTo(testDigitalVal1)

      val testDigitalVal2 = true
      val testDeviceState2: DeviceState = DeviceState(testId, Some(testDigitalVal2), None)
      MonitorAndStrobeManager.setDigitalOut(testId, testDigitalVal2)
      val foundMonitor2: Option[DeviceState] = MonitorAndStrobeManager.monitorsAndStrobes.find(monitor => monitor.id == testId)
      foundMonitor2 must equalTo(Some(testDeviceState2))
      MonitorAndStrobeManager.getDigitalOut(testId) must equalTo(testDigitalVal2)
    }
  }
}
