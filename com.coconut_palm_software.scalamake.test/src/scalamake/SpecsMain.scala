package scalamake

import scalamake._
import org.specs.runner.ConsoleRunner

object SpecsMain {
  def main(args : Array[String]) : Unit = {
    val specifications = List(PlatformExecSpec, ScalaMakeSpec)
    new ConsoleRunner().report(specifications)
  }
}
