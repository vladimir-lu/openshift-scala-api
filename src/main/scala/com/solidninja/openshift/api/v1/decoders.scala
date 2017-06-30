package com.solidninja.openshift.api.v1

import io.circe._
import io.circe.generic.semiauto._

trait DecoderInstances extends com.solidninja.k8s.api.v1.DecoderInstances {

  implicit val decodeDeploymentConfig: Decoder[DeploymentConfig] = deriveDecoder

  implicit val decodeDeploymentConfigSpec: Decoder[DeploymentConfigSpec] = deriveDecoder

  implicit val decodeDeploymentConfigStatus: Decoder[DeploymentConfigStatus] = deriveDecoder

  implicit val decodeDeploymentStrategy: Decoder[DeploymentStrategy] = deriveDecoder

  implicit val decodeDeploymentTriggerPolicy: Decoder[DeploymentTriggerPolicy] = deriveDecoder

  implicit val decodePodTemplateSpec: Decoder[PodTemplateSpec] = deriveDecoder

  implicit val decodeRouteTargetReference: Decoder[RouteTargetReference] = deriveDecoder

  implicit val decodeRouteSpec: Decoder[RouteSpec] = deriveDecoder

  implicit val decodeRoute: Decoder[Route] = deriveDecoder

  implicit val decodeTopLevel: Decoder[TopLevel] = deriveDecoder
}

object Decoders extends DecoderInstances
