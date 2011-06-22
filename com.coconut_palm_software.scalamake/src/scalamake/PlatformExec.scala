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
  
  def &() = {
    runInBackground(PlatformExec.printResults)
    print("")
  }
  
  def background() = {
    runInBackground(PlatformExec.printResults)
    print("")
  }
  
  def execPrint() = {
    run(true)
  }
  
  def exec() = {
    run(false)
  }
  
  def runInBackground(printResults : Boolean) = {
    val runtime = Runtime.getRuntime
    
    val userDirPath = System.getProperty("user.dir")
    val userDir = new java.io.File(userDirPath)
    
    if (printResults) {
      println(getCommand)
    }
    
    val process = runtime.exec(getCommand, null, userDir)
    val stdoutStream = new StreamGobbler(process.getInputStream)
    stdoutStream.printResults = printResults
    val stderrStream = new StreamGobbler(process.getErrorStream)
    stderrStream.printResults = printResults
     
    stdoutStream.start()
    stderrStream.start()

    (process, process.getInputStream, stdoutStream, stderrStream)
  }
  
  def run(printResults : Boolean) = {
    val (process, inputStream, stdoutStream, stderrStream) = runInBackground(printResults)
     
    this.exitVal = process.waitFor()
    // Deal with race conditions waiting for the streams to close...
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

object chdir {
  def apply[A](dir : String)(a: => A): A = {
    if (!new java.io.File(dir).isDirectory) {
      throw new IllegalArgumentException(dir + " is not a directory")
    }
    val original = System.getProperty("user.dir") 
    if (PlatformExec.printResults) println("Changing to : " + dir)
    System.setProperty("user.dir", dir) 
    try {
      a 
    } finally { 
      if (PlatformExec.printResults) println("Returning to: " + original)
      System.setProperty("user.dir", original) 
    }
  }
}

class ChDir(dir : String) {
  def cd[A](a: => A) = {
    chdir(dir)(a)
  }
}

object PlatformExec {
  implicit def string2PlatformExec(s : String) = new PlatformExec(s)
  implicit def string2File(s : String) = new java.io.File(s)
  implicit def string2ChDir(dir : String) = new ChDir(dir)

  def chdir[A](dir: String)(a: => A): A = { 
    new ChDir(dir).cd(a)
  } 

  var printResults = true
}
 
