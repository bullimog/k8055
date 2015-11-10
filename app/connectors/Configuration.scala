package connectors

import play.api.Play

object Configuration {
  val filename = Play.current.configuration.getString("file.name").fold("devices.json") (filename => filename)
}
