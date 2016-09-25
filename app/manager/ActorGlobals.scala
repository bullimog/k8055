package manager

import akka.actor.{ActorRef, ActorSystem, Props}

object ActorGlobals {
  val system: ActorSystem = ActorSystem("K8055")

  lazy val monitorActorRef:ActorRef = system.actorOf(Props(new MonitorActor()), name = "monitorActor")

  lazy val strobeActorRef:ActorRef = system.actorOf(Props(new StrobeActor()), name = "strobeActor")
}
