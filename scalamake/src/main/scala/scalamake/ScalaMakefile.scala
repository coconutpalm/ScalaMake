package scalamake

import Implicits._

trait ScalaMakefile {
  def main(args: Array[String]) : Unit = {
    if (args.length >= 1) {
      scalaMake(args(0))
    } else {
      scalaMake()
    }
  }
}

