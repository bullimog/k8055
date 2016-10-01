package manager

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import model.{Device, DeviceState}
import play.api.Logger
import ActorGlobals._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration.FiniteDuration

class StrobeActor extends StrobeActorTrait with Actor{
  override val deviceCollectionManager = DeviceCollectionManager
  override val deviceManager = DeviceManager

  def receive = {
    case Start(a) => receivedAStart(a)
    case Stop(a) => receivedAStop(a)
    case message => Logger.error("unknown message in StrobeActor" + message)
  }
}


trait StrobeActorTrait{
  val deviceCollectionManager:DeviceCollectionManager
  val deviceManager:DeviceManager

  def receivedAStart(strobeDeviceId: String) = {
    println("##received a  start")
    //println("##device is " +MonitorManager.getDigitalOut(strobeDeviceId))

    if(MonitorManager.getDigitalOut(strobeDeviceId)) {
      findAndSetDigitalOutDevice(strobeDeviceId, outputState = true)

      deviceCollectionManager.getDevice(strobeDeviceId).map { strobe =>
        val onSeconds = MonitorManager.getAnalogueOut(strobeDeviceId)
        val duration = new FiniteDuration(onSeconds, TimeUnit.SECONDS)
        system.scheduler.scheduleOnce(duration, strobeActorRef, Stop(strobeDeviceId)) //delay, Actor, Message
      }
    }else
      strobeMessagesInQueue -= strobeDeviceId
  }

  def receivedAStop(strobeDeviceId: String) = {
    println("##received a  stop")

    if(MonitorManager.getDigitalOut(strobeDeviceId)) {
      findAndSetDigitalOutDevice(strobeDeviceId, outputState = false)

      deviceCollectionManager.getDevice(strobeDeviceId).map { strobe =>
        val offSeconds = MonitorManager.getAnalogueOut2(strobeDeviceId)
        val duration = new FiniteDuration(offSeconds, TimeUnit.SECONDS)
        system.scheduler.scheduleOnce(duration, strobeActorRef, Start(strobeDeviceId)) //delay, Actor, Message
      }
    }else
      strobeMessagesInQueue -= strobeDeviceId
  }


  def findAndSetDigitalOutDevice(strobeDeviceId: String, outputState: Boolean) = {
    deviceCollectionManager.getDevice(strobeDeviceId).map{ strobeDevice =>
      strobeDevice.monitorIncreaser.map { increaserId =>
        deviceCollectionManager.getDevice(increaserId).map { increaser =>
          updateDigitalOutputDevice(increaser, outputState)
        }
      }
    }
  }


  private[manager] def updateDigitalOutputDevice(outputDevice:Device, outputState:Boolean) = {
    val outputDeviceState = DeviceState(outputDevice.id, Some(outputState), None)
    deviceCollectionManager.patchDevice(outputDeviceState, delta = false)
  }
}
