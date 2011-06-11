package scalamake

import ScalaMake._

trait ScalaMakefile {
  def main(args: Array[String]) = {
    if (args.length >= 1) {
      scalaMake(args(0))
    } else {
      scalaMake()
    }
  }
}

