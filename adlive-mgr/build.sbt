import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayScala

name := """adlive-mgr"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalikejdbcSettings

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "mysql" % "mysql-connector-java" % "5.1.32",
  "org.scalikejdbc" %% "scalikejdbc"                       % "2.+",
  "org.scalikejdbc" %% "scalikejdbc-config"                % "2.+",
  "org.scalikejdbc" %% "scalikejdbc-play-plugin"           % "2.+",
  "org.scalaz" %% "scalaz-core" % "7.0.6",
  "commons-io" % "commons-io" % "2.4",
  "com.github.seratch" %% "awscala" % "0.4.+",
  "com.amazonaws" % "aws-java-sdk" % "1.10.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.7",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars" % "bootstrap" % "3.3.4",
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// flyway
Seq(flywaySettings: _*)

flywayLocations := Seq("filesystem:conf/db/migration/default")

flywayInitOnMigrate := true

flywayUrl := "jdbc:mysql://adlivedb.cec8wybfjsob.ap-northeast-1.rds.amazonaws.com/adlive_db"

flywayUser := "adlive_user"

flywayPassword := "adlive_password"

