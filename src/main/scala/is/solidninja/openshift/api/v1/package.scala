package is.solidninja
package openshift
package api

package object v1 {
  type EitherTopLevel = Either[TopLevel, is.solidninja.k8s.api.v1.TopLevel]

  type HasMetadata = is.solidninja.k8s.api.v1.HasMetadata

  type Pod = is.solidninja.k8s.api.v1.Pod
  type Service = is.solidninja.k8s.api.v1.Service
}
