package is
package solidninja
package scalatest

import fs2.Task

/**
  * Functions that help testing purely functional code in ScalaTest
  */
trait Fs2Spec {

  def task(t: Task[Unit]) = t.unsafeRun()

}
