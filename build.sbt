sbtPlugin := true

organization := "com.github.jw3"
name := "sbt-openshift"
version := "0.1-SNAPSHOT"

scalaVersion := "2.10.5"
scalacOptions in Compile ++= Seq("-deprecation", "-target:jvm-1.7")

publishMavenStyle := false

dependsOn(uri("git://github.com/sbt/sbt-native-packager#v1.2.0"))
