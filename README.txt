text-searcher-scala/README.txt by john sullivan 2010.11

A brief description of files found here:

TextSearchRequirements.doc    a description of the problem to solve
search/TextSearcher.scala     my solution
search/TextTokenizer.java     a provided utility class
search/TextSearcherTest.java  provided unit tests
build.xml                     my build file with Java/Scala compilation
files/*.txt                   used by the tests
lib/*.jar                     junit jars

This is a pure Scala implementation of the Broad "Text Searcher"
coding exercise. The supporting classes, TextTokenizer and
TextSearcherTest, did not need to be modified for this solution to
work. I have, however, taken some liberties to bring TextTokenizer and
TextSearcherTest up to date:

 * Made TextTokenizer implement Iterator<String> instead of just
   Iterator, so that the client code does not always have to cast the
   tokens from Object to String
 * Added @Override annotations to TextTokenizer where appropriate
 * Upgraded TextSearcherTest to junit-4.8.2
   * This allowed me to replace local method assertArraysEqual with
     junit method assertArrayEquals

In order to get my build.xml to work, you will probably need to edit
the scala.home property on line 8. I am compiling against scala
2.8.0. My guess is that it will not compile with a lower version of
Scala.
