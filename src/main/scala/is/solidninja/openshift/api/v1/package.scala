package is.solidninja
package openshift
package api

import cats.syntax.EitherSyntax
import is.solidninja.k8s.api.v1.JsonOps

import scala.language.implicitConversions

package object v1 extends EitherSyntax {
  type HasMetadata = is.solidninja.k8s.api.v1.HasMetadata

  type Pod = is.solidninja.k8s.api.v1.Pod
  type Service = is.solidninja.k8s.api.v1.Service

  type EitherTopLevel = Either[TopLevel, is.solidninja.k8s.api.v1.TopLevel]

  // FIXME - better way to represent this?
  implicit class EitherTopLevelOps(val topLevel: EitherTopLevel) extends AnyVal {

    def kind: String = topLevel match {
      case Left(l) => l.kind
      case Right(r) => r.kind
    }

    def name: Option[String] = topLevel match {
      case Left(l) => l.name
      case Right(r) => r.name
    }
  }

  implicit def liftToEitherTopLevel(l: TopLevel): EitherTopLevel = Left(l)
  implicit def liftToEitherTopLevel(r: is.solidninja.k8s.api.v1.TopLevel): EitherTopLevel = Right(r)

  object JsonProtocol extends EncoderInstances with DecoderInstances with JsonOps
}
