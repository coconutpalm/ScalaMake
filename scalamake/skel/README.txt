ScalaMake - A cross-platform "make" with a sane syntax

Usage:

Currently there is a startup script for *nixes and Cygwin.  A .bat file
contribution for Windows would be appreciated.

Place this unpacked distribution in a reasonable directory for your platform.
e.g.:

/usr/local/java/scalamake

Put that directory on your path.

Assign the SCALAMAKE_HOME environment variable to the path where you 
put the distribution.

Make sure that the "scalamake" script is executable and that the "scala"
binary is on your path.


ScalaMake Syntax:

From the command line:

`scalamake ScalaMakefileName.scala [buildTarget]`


ScalaMakefile Syntax

A skeleton ScalaMakefile is packaged with the distribution as SMakefile.scala.

The allowed syntax is specified using Specs.  For convenience, the tests are
bundled and included in the distribution, but to run them you'll need to check
out the full repository and run them from Eclipse using the JUnit 4 runner.

---

Building:

Going forward, ScalaMake builds using SBT and/or Typesafe Activator.  Build
using the "package" target in SBT, then run the "package.sh" script to create
the release zip.  Note that depending on the version of Scala, etc., you might
have to update the scalamake script to have the correct jar file name for
scalamake.

Note that tests assume a POSIX-compatible command-line environment.  Building
on Windows outside of Cygwin is NOT supported at this time, and I haven't 
tested building on Cygwin either.  Patches / pull requests are welcome.

