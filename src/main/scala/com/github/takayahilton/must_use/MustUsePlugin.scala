package com.github.takayahilton.must_use

import scala.tools.nsc.{ Global, Phase }
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }

class MustUsePlugin(val global: Global) extends Plugin {
  selfPlugin =>

  import global._

  val name = "must-use"
  val description = "scala compiler plugin sample"
  val components = List[PluginComponent](Component)

  private object Component extends PluginComponent {

    override val global: selfPlugin.global.type = selfPlugin.global
    override val runsAfter  = List("parser")
    val phaseName = selfPlugin.name

    override def newPhase(prev: Phase) = new StdPhase(prev) {
      override def name = selfPlugin.name
      override def apply(unit: CompilationUnit): Unit = {
      }
    }
  }
}
