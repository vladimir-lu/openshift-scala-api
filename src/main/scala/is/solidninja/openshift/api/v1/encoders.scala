package is.solidninja
package openshift
package api
package v1

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

trait EncoderInstances extends is.solidninja.k8s.api.v1.EncoderInstances {
  implicit val encodeDeploymentConfig: Encoder[DeploymentConfig] =
    deriveEncoder[DeploymentConfig].mapJsonObject(v1Object("DeploymentConfig"))

  implicit val encodeDeploymentConfigList: Encoder[DeploymentConfigList] =
    deriveEncoder[DeploymentConfigList].mapJsonObject(v1Object("DeploymentConfigList"))

  implicit val encodeDeploymentConfigSpec: Encoder[DeploymentConfigSpec] = deriveEncoder

  implicit val encodeDeploymentConfigStatus: Encoder[DeploymentConfigStatus] = deriveEncoder

  implicit val encodeDeploymentStrategy: Encoder[DeploymentStrategy] = deriveEncoder

  implicit val encodeDeploymentTriggerPolicy: Encoder[DeploymentTriggerPolicy] = deriveEncoder

  implicit val encodePodTemplateSpec: Encoder[PodTemplateSpec] = deriveEncoder

  implicit val encodeRouteTargetReference: Encoder[RouteTargetReference] = deriveEncoder

  implicit val encodeRouteSpec: Encoder[RouteSpec] = deriveEncoder

  implicit val encodeRoute: Encoder[Route] = deriveEncoder[Route].mapJsonObject(v1Object("Route"))

  implicit val encodeRouteList: Encoder[RouteList] = deriveEncoder

  implicit val encodeTemplateList: Encoder[TemplateList] = deriveEncoder[TemplateList].mapJsonObject(v1Object("List"))

  implicit val encodeOapiTopLevel: Encoder[TopLevel] = Encoder.instance {
    case dc: DeploymentConfig => dc.asJson
    case dcl: DeploymentConfigList => dcl.asJson
    case r: Route => r.asJson
    case rl: RouteList => rl.asJson
    case tl: TemplateList => tl.asJson // FIXME: not sure if this is valid - it's not in the spec
  }

  implicit val encodeEitherTopLevel: Encoder[EitherTopLevel] = Encoder.instance {
    case Left(topLevel) => topLevel.asJson
    case Right(k8stopLevel) => k8stopLevel.asJson
  }

}
