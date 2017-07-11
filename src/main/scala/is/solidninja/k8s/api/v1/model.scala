package is.solidninja
package k8s
package api
package v1

import java.time.ZonedDateTime

import io.circe.Json

// FIXME: Incomplete mappings

case class IPAddress(v: String) extends AnyVal

sealed trait PortOrName
case class Port(port: Int) extends PortOrName
case class Name(name: String) extends PortOrName

case class Selector(v: Map[String, Json]) extends AnyVal

case class Timestamp(v: ZonedDateTime) extends AnyVal
case class Namespace(v: String) extends AnyVal
case class Annotations(v: Map[String, Json]) extends AnyVal

object Annotations {
  val empty: Annotations = Annotations(Map.empty)
}

case class Uid(v: String) extends AnyVal
case class Version(v: String) extends AnyVal
case class Path(v: String) extends AnyVal
case class ImageName(v: String) extends AnyVal {
  // TODO: mechanism for extracting version information?
}

case class ModeMask(v: Int) extends AnyVal

case class Capability(v: String) extends AnyVal

trait HasMetadata {
  def metadata: Option[ObjectMeta]
}

object HasMetadata {

  implicit class HasMetadataOps(val meta: HasMetadata) extends AnyVal {
    def name: Option[String] = meta.metadata.flatMap(_.name)
  }
}

sealed trait TopLevel extends HasMetadata {
  def kind: String
}

sealed trait V1Object extends TopLevel {
  val apiVersion = "v1"
}

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#pod-v1 Pod v1]]
  */
case class Pod(metadata: Option[ObjectMeta], spec: PodSpec) extends V1Object {
  val kind = "Pod"
}

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#podlist-v1 PodList v1]]
  */
case class PodList(metadata: Option[ObjectMeta], items: List[Pod]) extends V1Object {
  val kind = "PodList"
}

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#podspec-v1 PodSpec v1]]
  */
