package com.solidninja.openshift.api

package object v1 {
  type EitherTopLevel = Either[TopLevel, com.solidninja.k8s.api.v1.TopLevel]

  type Pod = com.solidninja.k8s.api.v1.Pod
  type Service = com.solidninja.k8s.api.v1.Service
}
