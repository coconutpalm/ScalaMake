package scalamake

import org.specs._
import PlatformExec._
import ScalaMake._
import SPrintf._
import scala.collection.mutable.HashSet

object ScalaMakeSpec extends ScalaMakeTest

class ScalaMakeTest extends SpecificationWithJUnit {
  PlatformExec.printResults = false

  def addFile(fileName : String, fileList : List[String]) = {
    !("touch %s" << fileName)
    val filesSoFar = fileList ::: List(fileName)
    filesSoFar
  }
  
  def makeTestFiles(rootDir : String, ext : String) = {
    !("rm -fr test%s" << rootDir)
    ("%s/one0/two0/three0" << rootDir).mkdirs
    ("%s/one0/two1" << rootDir).mkdirs
    ("%s/one1/two0/three0" << rootDir).mkdirs

    var filesCreated : List[String] = Nil
    filesCreated = addFile("%s/test.%s" << (rootDir, ext), filesCreated)
    filesCreated = addFile("%s/one0/test.%s" << (rootDir, ext), filesCreated)
    filesCreated = addFile("%s/one0/two0/three0/test.%s" << (rootDir, ext), filesCreated)
    filesCreated = addFile("%s/one0/two0/test.%s" << (rootDir, ext), filesCreated)
    filesCreated = addFile("%s/one0/two1/test.%s" << (rootDir, ext), filesCreated)
    filesCreated = addFile("%s/one1/two0/three0/test.%s" << (rootDir, ext), filesCreated)
    filesCreated = addFile("%s/one1/test.%s" << (rootDir, ext), filesCreated)
    filesCreated = addFile("%s/one1/two0/test.%s" << (rootDir, ext), filesCreated)
    filesCreated
  }
  
  def testToAvoidFailures = {
    
  }
  
  "Runs a rule with a single target and no dependencies" in {
    var ruleRan = false
    scalaMake {
      "ALL" buildWith {
        ruleRan = true
      }
    }
    ruleRan mustBe true
  }
  
  "Runs a rule and a dependency" in {
    var rulesProcessed : List[String] = Nil
    scalaMake {
      "ALL" dependsOn "dependantRule" buildWith { 
        rulesProcessed = rulesProcessed ::: List("ALL")
      }
      
      "dependantRule" buildWith { 
        rulesProcessed = rulesProcessed ::: List("dependantRule")
      }
    }
    val expectedRuleOrder = "dependantRule" :: "ALL" :: Nil
    expectedRuleOrder.equals(rulesProcessed) mustBe true
  }
  
  "Run an arbitrary starting rule by passing a parameter to ScalaMake" in {
    var ruleRan = false
    var startRuleDidNotRun = true
    
    scalaMake {
      "ALL" dependsOn "foo"
      
      "foo" buildWith {
        startRuleDidNotRun = false
      }
      
      "clean" buildWith {
        ruleRan = true
      }
      
      scalaMake("clean")
    }
    ruleRan mustBe true
    startRuleDidNotRun mustBe false
  }

  "Runs nested rules after all actions" in {
    var actionsProcessed : List[String] = Nil
    scalaMake {
      "ALL" buildWith { 
        actionsProcessed = actionsProcessed ::: List("First Action")
        "ALL_Nested" buildWith {
          actionsProcessed = actionsProcessed ::: List("Nested Rule Action")
        }
        actionsProcessed = actionsProcessed ::: List("Second Action")
      }
    }
    val expectedOrder = "First Action" :: "Second Action" :: "Nested Rule Action" :: Nil
    (expectedOrder equals actionsProcessed) mustBe true
  }
  
  "Actions that depend on files do not run if the target file is newer than the dependant" in {
    var actionRan = false;
    scalaMake {
      "target.txt" dependsOn "dependant.txt" buildWith {
        actionRan = true;
      }
    }
    actionRan mustBe false
  }
  
  "Actions that depend on files run if the target file is older than the dependant" in {
    !"touch dependant_newer.txt"
    var actionRan = false;
    scalaMake {
      "target.txt" dependsOn "dependant_newer.txt" buildWith {
        actionRan = true;
      }
    }
    actionRan mustBe true
  }
  
  "Actions that depend on files run if the target file is older than any dependant" in {
    !"touch dependant_newer.txt"
    var actionRan = false;
    scalaMake {
      "target.txt" dependsOn ("dependant.txt", "dependant_newer.txt") buildWith {
        actionRan = true;
      }
    }
    actionRan mustBe true
  }
  
  "Actions that depend on files run if the target file does not exist" in {
    var actionRan = false;
    scalaMake {
      "target_nonexistant.txt" dependsOn "dependant.txt" buildWith {
        actionRan = true;
      }
    }
    actionRan mustBe true
  }

  "Define and use generic rules to build files" in {
    var fileProcessed = ""
    scalaMake {
      "ALL" dependsOn "dependent.out"
      
      "*.out" dependsOn "*.txt" buildWith { fileBaseName =>
        fileProcessed = fileBaseName
      }
    }
    (fileProcessed == "dependent") mustBe true
  }

  "Define and use generic rules to build files does not build if target is newer than dependant" in {
    var fileProcessed = false
    scalaMake {
      "ALL" dependsOn "target.out"
      
      "*.out" dependsOn "*.txt" buildWith { fileBaseName =>
        fileProcessed = true
      }
    }
    fileProcessed mustBe false
  }

  "Recursively apply rules in all subdirectories of a root folder" in {
    var filesCreated = new HashSet[String]
    makeTestFiles("testSrc", "txt").foreach { file =>
      filesCreated += file
    }
    
    var filesFound = new HashSet[String]
    scalaMake {
      "testSrc/**" buildWith { dir =>
        "test.out" dependsOn "test.txt" buildWith { toBuild =>
          filesFound += "%s/test.txt" << dir
        }
      }
    }
    (filesCreated equals filesFound) mustBe true
  }

  "Build target if any (path/to/**/*.ext) is newer" in {
    // These were done above, so we won't redo them
    //!"touch target.txt"
    //Thread.sleep(500)
    //makeTestFiles("testSrc", "txt")  // test files will be newer
    
    var targetWasBuilt = false
    scalaMake {
      "target.txt" dependsOn "testSrc/**/*.txt" buildWith {
        targetWasBuilt = true
      }
    }
    
    targetWasBuilt mustBe true
  }
  
  "Build target if any file inside (path/to/**/) is newer" in {
    // These were done above, so we won't redo them
    //!"touch target.txt"
    //Thread.sleep(500)
    //makeTestFiles("testSrc", "txt")  // test files will be newer
    
    var targetWasBuilt = false
    scalaMake {
      "target.txt" dependsOn "testSrc/**/" buildWith {
        targetWasBuilt = true
      }
    }
    
    targetWasBuilt mustBe true
  }
  
  "Do not build target if all (path/to/**/*.ext) are older" in {
    //makeTestFiles("testSrc", "txt")
    !"touch newer_target.txt"  // target.txt will be newer
    
    var targetWasBuilt = false
    scalaMake {
      "newer_target.txt" dependsOn "testSrc/**/*.txt" buildWith {
        targetWasBuilt = true
      }
    }
    
    targetWasBuilt mustBe false
  }
  
  "Build (some/path/**) as a dependency" in {
    // This was done above, so we won't redo
    //makeTestFiles("testSrc", "txt")  // test files will be newer
    var built = false
    
    scalaMake {
      "ALL" dependsOn ("testSrc/**", "FOO")
      
      "FOO" buildWith {
        // noop
      }
      
      "testSrc/**" buildWith {
        built = true;
      }
    }
    
    built mustBe true
  }
  
}


