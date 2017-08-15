package is.solidninja
package openshift
package api
package v1

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait EncoderInstances extends is.solidninja.k8s.api.v1.EncoderInstances {

  implicit val encodeObjectRefenrece: Encoder[ObjectReference] = deriveEncoder

  implicit val encodeDeploymentConfig: Encoder[DeploymentConfig] =
    deriveEncoder[DeploymentConfig].mapJsonObject(v1Object("DeploymentConfig"))

  implicit val encodeDeploymentConfigList: Encoder[DeploymentConfigList] =
    deriveEncoder[DeploymentConfigList].mapJsonObject(v1Object("DeploymentConfigList"))

  implicit val encodeDeploymentConfigSpec: Encoder[DeploymentConfigSpec] = deriveEncoder

  implicit val encodeDeploymentConfigStatus: Encoder[DeploymentConfigStatus] = deriveEncoder

  implicit val encodeRollingDeploymentStrategyParams: Encoder[RollingDeploymentStrategyParams] = deriveEncoder

  implicit val encodeDeploymentStrategy: Encoder[DeploymentStrategy] = deriveEncoder

  implicit val encodeDeploymentTriggerImageChangeParams: Encoder[DeploymentTriggerImageChangeParams] = deriveEncoder

  implicit val encodeDeploymentTriggerPolicy: Encoder[DeploymentTriggerPolicy] = deriveEncoder

  implicit val encodePodTemplateSpec: Encoder[PodTemplateSpec] = deriveEncoder

  implicit val encodeRouteTargetReference: Encoder[RouteTargetReference] = deriveEncoder

  implicit val encodeRoutePort: Encoder[RoutePort] = deriveEncoder

  implicit val encodeRouteSpec: Encoder[RouteSpec] = deriveEncoder

  implicit val encodeRoute: Encoder[Route] = deriveEncoder[Route].mapJsonObject(v1Object("Route"))

  implicit val encodeRouteList: Encoder[RouteList] = deriveEncoder

  implicit val encodeTemplateList: Encoder[TemplateList] = deriveEncoder[TemplateList].mapJsonObject(v1Object("List"))

  implicit val encodeParameter: Encoder[Parameter] = deriveEncoder

  implicit val encodeTemplate: Encoder[Template] = deriveEncoder[Template].mapJsonObject(v1Object("Template"))

  implicit val encodeBuildConfig: Encoder[BuildConfig] =
    deriveEncoder[BuildConfig].mapJsonObject(v1Object("BuildConfig"))

  implicit val encodeImageStream: Encoder[ImageStream] =
    deriveEncoder[ImageStream].mapJsonObject(v1Object("ImageStream"))

  implicit val encodeOapiTopLevel: Encoder[TopLevel] = Encoder.instance {
    case bc: BuildConfig => bc.asJson
    case dc: DeploymentConfig => dc.asJson
    case dcl: DeploymentConfigList => dcl.asJson
    case is: ImageStream => is.asJson
    case r: Route => r.asJson
    case rl: RouteList => rl.asJson
    case tl: TemplateList => tl.asJson // FIXME: not sure if this is valid - it's not in the spec
    case t: Template => t.asJson
  }

  implicit val encodeEitherTopLevel: Encoder[EitherTopLevel] = Encoder.instance {
    case Left(topLevel) => topLevel.asJson
    case Right(k8stopLevel) => k8stopLevel.asJson
  }

}
