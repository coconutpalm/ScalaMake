import scalamake.ScalaMakefile
import scalamake.Implicits._
import scalamake.scalaMake
import scalamake.SPrintf._
//import scalamake.PlatformExec._

object SMakefile extends ScalaMakefile {
  val SYSTEM="My System"
  val VERSION="0.0.1-SNAPSHOT"

  println("===================================================================")
  println((" %s_%s: build starting " << (SYSTEM, VERSION)) + new java.util.Date())
  println("===================================================================")

  "ALL" buildWith {
    println("Hello, world")
  }
}

SMakefile.main(argv)

