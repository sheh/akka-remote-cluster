import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, ActorSystem, Address, Deploy, Props}
import akka.remote.RemoteScope
import akka.pattern._
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by sheh on 05/01/2017.
  */
object Cli {

  import ClusterCmds._

  def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load()
    val system = ActorSystem("cli", conf.getConfig("cluster-cli"))

    val managerUrl = conf.getString("cluster-cli.manager")
    println(s"Connecting to the cluster '$managerUrl'")

    system.actorSelection(managerUrl).resolveOne(1 seconds) onComplete {
      case Success(actorRef) =>
        cli(actorRef)
      case Failure(ex) =>
        println(s"cannot connect to the cluster manager '$managerUrl': $ex")
        system.terminate()
    }

  }

  val usage =
    """
      |cluster commands:
      |   ls      - list the cluster nodes
      |   add n - add `n` nodes to the cluster
      |   del n - delete `n` nodes from the cluster
      |   perf [pattern] - print performance of nodes which name starts with `pattern`,
      |       `perf` command without `pattern` prints performance for each node
      |   int val - set interval between massages to `val` microseconds
      |   q - quit
    """.stripMargin

  def cli(cluster: ActorRef): Unit = {
    println("connected to the cluster")
    println(usage)

    implicit val timeout = Timeout(3 seconds)
    def clusterExe(cmd: ClusterCmd) = {
      Try(Await.result(cluster ? cmd, 3 seconds)) match {
        case Success(ret) => println(ret)
        case Failure(ex) => println(ex)
      }
    }

    while (true) {
      print("cluster>> ")
      scala.io.StdIn.readLine().trim.split("\\s+") match {
        case Array("add") =>
          clusterExe(AddNodeCmd(1))
        case Array("add", n) =>
          clusterExe(AddNodeCmd(n.toInt))
        case Array("ls") =>
          clusterExe(LsNodeCmd())
        case Array("del") =>
          clusterExe(DelNodeCmd(1))
        case Array("del", n) =>
          clusterExe(DelNodeCmd(n.toInt))
        case Array("perf") =>
          clusterExe(GetPerformanceCmd(""))
        case Array("perf", p) =>
          clusterExe(GetPerformanceCmd(p))
        case Array("int", int) =>
          clusterExe(SetIntervalCmd(int.toInt))
        case Array("q") =>
          println("quit")
          System.exit(0)
        case Array("h") =>
          println(usage)
        case x =>
          println(s"Wrong command '$x'")
          println(usage)
      }
    }
  }


}
