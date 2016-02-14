package monitor

import akka.actor.{Props, ActorRef, ActorSystem}
import controllers.FakeDeviceCollectionController
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import play.api.test.WithApplication

@RunWith(classOf[JUnitRunner])
class MonitorActorSpec extends Specification{

  class TestMonitorActor extends MonitorActorTrait{
    override val deviceCollectionController = FakeDeviceCollectionController
  }


  "MonitorActor" should{
    "result in no change, when there are no active Monitors" in new WithApplication{
      val system: ActorSystem = ActorSystem("Fake")
      val actorRef:ActorRef = system.actorOf(Props(new TestMonitorActor()), name = "monitorActor")

      FakeDeviceCollectionController.heaterAnalogueState = 0
      FakeDeviceCollectionController.thermometerAnalogueState = 0
      FakeDeviceCollectionController.thermostatAnalogueState = 0
      FakeDeviceCollectionController.thermostatDigitalState = false
      actorRef ! "tick"
      Thread.sleep(2000)
      FakeDeviceCollectionController.lastDeviceState must equalTo(null)
      actorRef ! "stop"
    }


    "Not change anything, when there is one active Monitor which is on target" in new WithApplication{
      val system: ActorSystem = ActorSystem("Fake")
      val actorRef:ActorRef = system.actorOf(Props(new TestMonitorActor()), name = "monitorActor")

      FakeDeviceCollectionController.heaterAnalogueState = 0
      FakeDeviceCollectionController.thermometerAnalogueState = 2000
      FakeDeviceCollectionController.thermostatAnalogueState = 2000
      FakeDeviceCollectionController.thermostatDigitalState = true
      actorRef ! "tick"
      Thread.sleep(1000)
      FakeDeviceCollectionController.lastDeviceState.analogueState must equalTo(Some(0))
    }

    "Set Monitor increaser, when there is one active Monitor which is below target" in new WithApplication{
      val system: ActorSystem = ActorSystem("Fake")
      val actorRef:ActorRef = system.actorOf(Props(new TestMonitorActor()), name = "monitorActor")

      FakeDeviceCollectionController.heaterAnalogueState = 0
      FakeDeviceCollectionController.thermometerAnalogueState = 2000
      FakeDeviceCollectionController.thermostatAnalogueState = 2001
      FakeDeviceCollectionController.thermostatDigitalState = true
      actorRef ! "tick"
      Thread.sleep(1000)
      FakeDeviceCollectionController.lastDeviceState.analogueState must equalTo(Some(50))
    }

    "Switch off Monitor increaser, when there is an active Monitor which is above target" in new WithApplication {
      val system: ActorSystem = ActorSystem("Fake")
      val actorRef: ActorRef = system.actorOf(Props(new TestMonitorActor()), name = "monitorActor")

      FakeDeviceCollectionController.heaterAnalogueState = 0
      FakeDeviceCollectionController.thermometerAnalogueState = 2000
      FakeDeviceCollectionController.thermostatAnalogueState = 1999
      FakeDeviceCollectionController.thermostatDigitalState = true
      actorRef ! "tick"
      Thread.sleep(1000)
      FakeDeviceCollectionController.lastDeviceState.analogueState must equalTo(Some(0))
    }
  }
}
