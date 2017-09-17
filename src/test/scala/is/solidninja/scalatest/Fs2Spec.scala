package is
package solidninja
package scalatest

import cats.effect._

/**
  * Functions that help testing purely functional code in ScalaTest
  */
trait Fs2Spec {

  def io(t: IO[Unit]) = t.unsafeRunSync()

}
