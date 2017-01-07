import akka.actor.{Actor, PoisonPill}

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by sheh on 07/01/2017.
  */
class Manager extends Actor {
  import ClusterCmds._
  private var nodes = Map.empty[String, Node]
  private val randomName = new scala.util.Random
  private var interval = 100

  override def receive: Receive = {

    case AddNodeCmd(n) =>
      val names = 1 to n map { _ =>
        val name = randomName.alphanumeric.take(6).mkString
        nodes += name -> new Node(name, interval, nodes.values.map(_.actorRef).toSet)
        name
      }
      sender() ! names.mkString("\n")

    case LsNodeCmd() =>

      sender() ! nodes.keys.mkString("\n") + s"\n${nodes.size} nodes "

    case DelNodeCmd(n) =>
      val manager = sender()
      val stopFutures = 1 to math.min(nodes.size, n) map { _ =>
        val (name, node) = nodes.head
        nodes -= name
        node.stop().map(_ => name)
      }
      Future.sequence(stopFutures) onComplete {
        case Success(names) => manager ! names.mkString("\n")
        case Failure(ex) => manager ! ex.toString
      }

    case SetIntervalCmd(newInterval) =>
      interval = newInterval
      val manager = sender()
      Future.sequence { nodes.values map { _.setInterval(interval) } } onComplete {
        case Success(rets) => manager ! rets.mkString("\n")
        case Failure(ex) => manager ! ex.toString
      }

    case GetPerformanceCmd(pattern) =>
      val manager = sender()
      nodes.filterKeys(_.startsWith(pattern)).toList match {
        case Nil => manager ! s"node '$pattern' not found"
        case fNodes =>
          Future.sequence(fNodes map { _._2.performance }) onComplete {
            case Success(perfs) =>
              val sumPerf = if (perfs.size > 1) {
                val s = perfs.map(_.toString.split("\\s+").last.toDouble).sum
                s"total perf $s"
              } else {
                ""
              }
              manager ! perfs.mkString("\n") + "\n" + sumPerf
            case Failure(ex) => manager ! ex.toString
          }
      }

    case TerminateMsg() =>
      Future.sequence { nodes.values map { _.stop() } } onComplete {
        case Success(_) =>
          sender() ! "cluster terminated"
          self ! PoisonPill
        case Failure(ex) =>
          sender() ! ex.toString
      }

  }

  override def postStop(): Unit = {
    context.system.terminate()
  }
}