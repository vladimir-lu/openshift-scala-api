package is.solidninja
package k8s
package api
package v1

import java.time.ZonedDateTime

import io.circe.Json

// FIXME: Incomplete mappings

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
case class PodSpec(volumes: Option[List[Volume]], containers: List[Container])

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#container-v1 Container v1]]
  */
case class Container(image: ImageName,
                     imagePullPolicy: String,
                     args: Option[List[String]],
                     command: Option[List[String]],
                     env: Option[List[EnvVar]])

// FIXME: ImagePullPolicy not a string

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#volume-v1 Volume v1]]
  */
case class Volume(name: String)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#envvar-v1 EnvVar]]
  */
case class EnvVar(name: String, value: String)

/**
  * @see [[https://kubernetes.io/docs/api-reference/v1.5/#objectmeta-v1 ObjectMeta v1]]
  */
case class ObjectMeta(name: Option[String],
                      namespace: Option[Namespace],
                      labels: Option[Map[String, String]],
                      annotations: Option[Annotations],
                      uid: Option[Uid],
                      resourceVersion: Option[Version],
                      creationTimestamp: Option[Timestamp],
                      selfLink: Option[Path])

object ObjectMeta {
  def apply(name: String, namespace: Namespace, labels: Map[String, String], annotations: Annotations): ObjectMeta =
    ObjectMeta(Some(name),
               Some(namespace),
               Some(labels),
               Some(annotations),
               uid = None,
               resourceVersion = None,
               creationTimestamp = None,
               selfLink = None)

  val empty = ObjectMeta(
    name = None,
    namespace = None,
    labels = None,
    annotations = None,
    uid = None,
    resourceVersion = None,
    creationTimestamp = None,
    selfLink = None
  )
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
case class ServiceSpec()
