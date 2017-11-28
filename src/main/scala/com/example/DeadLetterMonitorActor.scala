package com.example

import akka.actor.{ Actor, DeadLetter }

class DeadLetterMonitorActor extends Actor {
  
  def receive = {
    case d: DeadLetter =>
      println(s"DeADleTTerMoniToR saw dead letter: $d")
  }

}
