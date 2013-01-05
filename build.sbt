name := "Uniscala JSON"

organization := "net.uniscala"

version := "0.3-SNAPSHOT"

scalaVersion := "2.10.0"

crossScalaVersions := "2.9.2" :: "2.10.0" :: Nil

scalacOptions <<= scalaVersion map { v: String =>
  val default = "-deprecation" :: "-unchecked" :: Nil
  if (v.startsWith("2.9.")) default else
    default ++ ("-feature" :: "-language:implicitConversions" :: Nil)
}

libraryDependencies ++= Seq("org.specs2" %% "specs2" % "1.12.3" % "test")

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
