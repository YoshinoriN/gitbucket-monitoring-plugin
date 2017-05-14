package gitbucket.monitoring.models

import java.nio.file.{Files, Paths}
import scala.sys.process._
import scala.xml.XML
import gitbucket.core.util.StringUtil
import gitbucket.monitoring.utils._

object LogBack {
  val enableLogging = Java.getSystemProperties.contains("logback.configurationFile")
  val confPath = Java.getSystemProperties.getOrElse("logback.configurationFile", "Can not find logback settings.xml")

  def getLogBackInfo: LogBackInfo = {
    val enableLogging = Java.getSystemProperties.contains("logback.configurationFile")
    val confPath = Java.getSystemProperties.getOrElse("logback.configurationFile", "Can not find logback settings.xml")
    val xmlFileContent: Either[String, String] = {
      if (enableLogging) {
        try {
          val bytes = Files.readAllBytes(Paths.get(confPath))
          (Right(
            StringUtil.convertFromByteArray(bytes)
          ))
        } catch {
          case e: Exception => Left("ERROR")
        }
      } else {
        Left("Dosen't configure Logback")
      }
    }
    val logFilePath: Either[String, String] = {
      if (enableLogging) {
        try {
          val xml = xmlFileContent match {
            case Left(message) => message
            case Right(s) => {
              (XML.loadString(s) \\ "appender" \ "file" toString).replace("<file>","").replace("</file>","")
            }
          }
          if (xml.trim.length == 0) {
            Left("ERROR : Not found")
          } else {
            (Right(
              xml
            ))
          }
        } catch {
          case e: Exception => Left("ERROR")
        }
      } else {
        Left("Dosen't configure Logback")
      }
    }

    LogBackInfo(
      Java.getSystemProperties.contains("logback.configurationFile"),
      Java.getSystemProperties.getOrElse("logback.configurationFile", "Can not find logback settings.xml"),
      xmlFileContent,
      logFilePath
    )
  }

  case class LogBackInfo (
    enableLogging: Boolean,
    confPath: String,
    settings: Either[String, String],
    logfilePath: Either[String, String]
  )
}

class GitBucketLog {
  val logBackInfo = LogBack.getLogBackInfo
  def getLog(path: String, lines: Int = 1000): Either[String, String] = {
    try {
      (Right(
        (Process(s"tail -n $lines $path") !!)
      ))
    } catch {
      case e: Exception => Left("ERROR")
    }
  }
}