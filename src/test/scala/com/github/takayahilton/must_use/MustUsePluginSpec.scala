package com.github.takayahilton.must_use

import java.net.URLClassLoader

import org.scalatest.{FunSuite, Matchers, OptionValues}

import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.ConsoleReporter
import scala.reflect.internal.util.{BatchSourceFile, Position}
import scala.tools.nsc.util.ClassPath

class MustUsePluginSpec extends FunSuite with Matchers with OptionValues {
  test("test") {
    val code =
      """object Test {
        |  Right(1)
        |  Option(1) match {
        |    case None =>
        |  }
        |}""".stripMargin
    Compiler.getWarningMsg(code).value should startWith("match may not be exhaustive.")
  }
}

object Compiler {
  private[this] var warningMsg: String = _
  private val settings = new Settings
  private val loader = getClass.getClassLoader.asInstanceOf[URLClassLoader]
  private val entries = loader.getURLs map(_.getPath)
  // annoyingly, the Scala library is not in our classpath, so we have to add it manually
  private val sclpath = entries find(_.endsWith("scala-compiler.jar")) map(
    _.replaceAll("scala-compiler.jar", "scala-library.jar"))
  settings.classpath.value = ClassPath.join(entries ++ sclpath : _*)
  // save class files to a virtual directory in memory
  settings.outputDirs.setSingleOutput(new VirtualDirectory("(memory)", None))

  private val reporter = new ConsoleReporter(settings) {
    override def warning(pos: Position, msg: String): Unit = {
      warningMsg = msg
    }
  }

  private val global = new Global(settings, reporter) {
    override protected def computeInternalPhases () {
      super.computeInternalPhases
      for (phase <- new MustUsePlugin(this).components)
        phasesSet += phase
    }
  }

  private val compiler = new global.Run()

  def getWarningMsg(code: String): Option[String] = {
    warningMsg = null
    val sources = List(new BatchSourceFile("<test>", code))
    compiler.compileSources(sources)
    Option(warningMsg)
  }
}
