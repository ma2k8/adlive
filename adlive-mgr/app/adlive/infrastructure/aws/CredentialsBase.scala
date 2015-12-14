package adlive.infrastructure.aws

import awscala.Credentials

/**
 * Credentialsのベース
 */
abstract class CredentialsBase {
  implicit val accessKeyId: String

  implicit val secretAccessKey: String

  implicit val credentials: Credentials
}
