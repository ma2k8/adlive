// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.2")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.1.0")

// MySQL Driver(データモデルgenerateに必要)
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.32"

// scalikejdbc generator
addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.1.0")

// flyway plugin

addSbtPlugin("org.flywaydb" % "flyway-sbt" % "3.0")

resolvers += "Flyway" at "http://flywaydb.org/repo"
