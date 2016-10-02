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
    case Start(strobeId) => receivedAMessage(strobeId, digitalState = true, MonitorAndStrobeManager.getStrobeOnTime)
    case Stop(strobeId) => receivedAMessage(strobeId, digitalState = false, MonitorAndStrobeManager.getStrobeOffTime)
    case _ => Logger.error("unknown message received by StrobeActor")
  }
}


trait StrobeActorTrait{
  val deviceCollectionManager:DeviceCollectionManager
  val deviceManager:DeviceManager

  def receivedAMessage(strobeDeviceId: String, digitalState: Boolean, delayReadFn:(String => Int)) = {
    if (MonitorAndStrobeManager.getDigitalOut(strobeDeviceId)) {
      findAndSetDigitalOutDevice(strobeDeviceId, outputState = digitalState)

      deviceCollectionManager.getDevice(strobeDeviceId).map { strobe =>
        val duration = new FiniteDuration(delayReadFn(strobeDeviceId), TimeUnit.SECONDS)
        val message = if(digitalState) Stop(strobeDeviceId) else Start(strobeDeviceId)
        system.scheduler.scheduleOnce(duration, strobeActorRef, message) //delay, Actor, Message
      }
    } else
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
