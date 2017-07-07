package is.solidninja
package k8s
package api
package v1

import java.time.format.DateTimeFormatter

import io.circe._
import io.circe.generic.semiauto._

private[v1] trait ValueEncoderInstances {

  implicit val encodeAnnotations: Encoder[Annotations] =
    Encoder.encodeMapLike[Map, String, Json].contramap(_.v)

  implicit val encodeTimestamp: Encoder[Timestamp] =
    Encoder.encodeString.contramap(timestampToString)

  implicit val encodeUid: Encoder[Uid] = Encoder.encodeString.contramap(_.v)

  implicit val encodePath: Encoder[Path] = Encoder.encodeString.contramap(_.v)

  implicit val encodeNamespace: Encoder[Namespace] =
    Encoder.encodeString.contramap(_.v)

  implicit val encodeVersion: Encoder[Version] =
    Encoder.encodeString.contramap(_.v)

  implicit val encodeImageName: Encoder[ImageName] =
    Encoder.encodeString.contramap(_.v)

  private def timestampToString(ts: Timestamp): String =
    ts.v.format(DateTimeFormatter.ISO_INSTANT)

}

trait EncoderInstances extends ValueEncoderInstances {

  implicit val encodeObjectMeta: Encoder[ObjectMeta] = deriveEncoder

  implicit val encodePod: Encoder[Pod] = deriveEncoder

  implicit val encodePodList: Encoder[PodList] = deriveEncoder

  implicit val encodePodSpec: Encoder[PodSpec] = deriveEncoder

  implicit val encodeVolume: Encoder[Volume] = deriveEncoder

  implicit val encodeContainer: Encoder[Container] = deriveEncoder

  implicit val encodeEnvVar: Encoder[EnvVar] = deriveEncoder

  implicit val encodeServiceSpec: Encoder[ServiceSpec] = deriveEncoder

  implicit val encodeService: Encoder[Service] = deriveEncoder

  implicit val encodeServiceList: Encoder[ServiceList] = deriveEncoder

  implicit val encodeTopLevel: Encoder[TopLevel] = deriveEncoder

  // FIXME - figure out whether a special instance for TopLevel is needed...

}
