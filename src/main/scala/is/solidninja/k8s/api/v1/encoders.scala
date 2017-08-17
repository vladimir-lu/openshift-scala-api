package is.solidninja
package k8s
package api
package v1

import java.time.format.DateTimeFormatter

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._

private[v1] trait ValueEncoderInstances {

  implicit val encodeAnnotations: Encoder[Annotations] =
    Encoder.encodeMapLike[Map, String, Json].contramap(_.v)

  implicit val encodeTimestamp: Encoder[Timestamp] =
    Encoder.encodeString.contramap(timestampToString)

  implicit val encodeUid: Encoder[Uid] = Encoder.encodeString.contramap(_.v)

  implicit val encodePath: Encoder[Path] = Encoder.encodeString.contramap(_.v)

  implicit val encodeNamespace: Encoder[Namespace] =
    Encoder.encodeString.contramap(_.v)

  implicit val encodeVersion: Encoder[Version] =
    Encoder.encodeString.contramap(_.v)

  implicit val encodeImageName: Encoder[ImageName] =
    Encoder.encodeString.contramap(_.v)

  implicit val encodeIPAddress: Encoder[IPAddress] = Encoder.encodeString.contramap(_.v)

  implicit val encodePort: Encoder[Port] = Encoder.encodeInt.contramap(_.port)

  implicit val encodeName: Encoder[Name] = Encoder.encodeString.contramap(_.name)

  implicit val encodePortOrName: Encoder[PortOrName] = Encoder.instance {
    case Port(port) => Json.fromInt(port)
    case Name(name) => Json.fromString(name)
  }

  implicit val encodeSelector: Encoder[Selector] =
    Encoder.encodeMapLike[Map, String, Json].contramap(_.v)

  implicit val encodeModeMask: Encoder[ModeMask] =
    Encoder.encodeInt.contramap(_.v)

  implicit val encodeCapability: Encoder[Capability] =
    Encoder.encodeString.contramap(_.v)

  implicit val encodeSeconds: Encoder[Seconds] =
    Encoder.encodeLong.contramap(_.v.toSeconds)

  private def timestampToString(ts: Timestamp): String =
    ts.v.format(DateTimeFormatter.ISO_INSTANT)

}

trait EncoderInstances extends ValueEncoderInstances {

  implicit val encodeObjectMeta: Encoder[ObjectMeta] = deriveEncoder

  implicit val encodePod: Encoder[Pod] = deriveEncoder[Pod].mapJsonObject(v1Object("Pod"))

  implicit val encodePodList: Encoder[PodList] = deriveEncoder[PodList].mapJsonObject(v1Object("PodList"))

  implicit val encodePersistentVolumeClaimSource: Encoder[PersistentVolumeClaimSource] = deriveEncoder

  implicit val encodeSecretVolumeSource: Encoder[SecretVolumeSource] = deriveEncoder

  implicit val encodeVolumeMount: Encoder[VolumeMount] = deriveEncoder

  implicit val encodeLocalObjectReference: Encoder[LocalObjectReference] = deriveEncoder

  implicit val encodeKeyToPath: Encoder[KeyToPath] = deriveEncoder

  implicit val encodePodSpec: Encoder[PodSpec] = deriveEncoder

  implicit val encodePodTemplateSpec: Encoder[PodTemplateSpec] = deriveEncoder

  implicit val encodeVolume: Encoder[Volume] = deriveEncoder

  implicit val encodeContainerPort: Encoder[ContainerPort] = deriveEncoder

  implicit val encodeCpuMemory: Encoder[CpuMemory] = deriveEncoder

  implicit val encodeResourceRequirements: Encoder[ResourceRequirements] = deriveEncoder

  implicit val encodePodSecurityContext: Encoder[PodSecurityContext] = deriveEncoder

  implicit val encodeLifecycle: Encoder[Lifecycle] = deriveEncoder

  implicit val encodeProbe: Encoder[Probe] = deriveEncoder

  implicit val encodeExecAction: Encoder[ExecAction] = deriveEncoder

  implicit val encodeHttpGetAction: Encoder[HTTPGetAction] = deriveEncoder

  implicit val encodeHttpHeader: Encoder[HTTPHeader] = deriveEncoder

  implicit val encodeSecurityContext: Encoder[SecurityContext] = deriveEncoder

  implicit val encodeCapabilities: Encoder[Capabilities] = deriveEncoder

  implicit val encodeSELinuxOptions: Encoder[SELinuxOptions] = deriveEncoder

  implicit val encodeContainer: Encoder[Container] = deriveEncoder

  implicit val encodeEnvVar: Encoder[EnvVar] = deriveEncoder

  implicit val encodeServicePort: Encoder[ServicePort] = deriveEncoder

  implicit val encodeServiceSpec: Encoder[ServiceSpec] = deriveEncoder

  implicit val encodeService: Encoder[Service] = deriveEncoder[Service].mapJsonObject(v1Object("Service"))

  implicit val encodeServiceList: Encoder[ServiceList] =
    deriveEncoder[ServiceList].mapJsonObject(v1Object("ServiceList"))

  implicit val encodeReplicationController: Encoder[ReplicationController] =
    deriveEncoder[ReplicationController].mapJsonObject(v1Object("ReplicationController"))

  implicit val encodeReplicationControllerList: Encoder[ReplicationControllerList] =
    deriveEncoder[ReplicationControllerList].mapJsonObject(v1Object("ReplicationControllerList"))

  implicit val encodeReplicationControllerStatus: Encoder[ReplicationControllerStatus] = deriveEncoder

  implicit val encodeReplicationControllerSpec: Encoder[ReplicationControllerSpec] = deriveEncoder

  implicit val encodeReplicationControllerCondition: Encoder[ReplicationControllerCondition] = deriveEncoder

  implicit val encodeTopLevel: Encoder[TopLevel] = Encoder.instance {
    case p: Pod => p.asJson
    case pl: PodList => pl.asJson
    case s: Service => s.asJson
    case sl: ServiceList => sl.asJson
    case rc: ReplicationController => rc.asJson
    case rl: ReplicationControllerList => rl.asJson
  }

  protected[solidninja] def v1Object(kind: String)(json: JsonObject): JsonObject =
    json
      .add("kind", Json.fromString(kind))
      .add("apiVersion", Json.fromString("v1"))
}

/**
  * Operations to ease (mainly) working with the high optionality of the Kubernetes and Openshift Objects
  */
trait JsonOps {

  implicit class JsonWithoutNulls(val j: Json) {

    /**
      * Remove all null object values from the json recursively (traversing inside arrays and objects)
      */
    def withoutNulls: Json =
      j.asObject
        .map(JsonWithoutNulls.removeNullFields)
        .orElse(j.asArray.map(JsonWithoutNulls.removeNullFields))
        .getOrElse(j)
  }

  private object JsonWithoutNulls {
    def removeNullFields(o: JsonObject): Json =
      Json.fromJsonObject(
        o.fields.foldLeft(o) {
          case (obj, field) if obj(field).exists(_.isNull) => obj.remove(field)
          case (obj, field) if obj(field).exists(o => o.isObject || o.isArray) =>
            obj.remove(field).add(field, obj(field).map(_.withoutNulls).get)
          case (x, _) => x
        }
      )

    def removeNullFields(j: Vector[Json]): Json = Json.fromValues(j.map(_.withoutNulls))
  }
}
