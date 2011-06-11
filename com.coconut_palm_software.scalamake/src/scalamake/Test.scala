package scalamake

import PlatformExec._
import ScalaMake._
import SPrintf._

/*
 * Scala/Eclipse blogs:
 * 
 * 1) Scala on Eclipse
 *    - Why Scala on the JVM?
 *       * Wrist-friendly, concise
 *       * Static typing catches bugs so I don't have to
 *       * Static typing will enable large-scale automatic refactoring
 *       * Tight Java / JVM integration
 *    - Scala in Eclipse
 *       * Scala plugin strengths (it generally works)
 *       * Scala plugin weaknesses (compiler out of sync; occasional crashes)
 *       * See also: screencast
 *    - Performance vs. Java
 *       * Sum of previous 2 elements benchmark
 *    - Scala lets me abstract away Java's verboseness while still using Java
 *       * String interpolation in Groovy
 *       * sprintf in C
 *       * --> Formatter#format in Java: UGH!
 *       * Scala: abstract Java's verbosity away
 *          Implicits let me add methods to your classes at *compile time*
 *          So we can add methods to java.lang.String... ;-)
 * 2) Backtick operator
 * 3) Directory listing
 * 4) ScalaMake in(dir) buildRules; dependingOn/buildScript
 */
object Test extends ScalaMakefile {
  
  new PlatformExec("cat /etc/hosts").execPrint()
  
  execute("cat /etc/passwd")

  for (line <- "ls".exec().split("\n")) {
	  println("-> " + line)
  }

 "ls".exec().split("\n").foreach { file => 
      println("=> " + file)
  }

  !"pwd"
  
  val name = "Dave"
  !("echo Hello, %s" << name)
  
  for (file <- "/home/djo".list) {
    println("==> %s" << file)
  }
  
  "/home/djo".list.foreach(println)
  
  "ALL" buildWith {
    println("Hello, world")
  }
}

