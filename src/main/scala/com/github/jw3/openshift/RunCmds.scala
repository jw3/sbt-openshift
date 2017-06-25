package com.github.jw3.openshift

import com.typesafe.sbt.packager.docker.{Cmd, CmdLike}

case class RunCmds(cmd: Cmd*) extends CmdLike {
  def makeContent = if (cmd.isEmpty) "" else
    cmd.drop(1).foldLeft("RUN %s" format cmd.head.args.mkString(" ")) { (s, c) ⇒
      "%s \\\n && %s" format(s, c.args.mkString(" "))
    } + "\n"
}
