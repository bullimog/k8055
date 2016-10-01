package manager

import akka.actor.{ActorRef, ActorSystem, Cancellable, Props}

import scala.collection.mutable

object ActorGlobals {
  val system: ActorSystem = ActorSystem("K8055")

  lazy val monitorActorRef:ActorRef = system.actorOf(Props(new MonitorActor()), name = "monitorActor")

  lazy val strobeActorRef:ActorRef = system.actorOf(Props(new StrobeActor()), name = "strobeActor")

  var strobeMessagesInQueue:scala.collection.mutable.Map[String, String] = mutable.Map()
}


case class Start(strobeId:String)
case class Stop(strobeId:String)

