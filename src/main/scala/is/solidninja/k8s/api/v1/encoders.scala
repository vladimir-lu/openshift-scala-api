package is.solidninja
package k8s
package api
package v1

import java.time.format.DateTimeFormatter

import io.circe._
import io.circe.syntax._
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

  implicit val encodeIPAddress: Encoder[IPAddress] = Encoder.encodeString.contramap(_.v)

  implicit val encodePort: Encoder[Port] = Encoder.encodeInt.contramap(_.port)

  implicit val encodeName: Encoder[Name] = Encoder.encodeString.contramap(_.name)

  implicit val encodePortOrName: Encoder[PortOrName] = Encoder.instance {
    case Port(port) => Json.fromInt(port)
    case Name(name) => Json.fromString(name)
  }

  implicit val encodeSeelector: Encoder[Selector] =
    Encoder.encodeMapLike[Map, String, Json].contramap(_.v)

  private def timestampToString(ts: Timestamp): String =
    ts.v.format(DateTimeFormatter.ISO_INSTANT)

}

trait EncoderInstances extends ValueEncoderInstances {

  implicit val encodeObjectMeta: Encoder[ObjectMeta] = deriveEncoder

  implicit val encodePod: Encoder[Pod] = deriveEncoder[Pod].mapJsonObject(v1Object("Pod"))

  implicit val encodePodList: Encoder[PodList] = deriveEncoder[PodList].mapJsonObject(v1Object("PodList"))

  implicit val encodePodSpec: Encoder[PodSpec] = deriveEncoder

  implicit val encodeVolume: Encoder[Volume] = deriveEncoder

  implicit val encodeContainer: Encoder[Container] = deriveEncoder

  implicit val encodeEnvVar: Encoder[EnvVar] = deriveEncoder

  implicit val encodeServicePort: Encoder[ServicePort] = deriveEncoder

  implicit val encodeServiceSpec: Encoder[ServiceSpec] = deriveEncoder

  implicit val encodeService: Encoder[Service] = deriveEncoder[Service].mapJsonObject(v1Object("Service"))

  implicit val encodeServiceList: Encoder[ServiceList] =
    deriveEncoder[ServiceList].mapJsonObject(v1Object("ServiceList"))

  implicit val encodeTopLevel: Encoder[TopLevel] = Encoder.instance {
    case p: Pod => p.asJson
    case pl: PodList => pl.asJson
    case s: Service => s.asJson
    case sl: ServiceList => sl.asJson
  }

  // FIXME - figure out whether a special instance for TopLevel is needed...

  protected[solidninja] def v1Object(kind: String)(json: JsonObject): JsonObject =
    json
      .add("kind", Json.fromString(kind))
      .add("apiVersion", Json.fromString("v1"))
}
