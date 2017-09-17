package is
package solidninja
package scalatest
package tags

import org.http4s.{BasicCredentials, Uri}
import org.scalatest.Tag

object LocalCluster extends Tag("LocalCluster") {

  /**
    * Configuration of a local cluster (typically brought up with `oc cluster up`)
    *
    * @note Typically it is bad practice to hardcode configuration in code but this local cluster will always stay at
    *       this address as long as `oc` does not change the defaults
    */
  object Config {
    val uri = Uri.unsafeFromString("https://localhost:8443")
    val credentials = BasicCredentials("developer", "developer")
  }
}
