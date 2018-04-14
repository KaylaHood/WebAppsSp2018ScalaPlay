name := """play-scala-tasks"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies ++= Seq(
	guice,
	"org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
	"com.h2database" % "h2" % "1.4.196",
	"com.vmunier" %% "scalajs-scripts" % "1.1.2",
	"com.typesafe.play" %% "play-json" % "2.6.9",
	"com.typesafe.play" %% "play-slick" % "3.0.0",
	"com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
	"mysql" % "mysql-connector-java" % "6.0.6",
	"com.typesafe.slick" %% "slick-codegen" % "3.2.3"
)