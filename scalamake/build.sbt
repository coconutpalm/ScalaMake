name := """scalamake"""

version := "1.0"

scalaVersion := "2.11.6"

// Change this to another test framework if you prefer
//libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "2.4.15" % "test"

    // with Scala 2.9.3 (specs2 1.12.4.1 is the latest version for scala 2.9.3)
    // "org.specs2" %% "specs2" % "1.12.4.1" % "test",
)

scalacOptions in Test ++= Seq("-Yrangepos")

// Read here for optional jars and dependencies:
// http://etorreborre.github.io/specs2/guide/org.specs2.guide.Runners.html#Dependencies

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

parallelExecution in Test := false

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.3.9"

//required for the javaOptions to be passed in
//fork := true

//javaOptions in (Test) += "-Xdebug"

//javaOptions in (Test) += "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5555"
