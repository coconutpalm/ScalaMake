ScalaMake
=========

Sometimes all you need is something to process filesystem constraints
and script Scala, Java, or platform shell commands based on the
dependencies in your filesystem.  Sometimes all you need is a
cross-platform thing like "Make".  Sometimes all you need is
ScalaMake.

Internally, I use ScalaMake to script separate builds (that may run
using Maven or SBT) into a cohesive whole.

"OK already, enough talk:  show me the code!"  :-)


What ScalaMake code looks like
------------------------------

The complete ScalaMake specification is defined using "Specs" and it's
in the zipfile, but here's a taste:

You can get into ScalaMake syntax using the scalaMake keyword in any
Scala code, you can define a ScalaMakefile object and run it like any
regular Scala application, or you can define a ScalaMake makefile as a
Scala script and run it using the "scalamake" shell script without
compiling it:

    "Runs a rule and a dependency" in {
      var rulesProcessed : List[String] = Nil
      scalaMake {
        "ALL" dependsOn "dependantRule" buildWith { 
          // In real code you would actually build something here, 
          // but since this is a test, we'll just note that we were called.
          rulesProcessed = rulesProcessed ::: List("ALL")
        }
      
        "dependantRule" buildWith { 
          rulesProcessed = rulesProcessed ::: List("dependantRule")
        }
      }
      val expectedRuleOrder = "dependantRule" :: "ALL" :: Nil
      expectedRuleOrder.equals(rulesProcessed) mustBe true
    }

You can use Ant-style recursive directory dependencies:

    "Build target if any (path/to/**/*.ext) is newer" in {
      !"touch target.txt"
      makeTestFiles("testSrc", "txt")  // test files will be newer
    
      var targetWasBuilt = false
      scalaMake {
        "target.txt" dependsOn "testSrc/**/*.txt" buildWith {
          targetWasBuilt = true
        }
      }
    
      targetWasBuilt mustBe true
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

That's enough for now.  If you like this, just download the .zip and
get started.  Or pull the whole repo, build it using Maven 3, and
contribute!

--Dave Orme
