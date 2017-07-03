package com.solidninja.openshift.api.v1

import com.solidninja.k8s.api.v1.{ObjectMeta, PodSpec}

sealed trait TopLevel

sealed trait V1Object extends TopLevel {
  val apiVersion = "v1"
}

case class DeploymentConfigList(items: List[DeploymentConfig])

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentconfig v1 DeploymentConfig]]
  */
case class DeploymentConfig(spec: DeploymentConfigSpec,
                            status: Option[DeploymentConfigStatus],
                            meta: Option[ObjectMeta])
    extends V1Object

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentconfigspec v1 DeploymentConfigSpec]]
  */
case class DeploymentConfigSpec(strategy: DeploymentStrategy,
                                triggers: List[DeploymentTriggerPolicy],
                                replicas: Int,
                                test: Boolean,
                                template: Option[PodTemplateSpec])

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentconfigstatus v1 DeploymentConfigStatus]]
  */
case class DeploymentConfigStatus(latestVersion: Int, observedGeneration: Int, replicas: Int)

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentstrategy v1 DeploymentStrategy]]
  */
case class DeploymentStrategy(`type`: String)

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymenttriggerpolicy v1 DeploymentTriggerPolicy]]
  */
case class DeploymentTriggerPolicy(`type`: String)

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-podtemplatespec v1 PodTemplateSpec]]
  */
case class PodTemplateSpec(metadata: Option[ObjectMeta], spec: PodSpec)

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-route v1 Route]]
  */
case class Route(metadata: Option[ObjectMeta], spec: RouteSpec) extends V1Object

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-routespec RouteSpec v1 ]]
  */
case class RouteSpec(host: String, to: RouteTargetReference)

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-routetargetreference RouteTargetReference v1]]
  */
case class RouteTargetReference(kind: String, name: String)
// FIXME: Implement template
//case class Template(test: Any = ???)

object TopLevel {
  type EitherTopLevel = Either[TopLevel, com.solidninja.k8s.api.v1.TopLevel]
}
