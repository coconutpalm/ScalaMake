package scalamake

import PlatformExec._
import org.specs._

object PlatformExecSpec extends PlatformExecTest

class PlatformExecTest extends SpecificationWithJUnit {

  PlatformExec.printResults=false
  
  def testToAvoidFailures = {
    
  }
  
  // Assumes *nix style tools available on the path
  """ !"<command>" runs an O/S command and returns stdout as a String""" in {
    (!"echo Hello, world" startsWith "Hello, world") mustBe true
  }
  
  """ !"<command>" throws RuntimeException with nonzero exit""" in {
    try {
      !"dir /blah/barf/blech"
      fail
    } catch {
      case ex : RuntimeException =>  true mustBe true // success
    }
  }
  
  "Implicitly convert java.lang.String to java.io.File" in {
    val fileList = new java.io.File(".").list
    val implicitFileList = ".".list
    fileList.corresponds(implicitFileList)( _ == _ ) mustBe true
  }
  
  "chdir changes the user.dir" in {
    val originalDir = System.getProperty("user.dir")
    chdir("/") {
      System.getProperty("user.dir") mustBe "/"
    }
    System.getProperty("user.dir") mustBe originalDir
  }
  
  "chdir to illegal dir throws IllegalArgumentException" in {
    try {
      chdir("/adirectorythatdoesnotexistinanyonescomputer") {
        fail
      }
    } catch {
      case e : IllegalArgumentException => true mustBe true // success
    }
  }
  
  "Run a command in the background" in {
    "echo In the background...".background
    true mustBe true
  }
}
