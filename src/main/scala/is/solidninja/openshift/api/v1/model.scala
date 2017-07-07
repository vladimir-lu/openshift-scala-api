package is.solidninja
package openshift
package api
package v1

import is.solidninja.k8s.api.v1.{ObjectMeta, PodSpec}

sealed trait TopLevel extends HasMetadata {
  def kind: String
}

sealed trait V1Object extends TopLevel {
  val apiVersion = "v1"
}

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentconfiglist v1 DeploymentConfigList]]
  */
case class DeploymentConfigList(metadata: Option[ObjectMeta], items: List[DeploymentConfig]) extends V1Object {
  val kind = "DeploymentConfigList"
}

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentconfig v1 DeploymentConfig]]
  */
case class DeploymentConfig(spec: DeploymentConfigSpec,
                            status: Option[DeploymentConfigStatus],
                            metadata: Option[ObjectMeta])
    extends V1Object {
  val kind = "DeploymentConfig"
}

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
case class DeploymentConfigStatus(latestVersion: Option[Int], observedGeneration: Option[Int], replicas: Option[Int])

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
case class Route(metadata: Option[ObjectMeta], spec: RouteSpec) extends V1Object {
  val kind = "Route"
}

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-routelist v1 RouteList]]
  */
case class RouteList(metadata: Option[ObjectMeta], items: List[Route]) extends V1Object {
  val kind = "RouteList"
}

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-routespec RouteSpec v1 ]]
  */
case class RouteSpec(host: String, to: RouteTargetReference)

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-routetargetreference RouteTargetReference v1]]
  */
case class RouteTargetReference(kind: String, name: String)

/**
  * List of items (kind: List) - comes from expansion of templates
  */
case class TemplateList(items: List[EitherTopLevel], metadata: Option[ObjectMeta] = None) extends V1Object {
  val kind = "List"
}

// FIXME: Implement template
//case class Template(test: Any = ???)
