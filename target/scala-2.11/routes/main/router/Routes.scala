
// @GENERATOR:play-routes-compiler
// @SOURCE:/home/graeme/projects/k8055/conf/routes
// @DATE:Sun Nov 01 19:50:30 GMT 2015

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._

import _root_.controllers.Assets.Asset

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:6
  Application_1: controllers.Application,
  // @LINE:10
  Assets_0: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:6
    Application_1: controllers.Application,
    // @LINE:10
    Assets_0: controllers.Assets
  ) = this(errorHandler, Application_1, Assets_0, "/")

  import ReverseRouteContext.empty

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, Application_1, Assets_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.Application.allDevices"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """device/$id<[^/]+>""", """controllers.Application.getDevice(id:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/$file<.+>""", """controllers.Assets.versioned(path:String = "/public", file:Asset)"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:6
  private[this] lazy val controllers_Application_allDevices0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_Application_allDevices0_invoker = createInvoker(
    Application_1.allDevices,
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "allDevices",
      Nil,
      "GET",
      """ Home page""",
      this.prefix + """"""
    )
  )

  // @LINE:7
  private[this] lazy val controllers_Application_getDevice1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("device/"), DynamicPart("id", """[^/]+""",true)))
  )
  private[this] lazy val controllers_Application_getDevice1_invoker = createInvoker(
    Application_1.getDevice(fakeValue[String]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Application",
      "getDevice",
      Seq(classOf[String]),
      "GET",
      """""",
      this.prefix + """device/$id<[^/]+>"""
    )
  )

  // @LINE:10
  private[this] lazy val controllers_Assets_versioned2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned2_invoker = createInvoker(
    Assets_0.versioned(fakeValue[String], fakeValue[Asset]),
    HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "versioned",
      Seq(classOf[String], classOf[Asset]),
      "GET",
      """ Map static resources from the /public folder to the /assets URL path""",
      this.prefix + """assets/$file<.+>"""
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:6
    case controllers_Application_allDevices0_route(params) =>
      call { 
        controllers_Application_allDevices0_invoker.call(Application_1.allDevices)
      }
  
    // @LINE:7
    case controllers_Application_getDevice1_route(params) =>
      call(params.fromPath[String]("id", None)) { (id) =>
        controllers_Application_getDevice1_invoker.call(Application_1.getDevice(id))
      }
  
    // @LINE:10
    case controllers_Assets_versioned2_route(params) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned2_invoker.call(Assets_0.versioned(path, file))
      }
  }
}