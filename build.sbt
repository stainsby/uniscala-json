name := "Uniscala JSON"

organization := "net.uniscala"

version := "0.4"

description := "A compact JSON library written in Scala."

startYear := Some(2012)

homepage := Some(url("https://github.com/stainsby/uniscala-json"))

organizationName := "Sustainable Software Pty Ltd"

organizationHomepage := Some(url("http://www.sustainablesoftware.com.au/"))

licenses := Seq(
  ("The Apache Software License, Version 2.0" ->
    url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/stainsby/uniscala-json"),
    "git@github.com:stainsby/uniscala-json.git"
  )
)

pomExtra := (
  <developers>
    <developer>
      <id>stainsby</id>
      <name>Sam Stainsby</name>
      <email>sam@sustainablesoftware.com.au</email>
    </developer>
  </developers>
)

scalaVersion := "2.10.1"

scalacOptions <<= scalaVersion map { v: String =>
  val default = "-deprecation" :: "-unchecked" :: Nil
  if (v.startsWith("2.9.")) default else
    default ++ ("-feature" :: "-language:implicitConversions" :: Nil)
}

crossScalaVersions := "2.9.3" :: "2.10.1" :: Nil

libraryDependencies <+= scalaVersion {
  case "2.9.3" => "org.scalatest" % "scalatest_2.9.2" % "2.0.M6-SNAP3" % "test"
  case _       => "org.scalatest" %% "scalatest" % "2.0.M6-SNAP8" % "test"
}

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

publishMavenStyle := true

publishArtifact in Test := false

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
