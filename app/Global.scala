import java.util.concurrent.TimeUnit
import akka.actor.{ActorRef, ActorSystem, Props}
import monitor.MonitorActor
import play.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object Global extends GlobalSettings {

  lazy val actorRef:ActorRef = system.actorOf(Props(new MonitorActor()), name = "monitorActor")

  private def startMonitors() = {
    val tickInterval  = new FiniteDuration(1, TimeUnit.SECONDS)
    val cancellable = system.scheduler.schedule(tickInterval, tickInterval, actorRef, "tick") //initialDelay, delay, Actor, Message
  }
  val system: ActorSystem = ActorSystem("K8055")
  override def onStart(app: Application) {
//    Logger.info("Application has started")
    startMonitors()
  }

  override def onStop(app: Application) {
//    Logger.info("Application shutdown...")
    actorRef ! "stop"
  }
}
