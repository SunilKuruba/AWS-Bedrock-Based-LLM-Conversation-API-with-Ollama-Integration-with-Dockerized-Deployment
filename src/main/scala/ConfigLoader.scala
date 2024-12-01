

import com.typesafe.config.{Config, ConfigFactory}

object ConfigLoader {
  val config: Config = ConfigFactory.load()
  def loadConfig(): Config = {
    config
  }

  def getConfig(key : String) : String = {
    config.getString(key)
  }
}