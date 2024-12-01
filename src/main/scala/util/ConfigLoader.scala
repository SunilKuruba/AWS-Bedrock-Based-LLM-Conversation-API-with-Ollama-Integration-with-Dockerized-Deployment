package util

import com.typesafe.config.{Config, ConfigFactory}

/**
 * A utility object to load and retrieve configuration values from application.conf.
 * This object provides methods to read values from a configuration file using the
 * Typesafe Config library.
 */
object ConfigLoader {
  val config: Config = ConfigFactory.load()

  def get(key : String) : String = {
    config.getString(key)
  }
}