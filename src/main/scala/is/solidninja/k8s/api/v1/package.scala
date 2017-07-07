package is.solidninja.k8s.api

import cats.syntax.EitherSyntax

package object v1 extends EitherSyntax {
  object JsonProtocol extends EncoderInstances with DecoderInstances
}
