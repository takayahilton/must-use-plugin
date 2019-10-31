package com.github.takayahilton.must_use

import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.{Global, Phase}

class MustUsePlugin(val global: Global) extends Plugin {
  selfPlugin =>

  import global._

  val name = "must-use"
  val description = "scala compiler plugin sample"
  val components = List[PluginComponent](Component)
  val warningsEnabled = true

  // Bind of pattern var was `x @ _`
  private def atBounded(t: Tree) = t.hasAttachment[NoWarnAttachment.type]

  // ValDef was a PatVarDef `val P(x) = ???`
  private def wasPatVarDef(t: Tree) = t.hasAttachment[PatVarDefAttachment.type]

  /** Does the positioned line assigned to t1 precede that of t2?
   */
  def posPrecedes(p1: Position, p2: Position) = p1.isDefined && p2.isDefined && p1.line < p2.line
  def linePrecedes(t1: Tree, t2: Tree) = posPrecedes(t1.pos, t2.pos)

  class MyTraverser(unit: CompilationUnit) extends Traverser {
    override def traverse(tree: Tree): Unit = super.traverse(tree)
  }

  private object Component extends PluginComponent {

    override val global: selfPlugin.global.type = selfPlugin.global
    override val runsAfter  = List("typer")
    val phaseName = selfPlugin.name

    override def newPhase(prev: Phase) = new StdPhase(prev) {
      override def name = selfPlugin.name
      override def apply(unit: CompilationUnit): Unit = if (warningsEnabled && !unit.isJava && !typer.context.reporter.hasErrors) {
        new MyTraverser(unit).traverse(unit.body)
      }
    }
  }
}
