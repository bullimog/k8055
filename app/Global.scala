import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import manager.{ActorGlobals, MonitorActor, StrobeActor}
import play.api._
import manager.ActorGlobals._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object Global extends GlobalSettings {
//  val system: ActorSystem = ActorSystem("K8055")
//
//  lazy val monitorActorRef:ActorRef = system.actorOf(Props(new MonitorActor()), name = "monitorActor")

  private def startMonitors() = {
    val tickInterval  = new FiniteDuration(1, TimeUnit.SECONDS)
    val cancellable = system.scheduler.schedule(tickInterval, tickInterval, monitorActorRef, "tick") //initialDelay, delay, Actor, Message
  }



  override def onStart(app: Application) {
//    Logger.info("Application has started")
    startMonitors()
  }

  override def onStop(app: Application) {
//    Logger.info("Application shutdown...")
    monitorActorRef ! "stop"
  }
}
