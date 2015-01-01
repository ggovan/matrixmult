scalaVersion := "2.11.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"

libraryDependencies += "colt" % "colt" % "1.2.0"

libraryDependencies += "net.sourceforge.parallelcolt" % "parallelcolt" % "0.10.0"

