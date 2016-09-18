package manager

import akka.actor.{Props, ActorRef, ActorSystem}
import controllers.{FakeDeviceManager, FakeDeviceCollectionManager}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication


@RunWith(classOf[JUnitRunner])
class MonitorActorSpec extends Specification{

  object TestMonitorActor extends MonitorActorTrait{
    override val deviceCollectionController = FakeDeviceCollectionManager
    override val deviceManager = FakeDeviceManager
  }


  "MonitorActor" should{
    "result in no change, when there are no active Monitors" in new WithApplication{
      FakeDeviceCollectionManager.heaterAnalogueState = 0
      FakeDeviceCollectionManager.thermometerAnalogueState = 0
      FakeDeviceCollectionManager.thermostatAnalogueState = 0
      FakeDeviceCollectionManager.thermostatDigitalState = false
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionManager.lastDeviceState must equalTo(null)
    }


    "Not change anything, when there is one active Monitor which is on target" in{
      FakeDeviceCollectionManager.heaterAnalogueState = 0
      FakeDeviceCollectionManager.thermometerAnalogueState = 0
      FakeDeviceCollectionManager.thermostatAnalogueState = 0
      FakeDeviceCollectionManager.thermostatDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionManager.lastDeviceState.analogueState must equalTo(Some(0))
    }

    "Set Monitor increaser, when there is one active Monitor which is below target" in{
      FakeDeviceManager.fakeAnalogueState = 190 //for sensor
      FakeDeviceCollectionManager.heaterAnalogueState = 0
//      FakeDeviceCollectionManager.thermometerAnalogueState = 190
      FakeDeviceCollectionManager.thermostatAnalogueState = 191
      FakeDeviceCollectionManager.thermostatDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionManager.lastDeviceState.analogueState must equalTo(Some(40))
    }

    "Switch off Monitor increaser, when there is an active Monitor which is above target" in {
      FakeDeviceManager.fakeAnalogueState = 190 //for sensor
      FakeDeviceCollectionManager.heaterAnalogueState = 0
//      FakeDeviceCollectionManager.thermometerAnalogueState = 190
      FakeDeviceCollectionManager.thermostatAnalogueState = 189
      FakeDeviceCollectionManager.thermostatDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionManager.lastDeviceState.analogueState must equalTo(Some(0))
    }

    "Not switch on Pump, when the monitor is disabled" in {
      FakeDeviceCollectionManager.pumpState = false
      FakeDeviceCollectionManager.switchState = true
      FakeDeviceCollectionManager.overflowMonitorDigitalState = false
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionManager.lastDeviceState.digitalState must equalTo(None)
    }

    "Switch on Pump, when the monitor is enabled and switch is on" in {
      FakeDeviceManager.fakeDigitalState = true //for sensor (switch)
      FakeDeviceCollectionManager.pumpState = false
      FakeDeviceCollectionManager.switchState = false
      FakeDeviceCollectionManager.overflowMonitorDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionManager.lastDeviceState.digitalState must equalTo(Some(true))
    }

    "Switch off Pump, when the monitor is enabled and switch is on and flipDigitalState is true" in {
      FakeDeviceManager.fakeDigitalState = true //for sensor (switch)
      FakeDeviceCollectionManager.pumpState = false
      FakeDeviceCollectionManager.switchState = false
      FakeDeviceCollectionManager.flipPumpState = true
      FakeDeviceCollectionManager.overflowMonitorDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionManager.lastDeviceState.digitalState must equalTo(Some(false))
    }

    "Switch on Pump, when the monitor is enabled and switch is off and flipDigitalState is true" in {
      FakeDeviceManager.fakeDigitalState = false //for sensor (switch)
      FakeDeviceCollectionManager.pumpState = false
      FakeDeviceCollectionManager.switchState = false
      FakeDeviceCollectionManager.flipPumpState = true
      FakeDeviceCollectionManager.overflowMonitorDigitalState = true
      TestMonitorActor.processActiveMonitors()
      FakeDeviceCollectionManager.lastDeviceState.digitalState must equalTo(Some(true))
    }

    "calculate the output setting properly when it is a lot above target " in {
      val result = TestMonitorActor.calculateOutputSetting(8)
      result must equalTo(255)
    }
    "calculate the output setting properly when it is above target " in {
      val result = TestMonitorActor.calculateOutputSetting(3)
      result must equalTo(120)
    }

    "calculate the output setting properly when there is a small drift from target " in {
      val result = TestMonitorActor.calculateOutputSetting(2)
      result must equalTo(80)
    }

    "calculate the output setting properly when there is a tiny drift from target " in {
      val result = TestMonitorActor.calculateOutputSetting(1)
      result must equalTo(40)
    }

    "calculate the output setting properly when it is on target " in {
      val result = TestMonitorActor.calculateOutputSetting(0)
      result must equalTo(0)
    }

    "calculate the output setting properly when it is below target " in {
      val result = TestMonitorActor.calculateOutputSetting(-1)
      result must equalTo(0)
    }


  }
}
