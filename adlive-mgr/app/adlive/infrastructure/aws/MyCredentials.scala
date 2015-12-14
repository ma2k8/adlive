package adlive.infrastructure.aws

import awscala.Credentials
import play.api.{Configuration, Play}

object MyCredentials extends CredentialsBase {

  def conf: Configuration = Play.current.configuration

  implicit val accessKeyId: String = conf.getString("aws.default.accessKeyId").getOrElse(throw new RuntimeException("no such aws.accessKeyId"))

  implicit val secretAccessKey: String = conf.getString("aws.default.secretAccessKey").getOrElse(throw new RuntimeException("no such aws.secretAccessKey"))

  implicit val credentials: Credentials = Credentials(accessKeyId, secretAccessKey)
}
