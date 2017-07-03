package com.solidninja.k8s.api.v1

import java.time.{ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

import io.circe._
import io.circe.generic.semiauto._

import scala.util.Try

private[v1] trait ValueInstances {

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

  private def toTimestamp(s: String): Try[Timestamp] =
    Try(ZonedDateTime.from(DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC")).parse(s)))
      .map(Timestamp)

}

trait DecoderInstances extends ValueInstances {

  implicit val decodeObjectMeta: Decoder[ObjectMeta] = deriveDecoder

  implicit val decodePod: Decoder[Pod] = deriveDecoder

  implicit val decodePodList: Decoder[PodList] = deriveDecoder

  implicit val decodePodSpec: Decoder[PodSpec] = deriveDecoder

  implicit val decodeVolume: Decoder[Volume] = deriveDecoder

  implicit val decodeContainer: Decoder[Container] = deriveDecoder

  implicit val decodeEnvVar: Decoder[EnvVar] = deriveDecoder

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

object Decoders extends DecoderInstances
