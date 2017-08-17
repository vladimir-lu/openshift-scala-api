package is.solidninja
package openshift
package api
package v1

import fs2.util.Attempt
import is.solidninja.k8s.api.v1.{
  Annotations,
  ImageName,
  ObjectMeta,
  PodTemplateSpec,
  ResourceRequirements,
  Seconds,
  Selector
}
import io.circe.Json

sealed trait TopLevel extends HasMetadata {
  def kind: String
}

sealed trait V1Object extends TopLevel {
  val apiVersion = "v1"
}

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-objectreference v1 ObjectReference]]
  */
case class ObjectReference(kind: Option[String],
                           namespace: Option[String],
                           name: Option[String],
                           uid: Option[String],
                           apiVersion: Option[String],
                           resourceVersion: Option[String],
                           fieldPath: Option[String])

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
                                template: Option[PodTemplateSpec],
                                selector: Option[Selector])

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentconfigstatus v1 DeploymentConfigStatus]]
  */
case class DeploymentConfigStatus(latestVersion: Option[Int], observedGeneration: Option[Int], replicas: Option[Int])

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentstrategy v1 DeploymentStrategy]]
  */
case class DeploymentStrategy(`type`: String,
                              rollingParams: Option[RollingDeploymentStrategyParams],
                              resources: Option[ResourceRequirements],
                              labels: Option[Map[String, String]],
                              annotations: Option[Annotations],
                              activeDeadlineSeconds: Option[Seconds])

// FIXME - customParams
// FIXME - recreateParams

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-rollingdeploymentstrategyparams v1 RollingDeploymentStrategyParams]]
  */
case class RollingDeploymentStrategyParams(updatePeriodSeconds: Option[Seconds],
                                           intervalSeconds: Option[Seconds],
                                           timeoutSeconds: Option[Seconds],
                                           maxUnavailable: Option[String],
                                           maxSurge: Option[String])

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymenttriggerpolicy v1 DeploymentTriggerPolicy]]
  */
case class DeploymentTriggerPolicy(`type`: String, imageChangeParams: Option[DeploymentTriggerImageChangeParams])

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymenttriggerimagechangeparams v1 DeploymentTriggerImageChangeParams]]
  */
case class DeploymentTriggerImageChangeParams(automatic: Option[Boolean],
                                              containerNames: List[String],
                                              from: ObjectReference,
                                              lastTriggeredImage: Option[ImageName])

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
case class RouteSpec(host: String, to: RouteTargetReference, port: Option[RoutePort], wildcardPolicy: Option[String])

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-routetargetreference RouteTargetReference v1]]
  */
case class RouteTargetReference(kind: String, name: String, weight: Option[Int] = None)

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-routeport v1 RoutePort]]
  */
case class RoutePort(targetPort: String)

/**
  * List of items (kind: List) - comes from expansion of templates
  */
case class TemplateList(items: List[EitherTopLevel], metadata: Option[ObjectMeta] = None) extends V1Object {
  val kind = "List"
}

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-template v1 Template]]
  */
case class Template(labels: Option[Map[String, String]],
                    message: Option[String],
                    metadata: Option[ObjectMeta],
                    objects: List[Json],
                    parameters: List[Parameter])
    extends V1Object {
  val kind = "Template"
}

object Template {
  implicit class TemplateExpansionOps(val template: Template) extends AnyVal {
    def expand(parameters: Map[String, String]): Attempt[TemplateList] =
      TemplateExpander.expandTemplate(template, parameters)
  }
}

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-parameter v1 Parameter]]
  */
case class Parameter(name: String,
                     displayName: Option[String] = None,
                     description: Option[String] = None,
                     value: Option[String] = None,
                     generate: Option[String] = None,
                     from: Option[String] = None,
                     required: Option[Boolean] = None)

/**
  * FIXME - implement
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-imagestream v1 ImageStream]]
  */
case class ImageStream(metadata: Option[ObjectMeta] = None) extends V1Object {
  val kind = "ImageStream"
}

/**
  * FIXME - implement
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-buildconfig v1 BuildConfig]]
  */
case class BuildConfig(metadata: Option[ObjectMeta] = None) extends V1Object {
  val kind = "BuildConfig"
}
