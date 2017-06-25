package com.github.jw3.openshift

import com.typesafe.sbt.packager.Keys.{daemonGroup, executableScriptName, maintainer}
import com.typesafe.sbt.packager.docker.DockerPlugin._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.docker.{ExecCmd, _}
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{daemonUser, defaultLinuxInstallLocation}
import sbt.{AutoPlugin, Configuration, config}


object OpenShiftPlugin extends AutoPlugin {

  object autoImport extends OpenShiftKeys {
    val OpenShift: Configuration = config("openshift")
  }

  override def requires = DockerPlugin

  override def projectSettings = DockerPlugin.projectSettings ++ Seq(
    dockerBaseImage := "registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift",
    (daemonUser in Docker) := "10001",
    (daemonGroup in Docker) := "0",
    dockerCmd := Seq("bin/%s" format executableScriptName.value),
    dockerEntrypoint := Seq(),

    // todo;; k8s labels

    dockerCommands := {
      val dockerBaseDirectory = (defaultLinuxInstallLocation in Docker).value
      val user = (daemonUser in Docker).value
      val group = (daemonGroup in Docker).value

      val generalCommands = makeFrom(dockerBaseImage.value) +: makeMaintainer((maintainer in Docker).value).toSeq

      generalCommands ++
        Seq(makeUser("root"), makeWorkdir(dockerBaseDirectory),
          makeAdd(dockerBaseDirectory), makeArbitraryUserAccessible("." :: Nil)) ++
        dockerLabels.value.map(makeLabel) ++
        makeExposePorts(dockerExposedPorts.value, dockerExposedUdpPorts.value) ++
        makeVolumes(dockerExposedVolumes.value, user, group) ++
        Seq(makeUser(user)) ++
        Seq(makeEntrypoint(dockerEntrypoint.value), makeCmd(dockerCmd.value)).flatten
    }
  )

  private final def makeArbitraryUserAccessible(directories: Seq[String]): CmdLike = RunCmds(
    Cmd("RUN", Seq("chgrp", "-R", "0") ++ directories: _*),
    Cmd("RUN", Seq("chmod", "-R", "g+rwX") ++ directories: _*)
  )

  private final def makeMaintainer(maintainer: String): Option[CmdLike] =
    if (maintainer.isEmpty) None else Some(Cmd("MAINTAINER", maintainer))

  private final def makeFrom(dockerBaseImage: String): CmdLike =
    Cmd("FROM", dockerBaseImage)

  private final def makeLabel(label: Tuple2[String, String]): CmdLike = {
    val (variable, value) = label
    Cmd("LABEL", s"${variable}=${value}")
  }

  private final def makeWorkdir(dockerBaseDirectory: String): CmdLike =
    Cmd("WORKDIR", dockerBaseDirectory)

  private final def makeAdd(dockerBaseDirectory: String): CmdLike = {

    /**
     * This is the file path of the file in the Docker image, and does not depend on the OS where the image
     * is being built. This means that it needs to be the Unix file separator even when the image is built
     * on e.g. Windows systems.
     */
    val files = dockerBaseDirectory.split(UnixSeparatorChar)(1)
    Cmd("ADD", s"$files /$files")
  }

  private final def makeChown(daemonUser: String, daemonGroup: String, directories: Seq[String]): CmdLike =
    ExecCmd("RUN", Seq("chown", "-R", s"$daemonUser:$daemonGroup") ++ directories: _*)

  private final def makeUser(daemonUser: String): CmdLike =
    Cmd("USER", daemonUser)

  private final def makeEntrypoint(entrypoint: Seq[String]): Option[CmdLike] =
    if (entrypoint.isEmpty) None
    else Some(ExecCmd("ENTRYPOINT", entrypoint: _*))

  private final def makeCmd(args: Seq[String]): Option[CmdLike] =
    if (args.isEmpty) None
    else Some(ExecCmd("CMD", args: _*))

  private final def makeExposePorts(exposedPorts: Seq[Int], exposedUdpPorts: Seq[Int]): Option[CmdLike] =
    if (exposedPorts.isEmpty && exposedUdpPorts.isEmpty) None
    else
      Some(
        Cmd("EXPOSE", (exposedPorts.map(_.toString) ++ exposedUdpPorts.map(_.toString).map(_ + "/udp")) mkString " ")
      )

  private final def makeVolumes(exposedVolumes: Seq[String], daemonUser: String, daemonGroup: String): Seq[CmdLike] =
    if (exposedVolumes.isEmpty) Seq.empty
    else
      Seq(
        ExecCmd("RUN", Seq("mkdir", "-p") ++ exposedVolumes: _*),
        makeChown(daemonUser, daemonGroup, exposedVolumes),
        ExecCmd("VOLUME", exposedVolumes: _*)
      )
}
