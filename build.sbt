
val JvmSdkVersion = "1.0.0-M13-2015-03-20-14-55-35-fd0792c-SNAPSHOT"

libraryDependencies ++=
    "com.ning" % "async-http-client" % "1.8.7" ::
    "io.sphere.sdk.jvm" % "models" % JvmSdkVersion ::
    "io.sphere.sdk.jvm" % "java-client" % JvmSdkVersion ::
    "com.typesafe.akka" %% "akka-actor" % "2.3.9" ::
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % "2.5.1" ::
    "ch.qos.logback" % "logback-classic" % "1.1.2" ::
    "org.codehaus.woodstox" % "woodstox-core-asl" % "4.4.1" ::
    "com.novocode" % "junit-interface" % "0.10" % "test,it" ::
    "junit" % "junit-dep" % "4.11" % "test,it" ::
    "org.easytesting" % "fest-assert" % "1.4" % "test,it" ::
    "xmlunit" % "xmlunit" % "1.6" % "test,it" ::
    Nil

resolvers += Resolver.typesafeRepo("releases")
resolvers += Resolver.sonatypeRepo("snapshots")

organization := "io.sphere.sdk.jvm"

name := "sphere-lightspeed"

javacOptions ++= Seq("-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-Xlint:all", "-Xlint:-options", "-Xlint:-path", "-Werror")

javacOptions in (Compile, doc) := Seq()

bintrayPublishSettings

releaseSettings

licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))

lazy val root = Project("root", file(".")).configs(IntegrationTest).settings(Defaults.itSettings: _*)

parallelExecution in jacoco.Config := false

parallelExecution in IntegrationTest := false

jacoco.settings

itJacoco.settings