package is.solidninja
package openshift
package api
package v1

import is.solidninja.k8s.api.v1.{TopLevel => K8sTopLevel}

import io.circe._
import io.circe.generic.semiauto._

trait DecoderInstances extends is.solidninja.k8s.api.v1.DecoderInstances {

  implicit val decodeObjectReference: Decoder[ObjectReference] = deriveDecoder

  implicit val decodeDeploymentConfig: Decoder[DeploymentConfig] = deriveDecoder

  implicit val decodeDeploymentConfigList: Decoder[DeploymentConfigList] = deriveDecoder

  implicit val decodeDeploymentConfigSpec: Decoder[DeploymentConfigSpec] = deriveDecoder

  implicit val decodeDeploymentConfigStatus: Decoder[DeploymentConfigStatus] = deriveDecoder

  implicit val decodeRollingDeploymentStrategyParams: Decoder[RollingDeploymentStrategyParams] = deriveDecoder

  implicit val decodeDeploymentStrategy: Decoder[DeploymentStrategy] = deriveDecoder

  implicit val decodeDeploymentTriggerImageChangeParams: Decoder[DeploymentTriggerImageChangeParams] = deriveDecoder

  implicit val decodeDeploymentTriggerPolicy: Decoder[DeploymentTriggerPolicy] = deriveDecoder

  implicit val decodeRouteTargetReference: Decoder[RouteTargetReference] = deriveDecoder

  implicit val decodeRoutePort: Decoder[RoutePort] = deriveDecoder

  implicit val decodeRouteSpec: Decoder[RouteSpec] = deriveDecoder

  implicit val decodeRoute: Decoder[Route] = deriveDecoder

  implicit val decodeRouteList: Decoder[RouteList] = deriveDecoder

  implicit val decodeParameter: Decoder[Parameter] = deriveDecoder

  implicit val decodeTemplate: Decoder[Template] = deriveDecoder

  implicit val decodeImageStream: Decoder[ImageStream] = deriveDecoder

  implicit val decodeBuildConfig: Decoder[BuildConfig] = deriveDecoder

  implicit val decodeOapiTopLevel: Decoder[TopLevel] = for {
    kind <- Decoder[String].prepare(_.downField("kind"))
    v <- kind match {
      case "BuildConfig" => Decoder[BuildConfig]
      case "DeploymentConfig" => Decoder[DeploymentConfig]
      case "ImageStream" => Decoder[ImageStream]
      case "Route" => Decoder[Route]
      case _ => Decoder.failedWithMessage(s"Unknown oapi kind '$kind'")
    }
  } yield v

  // Thanks to https://github.com/circe/circe/issues/626

  implicit val decodeEitherTopLevel: Decoder[EitherTopLevel] =
    List[Decoder[EitherTopLevel]](
      Decoder[K8sTopLevel].map(Either.right(_)),
      Decoder[TopLevel].map(Either.left[TopLevel, K8sTopLevel](_))
    ).reduceLeft(_ or _)

  implicit val decodeTemplateList: Decoder[TemplateList] = deriveDecoder
}
