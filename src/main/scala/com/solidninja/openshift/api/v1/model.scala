package com.solidninja.openshift.api.v1

import com.solidninja.k8s.api.v1.{ObjectMeta, PodSpec}

case class DeploymentConfigList(items: List[DeploymentConfig])

/**
  * @see [[https://docs.openshift.org/latest/rest_api/openshift_v1.html#v1-deploymentconfig v1 DeploymentConfig]]
  */
case class DeploymentConfig(spec: DeploymentConfigSpec, status: DeploymentConfigStatus, meta: Option[ObjectMeta])

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
