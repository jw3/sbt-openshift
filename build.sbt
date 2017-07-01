sbtPlugin := true

organization := "com.github.jw3"
name := "sbt-openshift"
version := "0.2-SNAPSHOT"

scalaVersion := "2.10.5"
scalacOptions in Compile ++= Seq("-deprecation", "-target:jvm-1.7")

publishMavenStyle := false

bintrayRepository := {
  if (isSnapshot.value) "sbt-plugin-snapshots" else "sbt-plugin-releases"
}

enablePlugins(BintrayPlugin)

dependsOn(uri("git://github.com/sbt/sbt-native-packager#v1.2.0"))
