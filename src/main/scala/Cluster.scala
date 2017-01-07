import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}



/**
  * Created by sheh on 05/01/2017.
  */
object Cluster extends App {

  override def main(args: Array[String]): Unit = {
    val system = ActorSystem("cluster-manager", ConfigFactory.load().getConfig("cluster-manager"))
    system.actorOf(Props[Manager], "manager")
  }

}

object ClusterCmds {
  trait ClusterCmd
  case class AddNodeCmd(n: Int) extends ClusterCmd
  case class LsNodeCmd() extends ClusterCmd
  case class DelNodeCmd(n: Int) extends ClusterCmd
  case class GetIntervalCmd() extends ClusterCmd
  case class SetIntervalCmd(interval: Int) extends ClusterCmd
  case class GetPerformanceCmd(pattern: String) extends ClusterCmd
  case class TerminateMsg() extends ClusterCmd
}