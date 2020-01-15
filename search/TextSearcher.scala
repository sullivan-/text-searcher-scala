package search

import scala.collection.immutable.IndexedSeq
import scala.collection.immutable.Map
import scala.collection.immutable.MapProxy

import java.io.File
import java.io.FileReader
import java.io.StringWriter

/**
 * Processes the contents of a text file for searching the file contents for
 * specific words.
 * 
 * @author john sullivan
 */
class TextSearcher(file: File) {

  /**
   * The contents of the text file to search over.
   */
  private val fileContents: String = extractContentsFromFile()

  private def extractContentsFromFile(): String = {
    val fileReader: FileReader = new FileReader(file)
    val stringWriter: StringWriter = new StringWriter()
    val buffer: Array[Char] = Array[Char](4096)
    extractContentsFromFileReader(fileReader, stringWriter, buffer)
    stringWriter.toString()
  }

  private def extractContentsFromFileReader(fileReader: FileReader,
                                            stringWriter: StringWriter,
                                            buffer: Array[Char]): Unit = {
    // i could define buffer here, instead of passing it as argument. problem is, if
    // i do that, then a new array buffer is going to get allocated for every iteration
    // in the tail-call optimized loop
    val readCount = fileReader.read(buffer)
    if (readCount > 0) {
      stringWriter.write(buffer, 0, readCount)

      // compiler is able to convert this tail recursion into a while loop
      extractContentsFromFileReader(fileReader, stringWriter, buffer)
    }
  }

  /**
   * An immutable Map where the keys are always Strings, and are always converted into
   * uppercase before any map operations.
   */
  // the default value for constructor arg self, Map[String,B](), returns an immutable.HashMap
  private[TextSearcher] class UpperCaseMap[+B]
  (val self: Map[String,B] = Map[String,B]())
  extends Map[String,B] {
    def get(key: String): Option[B] = self get key.toUpperCase
    def iterator: Iterator[(String, B)] = self.iterator
    def +[B1 >: B](kv: (String, B1)): UpperCaseMap[B1] =
      new UpperCaseMap(self + Pair(kv._1.toUpperCase, kv._2))
    def -(key: String): UpperCaseMap[B] = new UpperCaseMap(self - key.toUpperCase)
  }

  /**
   * Encapsulates the data and methods needed to perform a search, as well as a
   * method for producing a new Searcher given the next word in the file contents.
   */
  private[TextSearcher] case class Searcher(wordCount: Int,
                                            wordStartEndIndexes: IndexedSeq[(Int,Int)],
                                            wordIndexMap: UpperCaseMap[IndexedSeq[Int]]) {

    /**
     * Creates and returns a new searcher that is equivalent to the current searcher,
     * but with one more word occurrence indexed.
     */
    def addWord(nextWord: String, charIndex: Int): Searcher = {
      Searcher(this.wordCount + 1,
               this.wordStartEndIndexes :+ (charIndex -> (charIndex + nextWord.length())),
               this.wordIndexMap +
               (nextWord ->
                (wordIndexMap.getOrElse(nextWord, IndexedSeq()) :+ this.wordCount)))
    }

    /**
     * Searches the file contents for occurrences of a specific word.
     * @param queryWord The word to search for in the file contents.
     * @param contextWords The number of words of context to provide on
     *                     each side of the query word.
     * @return One context string for each time the query word appears in the file.
     */
    def search(queryWord: String, contextWords: Int): Array[String] = {
      val wordIndices: IndexedSeq[Int] = wordIndexMap.getOrElse(queryWord,
                                                                IndexedSeq())

      // the call to wordIndices.view here avoids construction of an intermediate
      // Vector to represent contextStrings
      val contextStrings: Seq[String] = wordIndices.view map { wordIndex =>
        fileContents.substring(getContextStringStartIndex(wordIndex, contextWords),
                               getContextStringEndIndex(wordIndex, contextWords))
      }
      contextStrings.toArray
    }
  
    /**
     * Gets the index of the start of the context string from within the file contents,
     * based on the word number and the number of context words. Takes care not to return an
     * index that is out of bounds.
     */
    private def getContextStringStartIndex(wordNumber: Int, contextWords: Int): Int = {
      if (wordNumber - contextWords < 0)
        0
      else
        wordStartEndIndexes(wordNumber - contextWords)._1
    }
  
    /**
     * Gets the index of the end of the context string from within the file contents,
     * based on the word number and the number of context words. Takes care not to return an
     * index that is out of bounds.
     */
    private def getContextStringEndIndex(wordNumber: Int, contextWords: Int): Int = {
      if (wordNumber + contextWords >= wordCount)
        fileContents.length()
      else
        wordStartEndIndexes(wordNumber + contextWords)._2
    }
  }

  private val searcher: Searcher = buildSearcher()

  /**
   * Initializes the internal data structure needed for this class to implement
   * search efficiently.
   */
  private def buildSearcher(): Searcher = {
    val textTokenizer = new TextTokenizer(fileContents, "[a-zA-Z0-9']+")
    // IndexedSeq() returns a immutable.Vector
    val searcher = Searcher(0, IndexedSeq(), new UpperCaseMap[IndexedSeq[Int]]())
    buildSearcher(textTokenizer, 0, searcher)
  }

  private def buildSearcher(textTokenizer: TextTokenizer,
                            charIndex: Int,
                            searcher: Searcher): Searcher = {
    if (textTokenizer.hasNext()) {
      val token: String = textTokenizer.next()
      val newSearcher =
        if (textTokenizer.isWord(token))
          searcher.addWord(token, charIndex)
        else
          searcher
      // compiler is able to convert this tail recursion into a while loop
      buildSearcher(textTokenizer, charIndex + token.length(), newSearcher)
    }
    else
      searcher
  }

  /**
   * Searches the file contents for occurrences of a specific word.
   * @param queryWord The word to search for in the file contents.
   * @param contextWords The number of words of context to provide on
   *                     each side of the query word.
   * @return One context string for each time the query word appears in the file.
   */
  def search(queryWord: String, contextWords: Int): Array[String] =
    searcher.search(queryWord, contextWords)
}
