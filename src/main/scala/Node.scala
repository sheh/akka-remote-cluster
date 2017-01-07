import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.{ConfigFactory, ConfigValue, ConfigValueFactory}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by sheh on 06/01/2017.
  */
class Node(name: String, nodes: Set[ActorRef]) {
  import ApplicationMsgs._
  private val config = ConfigFactory.load().getConfig("cluster-node")
  private val system = ActorSystem(name, config)
  private val appActor = system.actorOf(Props(classOf[Application], name, nodes))
  implicit private val timeout = Timeout(1 seconds)

  def actorRef = appActor.actorRef

  nodes foreach { _ ! AddNode(actorRef) }

  def performance = {
    appActor ? 'GetPerformance
  }

  def setInterval(interval: Int) = {
    appActor ? SetInterval(interval)
  }

  def stop() = {
    system.stop(appActor)
    system.terminate()
  }

}
