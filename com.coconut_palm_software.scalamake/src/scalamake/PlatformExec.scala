package scalamake

import java.io.InputStream
import java.io.InputStreamReader
import java.io.BufferedReader


class PlatformExec(command: String) {
  private def getCommand = {
    val osName = System.getProperty("os.name")
    osName match {
      case "Windows Vista" =>
        "cmd.exe /C " + command
      case "Windows XP" =>
        "cmd.exe /C " + command
      case "Windows NT" =>
        "cmd.exe /C " + command
      case "Windows 95" =>
        "command.com /C " + command
      case _ =>
        command
    }
  }
   
  var stdout = ""
  var stderr = ""
  var exitVal = 0
   
  def unary_! = {
    run(PlatformExec.printResults)
  }
  
  def execPrint() = {
    run(true)
  }
  
  def exec() = {
    run(false)
  }
  
  def run(printResults : Boolean) = {
    val runtime = Runtime.getRuntime
    if (printResults) {
      println(getCommand)
    }
    val process = runtime.exec(getCommand)
    val stdoutStream = new StreamGobbler(process.getInputStream)
    stdoutStream.printResults = printResults
    val stderrStream = new StreamGobbler(process.getErrorStream)
    stderrStream.printResults = printResults
     
    stdoutStream.start()
    stderrStream.start()
     
    this.exitVal = process.waitFor()
    while (stdoutStream.isAlive) java.lang.Thread.sleep(1)
    this.stdout = stdoutStream.result
    while (stderrStream.isAlive) java.lang.Thread.sleep(1)
    this.stderr = stderrStream.result
    
    if (exitVal > 0) {
      throw new RuntimeException(this.stdout + "\n" + this.stderr)
    }
    
    this.stdout
  }
   
  class StreamGobbler(stream : InputStream) extends Thread {
    var result = ""
    var printResults = false;
   
    override def run = {
      val isr = new InputStreamReader(stream)
      val br = new BufferedReader(isr)
     
      var line = ""

      do {
        line = br.readLine()
        if (line != null) {
          if (printResults) {
              println(line)
          }
          result = result + line + "\n"
        }
      } while (line != null)
    }
  }
}


object execute {
  def apply (command : String) {
    new PlatformExec(command).run(PlatformExec.printResults)
  }
}


object PlatformExec {
  implicit def string2PlatformExec(s : String) = new PlatformExec(s)
  implicit def string2File(s : String) = new java.io.File(s)

  var printResults = true
}
 
