package search;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.JUnitCore;

/** Unit tests for TextSearcher. Don't modify this file. */
public class TextSearcherTest {

  /** Runs the tests in {@link TextSearcherTest}. */
  public static void main(String[] args) {
    JUnitCore.main("search.TextSearcherTest");
  }

  /** Simplest possible case, no context and the word occurs exactly once. */
  @Test public void testOneHitNoContext() throws Exception {
    String[] expected = { "sketch" };
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("sketch", 0);
    assertArrayEquals(expected, results);
  }

  /** Next simplest case, no context and multiple hits. */
  @Test public void testMultipleHitsNoContext() throws Exception {
    String[] expected = { "naturalists", "naturalists" };
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("naturalists", 0);
    assertArrayEquals(expected, results);
  }

  /** This is the example from the document. */
  @Test public void testBasicSearch() throws Exception {
    String[] expected = {
      "great majority of naturalists believed that species",
      "authors.  Some few naturalists, on the other" };
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("naturalists", 3);
    assertArrayEquals(expected, results);
  }

  /** Same as basic search but a little more context. */
  @Test public void testBasicMoreContext() throws Exception {
    String[] expected = {
      "Until recently the great majority of naturalists believed that species were immutable productions",
      "maintained by many authors.  Some few naturalists, on the other hand, have believed" };
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("naturalists", 6);
    assertArrayEquals(expected, results);
  }

  /** Tests query word with apostrophe. */
  @Test public void testApostropheQuery() throws Exception {
    String[] expected = { "not indeed to the animal's or plant's own good",
                          "habitually speak of an animal's organisation as\r\nsomething plastic" };
    File file = new File("files/long_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("animal's", 4);
    assertArrayEquals(expected, results);
  }

  /** Tests numeric query word. */
  @Test public void testNumericQuery() throws Exception {
    String[] expected = { "enlarged in 1844 into a",
                          "sketch of 1844--honoured me" };
    File file = new File("files/long_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("1844", 2);
    assertArrayEquals(expected, results);
  }

  /** Tests mixed alphanumeric query word. */
  @Test public void testMixedQuery() throws Exception {
    String[] expected = { "date first edition [xxxxx10x.xxx] please check" };
    File file = new File("files/long_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("xxxxx10x", 3);
    assertArrayEquals(expected, results);
  }

  /** Should get same results regardless of case. */
  @Test public void testCaseInsensitiveSearch() throws Exception {
    String[] expected = {
      "on the Origin of Species.  Until recently the great",
      "of naturalists believed that species were immutable productions, and",
      "hand, have believed that species undergo modification, and that" };
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("species", 4);
    assertArrayEquals(expected, results);

    results = searcher.search("SPECIES", 4);
    assertArrayEquals(expected, results);

    results = searcher.search("SpEcIeS", 4);
    assertArrayEquals(expected, results);
  }

  /** Hit that overlaps file start should still work. */
  @Test public void testNearBeginning() throws Exception {
    String[] expected = { "I will here give a brief sketch" };
    
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("here", 4);
    assertArrayEquals(expected, results);
  }

  /** Hit that overlaps file end should still work. */
  @Test public void testNearEnd() throws Exception {
    String[] expected = { "and that the existing forms of life",
                          "generation of pre existing forms." };
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("existing", 3);
    assertArrayEquals(expected, results);
  }

  /** Searcher can execute multiple searches after initialization. */
  @Test public void testMultipleSearches() throws Exception {
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] expected;
    String[] results;
    
    // Just runs the same queries as other tests, but on a single TextSearcher instance:
    expected = new String[] {
      "on the Origin of Species.  Until recently the great",
      "of naturalists believed that species were immutable productions, and",
      "hand, have believed that species undergo modification, and that" };
    results = searcher.search("species",4);
    assertArrayEquals(expected,results);
    
    expected = new String[] { "I will here give a brief sketch" };
    results = searcher.search("here",4);
    assertArrayEquals(expected,results);
    
    expected = new String[] { "and that the existing forms of life",
                              "generation of pre existing forms." };
    results = searcher.search("existing",3);
    assertArrayEquals(expected,results);
  }
  
  /** Overlapping hits should just come back as separate hits. */
  @Test public void testOverlappingHits() throws Exception {
    String[] expected = {
      "of naturalists believed that species were immutable",
      "hand, have believed that species undergo modification",
      "undergo modification, and that the existing forms",
      
    };
    File file = new File("files/short_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("that", 3);
    assertArrayEquals(expected, results);
  }
  
  /** If no hits, get back an empty array. */
  @Test public void testNoHits() throws Exception {
    File file = new File("files/long_excerpt.txt");
    TextSearcher searcher = new TextSearcher(file);
    String[] results = searcher.search("slejrlskejrlkajlsklejrlksjekl", 3);
    assertNotNull(results);
    assertEquals(0, results.length);
  }
  
  /** Verify the tokenizer. This should always pass. */
  @Test public void testTokenizer() throws Exception {
    String input = "123, 789: def";
    // In this test we define words to be strings of digits
    String[] expected = { "123",", ","789", ": def" };
    TextTokenizer lexer = new TextTokenizer(input, "[0-9]+");
    List<String> tokens = new ArrayList<String>();
    while (lexer.hasNext()) tokens.add(lexer.next());
    String[] results = tokens.toArray(new String[tokens.size()]);
    assertArrayEquals(expected,results);
    
    assertTrue(lexer.isWord("1029384"));
    assertFalse(lexer.isWord("1029388 "));
    assertFalse(lexer.isWord("123,456"));
  }
}
