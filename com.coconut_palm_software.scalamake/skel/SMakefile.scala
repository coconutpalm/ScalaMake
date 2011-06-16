import scalamake.ScalaMakefile
import scalamake.Implicits._
import scalamake.scalaMake
//import scalamake.PlatformExec._

object SMakefile extends ScalaMakefile {
  println("Hello1")

  "ALL" buildWith {
    println("Hello, world")
  }
}
SMakefile.main(argv)

