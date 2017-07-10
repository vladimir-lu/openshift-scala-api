package is.solidninja
package k8s
package api
package v1

import java.time.{ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

import cats.syntax.functor._

import io.circe._
import io.circe.generic.semiauto._

import scala.util.Try

private[v1] trait ValueDecoderInstances {

  implicit val decodeAnnotations: Decoder[Annotations] =
    Decoder.decodeMapLike[Map, String, Json].map(Annotations.apply)

  implicit val decodeTimestamp: Decoder[Timestamp] =
    Decoder.decodeString.emapTry(toTimestamp)

  implicit val decodeUid: Decoder[Uid] = Decoder.decodeString.map(Uid)

  implicit val decodePath: Decoder[Path] = Decoder.decodeString.map(Path)

  implicit val decodeNamespace: Decoder[Namespace] =
    Decoder.decodeString.map(Namespace)

  implicit val decodeVersion: Decoder[Version] =
    Decoder.decodeString.map(Version)

  implicit val decodeImageName: Decoder[ImageName] =
    Decoder.decodeString.map(ImageName)

  implicit val decodeIPAddress: Decoder[IPAddress] = Decoder.decodeString.map(IPAddress)

  implicit val decodePort: Decoder[Port] = Decoder.decodeInt.map(Port)

  implicit val decodeName: Decoder[Name] = Decoder.decodeString.map(Name)

  implicit val decodePortOrName: Decoder[PortOrName] = List[Decoder[PortOrName]](
    Decoder[Port].widen,
    Decoder[Name].widen
  ).reduceLeft(_ or _)

  implicit val decodeSelector: Decoder[Selector] =
    Decoder.decodeMapLike[Map, String, Json].map(Selector.apply)

  private def toTimestamp(s: String): Try[Timestamp] =
    Try(ZonedDateTime.from(DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")).parse(s)))
      .map(Timestamp)

}

trait DecoderInstances extends ValueDecoderInstances {

  implicit val decodeObjectMeta: Decoder[ObjectMeta] = deriveDecoder

  implicit val decodePod: Decoder[Pod] = deriveDecoder

  implicit val decodePodList: Decoder[PodList] = deriveDecoder

  implicit val decodeLocalObjectReference: Decoder[LocalObjectReference] = deriveDecoder

  implicit val decodePodSpec: Decoder[PodSpec] = deriveDecoder

  implicit val decodePersistentVolumeClaimSource: Decoder[PersistentVolumeClaimSource] = deriveDecoder

  implicit val decodeSecretVolumeSource: Decoder[SecretVolumeSource] = deriveDecoder

  implicit val decodeVolume: Decoder[Volume] = deriveDecoder

  implicit val decodeContainerPort: Decoder[ContainerPort] = deriveDecoder

  implicit val decodePodSecurityContext: Decoder[PodSecurityContext] = deriveDecoder

  implicit val decodeVolumeMount: Decoder[VolumeMount] = deriveDecoder

  implicit val decodeContainer: Decoder[Container] = deriveDecoder

  implicit val decodeResourceRequirements: Decoder[ResourceRequirements] = deriveDecoder

  implicit val decodeEnvVar: Decoder[EnvVar] = Decoder.instance(c =>
    for {
      name <- c.get[String]("name")
      value <- c.getOrElse[String]("value")("")
    } yield EnvVar(name, value))

  implicit val decodeServicePort: Decoder[ServicePort] = deriveDecoder

  implicit val decodeServiceSpec: Decoder[ServiceSpec] = deriveDecoder

  implicit val decodeService: Decoder[Service] = deriveDecoder

  implicit val decodeServiceList: Decoder[ServiceList] = deriveDecoder

  implicit val decodeK8sTopLevel: Decoder[TopLevel] = for {
    kind <- Decoder[String].prepare(_.downField("kind"))
    v <- kind match {
      case "Pod" => Decoder[Pod]
      case "Service" => Decoder[Service]
      case kind => Decoder.failedWithMessage(s"Unknown k8s kind '$kind'")
    }
  } yield v

}
