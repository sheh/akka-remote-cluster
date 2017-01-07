import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.PriorityQueue

/**
  * Created by sheh on 05/01/2017.
  */
class Application(name: String, var nodes: Set[ActorRef]) extends Actor {
  import ApplicationMsgs._

  var messageSchduler = context.system.scheduler.schedule(1 seconds, 100 milliseconds, self, 'MessageReady)

  val r = new scala.util.Random(5)

  private var perf = List.empty[Long]
  private val perfPeriod = 1000

  override def receive: Receive = {
    case 'MessageReady =>
      nodes foreach { n =>
        n ! PayloadMsg(System.currentTimeMillis())
      }
    case AddNode(actorRef) =>
      nodes += actorRef
    case DelNode(actorRef) =>
      nodes -= actorRef
    case 'GetPerformance =>
      perf = perf.dropWhile(System.currentTimeMillis() - _ > perfPeriod)
      sender() ! s"$name ${perf.size}"
    case SetInterval(int) =>
      messageSchduler.cancel()
      messageSchduler = context.system.scheduler.schedule(int milliseconds, int milliseconds, self, 'MessageReady)
      sender() ! s"$name $int"
    case PayloadMsg(ts) =>
      val ts = System.currentTimeMillis()
      perf = perf.dropWhile(ts - _ > perfPeriod) :+ ts
      //println(s"$name ${perf.size}")
  }

}

object ApplicationMsgs {
  trait ApplicationMsg
  case class SendMessageToNode(node: ActorRef) extends ApplicationMsg
  case class AddNode(node:ActorRef) extends ApplicationMsg
  case class DelNode(node:ActorRef) extends ApplicationMsg
  case class SetInterval(int: Int) extends ApplicationMsg
  case class PayloadMsg(timestamp: Long) extends ApplicationMsg
}