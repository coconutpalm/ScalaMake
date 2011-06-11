package scalamake

import PlatformExec._
import SPrintf._
import scala.collection.mutable.HashMap

object EvalStack {
  val FIRST_RULE = "ScalaMakeFirstRule"
  var ruleSet = new HashMap[String, Rule]
  var stack : List[HashMap[String, Rule]] = Nil
}

class Rule(_target : String) {
  if (EvalStack.ruleSet.isEmpty) {
    EvalStack.ruleSet += (EvalStack.FIRST_RULE -> this)
  }
  EvalStack.ruleSet += (_target -> this)

  private var _dependencies : List[String] = Nil
  private var action0 : () => Unit = {() =>  }
  private var action1 : (String) => Unit = { file => }

  def target = {
    if (directoryBase.length > 0) {
      "%s/%s" << (directoryBase, _target)
    } else {
      _target
    }
  }
  
  def dependencies = _dependencies.map { dependent =>
    if (directoryBase.length > 0) {
      "%s/%s" << (directoryBase, dependent)
    } else {
      dependent
    }
  }

  var directoryBase = ""
  var fileBaseName = ""
  
  def dependsOn(dependencies : String*) = {
    for (dependant <- dependencies) {
        this._dependencies = this._dependencies ::: List(dependant)
    }
    this
  }
  def buildWith(action : => Unit) = {
    this.action0 = { () => action }
  }
  def buildWith(action : (String) => Unit) = {
    this.action1 = { file => action(file) }
    if (_dependencies.length == 0) {
      runActions = recursivelyBuildDirectory
    } else {
      runActions = buildGenericTarget
    }
  }

  // By default runActions() just runs action0()
  var runActions : () => Unit = { () => action0() }
  
  def buildGenericTarget() = {
    action1(fileBaseName)
  }
  
  def recursivelyBuildDirectory() = {
    var targetDir = target
    if (target.endsWith("/**")) {
      targetDir = target.subSequence(0, target.length-3).toString
    }
    var directories : List[String] = Nil
    if (directoryBase.length > 0) {
      val startDir = "%s/%s" << (directoryBase, targetDir)
      directories = FileUtil.getDirectories(startDir)
    } else {
      directories = FileUtil.getDirectories(targetDir)
    }
    
    directories.foreach { directory =>
      EvalStack.stack = EvalStack.ruleSet :: EvalStack.stack
      EvalStack.ruleSet = new HashMap[String, Rule]
      try {
        action1(directory)
        for (rule <- EvalStack.ruleSet.values) {
          rule.directoryBase = directory
        }
        scalaMake.runRule(EvalStack.FIRST_RULE, EvalStack.ruleSet)
      } finally {
        EvalStack.ruleSet = EvalStack.stack.head
        EvalStack.stack = EvalStack.stack.tail
      }
    }
  }
}

object scalaMake {
  /**
   * Run the current ScaleMake context
   */
  def apply() = {
    runRule(EvalStack.FIRST_RULE, EvalStack.ruleSet)
  }
  /**
   * Run ScalaMake on the current context, starting with rule "target"
   */
  def apply(target : String) = {
    runRule(target, EvalStack.ruleSet)
  }
  /**
   * Run ScalaMake, using the build rules contained in the specified function.
   */
  def apply(ruleDefinitions : => Unit) = {
    EvalStack.stack = EvalStack.ruleSet :: EvalStack.stack
    EvalStack.ruleSet = new HashMap[String, Rule]
    try {
      ruleDefinitions
      runRule(EvalStack.FIRST_RULE, EvalStack.ruleSet)
    } finally {
      EvalStack.ruleSet = EvalStack.stack.head
      EvalStack.stack = EvalStack.stack.tail
    }
  }
  
  private def isDependencyUpToDate(target : String, dependency : String) = {
    if (!new java.io.File(target).exists || 
          dependency.lastModified > target.lastModified) {
      false
    } else {
      true
    }
  }
  