case class PodSpec(volumes: Option[List[Volume]],
                   containers: List[Container],
                   restartPolicy: Option[String] = None,
                   terminationGracePeriodSeconds: Option[Int] = None,
                   dnsPolicy: Option[String] = None,
                   securityContext: Option[PodSecurityContext] = None,
                   imagePullSecrets: Option[List[LocalObjectReference]] = None,
                   activeDeadlineSeconds: Option[Int] = None,
                   hostIPC: Option[Boolean] = None,
                   hostNetwork: Option[Boolean] = None,
                   hostPID: Option[Boolean] = None,
                   hostname: Option[String] = None,
                   nodeName: Option[String] = None,
                   nodeSelector: Option[Selector] = None,
                   serviceAccountName: Option[String] = None,
                   subdomain: Option[String] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#localobjectreference-v1 LocalObjectReference v1]]
  */
case class LocalObjectReference(name: String)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#container-v1 Container v1]]
  */
case class Container(image: ImageName,
                     imagePullPolicy: String,
                     name: Option[String] = None,
                     ports: Option[List[ContainerPort]] = None,
                     args: Option[List[String]] = None,
                     command: Option[List[String]] = None,
                     env: Option[List[EnvVar]] = None,
                     resources: Option[ResourceRequirements] = None,
                     terminationMessagePath: Option[String] = None,
                     volumeMounts: Option[List[VolumeMount]] = None,
                     securityContext: Option[SecurityContext] = None,
                     lifecycle: Option[Lifecycle] = None,
                     livenessProbe: Option[Probe] = None,
                     readinessProbe: Option[Probe] = None,
                     stdin: Option[Boolean] = None,
                     stdinOnce: Option[Boolean] = None,
                     tty: Option[Boolean] = None,
                     workingDir: Option[String] = None)

// FIXME: ImagePullPolicy not a string

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#lifecycle-v1 Lifecycle v1]]
  */
case class Lifecycle( /* FIXME */ )

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#probe-v1 Probe v1]]
  */
case class Probe(exec: Option[ExecAction] = None,
                 failureThreshold: Option[Int] = None,
                 httpGet: Option[HTTPGetAction] = None,
                 initialDelaySeconds: Option[Int] = None,
                 periodSeconds: Option[Int] = None,
                 successThreshold: Option[Int] = None,
                 timeoutSeconds: Option[Int] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#execaction-v1 ExecAction v1]]
  */
case class ExecAction(command: List[String])

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#httpgetaction-v1 HTTPGetAction v1]]
  */
case class HTTPGetAction(path: String,
                         port: PortOrName,
                         scheme: Option[String] = None,
                         host: Option[String] = None,
                         httpHeaders: Option[List[HTTPHeader]] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#httpheader-v1 HTTPHeader v1]]
  */
case class HTTPHeader(name: String, value: String)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#securitycontext-v1 SecurityContext v1]]
  */
case class SecurityContext(capabilities: Option[Capabilities] = None,
                           privileged: Option[Boolean] = None,
                           readOnlyRootFilesystem: Option[Boolean] = None,
                           runAsNonRoot: Option[Boolean] = None,
                           runAsUser: Option[Long] = None,
                           seLinuxOptions: Option[SELinuxOptions] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#capabilities-v1 Capabilities v1]]
  */
case class Capabilities(add: Option[List[Capability]], drop: Option[List[Capability]])

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#selinuxoptions-v1 SELinuxOptions v1]]
  */
case class SELinuxOptions(level: Option[String] = None,
                          role: Option[String] = None,
                          `type`: Option[String] = None,
                          user: Option[String] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#volumemount-v1 v1 VolumeMount]]
  */
case class VolumeMount(mountPath: String,
                       name: String,
                       readOnly: Option[Boolean] = None,
                       subPath: Option[String] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#podsecuritycontext-v1 v1 PodSecurityContext]]
  */
case class PodSecurityContext(fsGroup: Option[Long] = None,
                              runAsNonRoot: Option[Boolean] = None,
                              runAsUser: Option[Long] = None,
                              seLinuxOptions: Option[SELinuxOptions] = None,
                              supplementalGroups: Option[List[Long]] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#containerport-v1 v1 ContainerPort]]
  */
case class ContainerPort(containerPort: Int,
                         protocol: Option[String] = None,
                         hostIP: Option[IPAddress] = None,
                         hostPort: Option[Port] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#resourcerequirements-v1 v1 ResourceRequirements]]
  */
case class ResourceRequirements( /* FIXME */ )

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#volume-v1 Volume v1]]
  */
case class Volume(name: String,
                  persistentVolumeClaim: Option[PersistentVolumeClaimSource] = None,
                  secret: Option[SecretVolumeSource] = None)

// FIXME - volume should be ADT

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#secretvolumesource-v1 SecretVolumeSource v1]]
  */
case class SecretVolumeSource(secretName: String,
                              items: Option[List[KeyToPath]] = None,
                              defaultMode: Option[ModeMask] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#keytopath-v1 KeyToPath v1]]
  */
case class KeyToPath(key: String, path: String, mode: ModeMask)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#persistentvolumeclaimvolumesource-v1 PersistentVolumeClaimSource v1]]
  */
case class PersistentVolumeClaimSource(claimName: String)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#envvar-v1 EnvVar]]
  */
case class EnvVar(name: String, value: String)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#objectmeta-v1 ObjectMeta v1]]
  */
case class ObjectMeta(name: Option[String] = None,
                      namespace: Option[Namespace] = None,
                      labels: Option[Map[String, String]] = None,
                      annotations: Option[Annotations] = None,
                      uid: Option[Uid] = None,
                      resourceVersion: Option[Version] = None,
                      creationTimestamp: Option[Timestamp] = None,
                      selfLink: Option[Path] = None)

object ObjectMeta {
  def apply(objectName: String,
            namespace: Namespace,
            labels: Map[String, String],
            annotations: Annotations): ObjectMeta =
    ObjectMeta(Some(objectName),
               Some(namespace),
               Some(labels),
               Some(annotations),
               uid = None,
               resourceVersion = None,
               creationTimestamp = None,
               selfLink = None)
}

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#service-v1 Service v1]]
  */
case class Service(metadata: Option[ObjectMeta], spec: ServiceSpec) extends V1Object {
  val kind = "Service"
}

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#servicelist-v1 ServiceList v1]]
  */
case class ServiceList(metadata: Option[ObjectMeta], items: List[Service]) extends V1Object {
  val kind = "ServiceList"
}

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#servicespec-v1 ServiceSpec v1]]
  */
case class ServiceSpec(`type`: String,
                       clusterIP: Option[IPAddress] = None,
                       externalIPs: Option[IPAddress] = None,
                       externalName: Option[String] = None,
                       loadBalancerIP: Option[IPAddress] = None,
                       ports: Option[List[ServicePort]] = None,
                       selector: Option[Selector] = None,
                       sessionAffinity: Option[String] = None)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#serviceport-v1 ServicePort v1]]
  */
case class ServicePort(name: String,
                       port: Port,
                       protocol: String,
                       targetPort: PortOrName,
                       nodePort: Option[Int] = None)
