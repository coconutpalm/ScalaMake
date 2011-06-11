package scalamake

class Formatter(s : String) {
  val result = new StringBuffer()
  val formatter = new java.util.Formatter(result)
  
  def << (args : Object*) = {
    formatter.format(s, args : _*)
    result.toString
  }
  
  def substituting(args : Object*) = this << (args : _*)
  def sprintf(args : Object*) = this << (args : _*)
}

object SPrintf {
  implicit def string2Formatter(s : String) = new Formatter(s)
}