  private def processDependencies(rule : Rule, ruleSet : HashMap[String, Rule]) : Boolean = {
    var foundFileDependencies = false
    var upToDate = true
    
    for (dependency <- rule.dependencies) {
      val baseDirEndIndex = dependency.lastIndexOf("/**")
      if (baseDirEndIndex >= 0) {
        val baseDirPath = dependency.subSequence(0, baseDirEndIndex)
        var filePattern = ""
        if (dependency.length > baseDirEndIndex+4) {
          filePattern = dependency.substring(baseDirEndIndex+4)
        }
        if (dependency.substring(baseDirEndIndex+3).length <= 0) {
          runRule(dependency, ruleSet)
        } else { 
          foundFileDependencies = true;
        
          val files = FileUtil.getFiles(baseDirPath.toString())
          for (dependentFile <- files) {
            if (filePattern.length > 0) {
              val wildcardPos = filePattern.indexOf("*")
              if (wildcardPos >= 0) {
                // |base/dir/**/suffix/dir/*abc.ext|
                val fileSuffix = dependentFile.substring(baseDirEndIndex+1)
                val suffixBeforeWildcard = fileSuffix.subSequence(0, wildcardPos)
                val suffixAfterWildcard = filePattern.substring(wildcardPos+1)
                if (fileSuffix.startsWith(suffixBeforeWildcard.toString) &&
                    fileSuffix.endsWith(suffixAfterWildcard)) 
                {
                  // We found a wildcard match
                  if (!isDependencyUpToDate(rule.target, dependentFile)) {
                    upToDate = false
                  }
                }
              } else {
                if (dependentFile.endsWith(filePattern)) {
                  if (!isDependencyUpToDate(rule.target, dependentFile)) {
                    upToDate = false
                  }
                }
              }
            } else {
              if (!isDependencyUpToDate(rule.target, dependentFile)) {
                upToDate = false
              }
            }
          }
        }
      } else if (dependency.startsWith("*.")) {
        val targetFile = rule.fileBaseName + rule.target.substring(1)
        val dependencyFile = rule.fileBaseName + dependency.substring(1)
        if (dependencyFile.isFile()) {
          foundFileDependencies = true
          if (!isDependencyUpToDate(targetFile, dependencyFile)) {
            upToDate = false
          }
        }
      } else if (dependency.isFile()) {
        foundFileDependencies = true
        if (!isDependencyUpToDate(rule.target, dependency)) {
          upToDate = false
        }
      } else {
        runRule(dependency, ruleSet)
      }
    }
    !foundFileDependencies || !upToDate
  }
  
  private def runRule(rule : Rule, ruleSet : HashMap[String,Rule]) : Unit = {
    val runActions = processDependencies(rule, ruleSet)
        
    if (runActions) {
      EvalStack.stack = EvalStack.ruleSet :: EvalStack.stack
      EvalStack.ruleSet = new HashMap[String, Rule]
      try {
        rule.runActions()
        if (!EvalStack.ruleSet.isEmpty) {
          runRule(EvalStack.FIRST_RULE, EvalStack.ruleSet)
        }
      } finally {
        EvalStack.ruleSet = EvalStack.stack.head
        EvalStack.stack = EvalStack.stack.tail
      }
    }
  }
  
  def runRule(ruleTarget : String, ruleSet : HashMap[String,Rule]) : Unit = {
    ruleSet.get(ruleTarget) match {
      case Some(rule) => {
        runRule(rule, ruleSet)
      }
      case None => {
        val possibleError = "No rule to make : %s" << ruleTarget;
        
        val suffixPos = ruleTarget.lastIndexOf('.')
        if (suffixPos > 0) {
          val suffix = ruleTarget.substring(suffixPos)
          val genericRuleTarget = "*" + suffix
          ruleSet.get(genericRuleTarget) match {
            case Some(rule) => {
              val lastBaseIndexPos = ruleTarget.lastIndexOf(".")
              rule.fileBaseName = ruleTarget.slice(0, lastBaseIndexPos)
              try {
                runRule(rule, ruleSet)
              } finally {
                rule.fileBaseName = ""
              }
            }
            case None => {
              throw new RuntimeException(possibleError)
            }
          }
        } else {
          throw new RuntimeException(possibleError)
        }
      }
    }
  }
}

object ScalaMake {
  implicit def string2BuildRule(s : String) = new Rule(s)
}

private object FileUtil {
  def getDirectories(startDir : String) : List[String] = {
    var directories : List[String] = Nil
  
    def get(startDir : String) : Unit = {
      directories = directories ::: List(startDir)

      val dirsToProcess = startDir.list
      if (dirsToProcess == null) {
        throw new RuntimeException("Directory: %s not found" << startDir)
      }

      dirsToProcess.foreach { entry =>
        val fullDir = "%s/%s" << (startDir, entry)
        if (fullDir.isDirectory) {
          get(fullDir)
        }
      }
    }
    
    get(startDir)
    directories
  }
  
  def getFiles(startDir : String) : List[String] = {
    var files : List[String] = Nil
  
    def get(startDir : String) : Unit = {
      val dirsToProcess = startDir.list
      if (dirsToProcess == null) {
        throw new RuntimeException("Directory: %s not found" << startDir)
      }

      dirsToProcess.foreach { entry =>
        val fullEntry = "%s/%s" << (startDir, entry)
        if (fullEntry.isDirectory) {
          get(fullEntry)
        } else if (fullEntry.isFile) {
          files = files ::: List(fullEntry)
        }
      }
    }
    
    get(startDir)
    files
  }
  
  
}

