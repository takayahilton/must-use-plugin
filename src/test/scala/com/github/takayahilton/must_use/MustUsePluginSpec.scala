package com.github.takayahilton.must_use

import java.net.URLClassLoader

import org.scalatest.{FunSuite, Matchers}

import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.ConsoleReporter
import scala.reflect.internal.util.{BatchSourceFile, Position}
import scala.tools.nsc.util.ClassPath


class MustUsePluginSpec extends FunSuite with Matchers {
  val code = "object Test { Option(1) match { case None => } }"

  val sources = List(new BatchSourceFile("<test>", code))

  val settings = new Settings
  val loader = getClass.getClassLoader.asInstanceOf[URLClassLoader]
  val entries = loader.getURLs map(_.getPath)
  // annoyingly, the Scala library is not in our classpath, so we have to add it manually
  val sclpath = entries find(_.endsWith("scala-compiler.jar")) map(
    _.replaceAll("scala-compiler.jar", "scala-library.jar"))
  settings.classpath.value = ClassPath.join((entries ++ sclpath) : _*)
  // save class files to a virtual directory in memory
  settings.outputDirs.setSingleOutput(new VirtualDirectory("(memory)", None))

  val reporter = new ConsoleReporter(settings) {
    override def warning(pos: Position, msg: String): Unit = {
      println("aaaaa")
      super.warning(pos, msg)
    }
  }

  val compiler = new Global(settings, reporter) {
    override protected def computeInternalPhases () {
      super.computeInternalPhases
      for (phase <- new MustUsePlugin(this).components)
        phasesSet += phase
    }
  }

  test("test") {
    new compiler.Run().compileSources(sources)
  }
}
