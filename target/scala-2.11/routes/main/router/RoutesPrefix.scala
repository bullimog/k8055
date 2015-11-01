
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/graeme/projects/k8055/conf/routes
// @DATE:Sun Nov 01 19:50:30 GMT 2015


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
