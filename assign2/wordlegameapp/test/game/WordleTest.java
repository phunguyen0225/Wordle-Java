package game;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static game.Wordle.MatchResponse.*;
import static game.Wordle.Status;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


public class WordleTest {
  WordSpellingService wordSpellingService = word -> true;

  @Test
  public void canary() {
    assertTrue(true);
  }

  @Test
  void tallyGuessWithTarget() {
    assertAll(
            () -> assertEquals(List.of(EXACT, EXACT, EXACT, EXACT, EXACT), Wordle.tally("FAVOR", "FAVOR")),
            () -> assertEquals(List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), Wordle.tally("FAVOR", "TESTS")),
            () -> assertEquals(List.of(MATCH, EXACT, NO_MATCH, NO_MATCH, NO_MATCH), Wordle.tally("FAVOR", "RAPID")),
            () -> assertEquals(List.of(NO_MATCH, EXACT, NO_MATCH, EXACT, EXACT), Wordle.tally("FAVOR", "MAYOR")),
            () -> assertEquals(List.of(NO_MATCH, NO_MATCH, EXACT, NO_MATCH, EXACT), Wordle.tally("FAVOR", "RIVER")),
            () -> assertEquals(List.of(MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), Wordle.tally("FAVOR", "AMASS")),
            () -> assertEquals(List.of(EXACT, EXACT, EXACT, EXACT, EXACT), Wordle.tally("SKILL", "SKILL")),
            () -> assertEquals(List.of(EXACT, NO_MATCH, EXACT, NO_MATCH, EXACT), Wordle.tally("SKILL", "SWIRL")),
            () -> assertEquals(List.of(NO_MATCH, MATCH, NO_MATCH, NO_MATCH, EXACT), Wordle.tally("SKILL", "CIVIL")),
            () -> assertEquals(List.of(EXACT, NO_MATCH, EXACT, NO_MATCH, NO_MATCH), Wordle.tally("SKILL", "SHIMS")),
            () -> assertEquals(List.of(EXACT, MATCH, MATCH, EXACT, NO_MATCH), Wordle.tally("SKILL", "SILLY")),
            () -> assertEquals(List.of(MATCH, NO_MATCH, MATCH, MATCH, NO_MATCH), Wordle.tally("SAGAS", "ABASE"))
    );
  }

  @Test
  void tallyInvalidGuess() {
    assertAll(
            () -> assertEquals("Invalid guess", assertThrows(RuntimeException.class, () -> Wordle.tally("FAVOR", "FOR")).getMessage()),
            () -> assertEquals("Invalid guess", assertThrows(RuntimeException.class, () -> Wordle.tally("FAVOR", "FERVER")).getMessage())
    );
  }

  @Test
  void playFirstAttemptCorrectGuess() {
    Supplier<String> readGuess = () -> "FAVOR";
    AtomicBoolean displayCalled = new AtomicBoolean(false);

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      assertEquals(1, numberOfAttempts);
      assertEquals(Status.WON, status);
      assertEquals(List.of(EXACT, EXACT, EXACT, EXACT, EXACT), response);
      assertEquals("Amazing", message);
      displayCalled.set(true);
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertTrue(displayCalled.get());
  }

  @Test
  void playFirstAttemptInvalidGuess() {
    Supplier<String> readGuess = () -> "FOR";

    assertThrows(RuntimeException.class, () -> Wordle.play("FAVOR", readGuess, null, wordSpellingService), "Invalid guess");
  }

  @Test
  void playFirstAttemptInCorrectGuess() {
    var guesses = new LinkedList<String>(List.of("TESTS", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();
    AtomicBoolean displayCalled = new AtomicBoolean(false);

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      if (numberOfAttempts == 1) {
        assertEquals(1, numberOfAttempts);
        assertEquals(Status.IN_PROGRESS, status);
        assertEquals(List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), response);
        assertEquals("", message);
        displayCalled.set(true);
      }
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertTrue(displayCalled.get());
  }

  @Test
  void playSecondAttemptCorrectGuess() {
    var guesses = new LinkedList<String>(List.of("TESTS", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();

    var displayCallCount = new AtomicInteger(0);
    var expectedResults = new LinkedList<List<Object>>(List.of(
            List.of(1, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(2, Status.WON, List.of(EXACT, EXACT, EXACT, EXACT, EXACT), "Splendid")
    ));

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      var expected = expectedResults.pop();

      assertEquals(expected.get(0), numberOfAttempts);
      assertEquals(expected.get(1), status);
      assertEquals(expected.get(2), response);
      assertEquals(expected.get(3), message);

      displayCallCount.incrementAndGet();
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertEquals(2, displayCallCount.get());
  }

  @Test
  void playSecondAttemptInCorrectGuess() {
    var guesses = new LinkedList<String>(List.of("TESTS", "RAPID", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();

    AtomicBoolean displayCalled = new AtomicBoolean(false);

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      if (numberOfAttempts == 2) {
        assertEquals(2, numberOfAttempts);
        assertEquals(Status.IN_PROGRESS, status);
        assertEquals(List.of(MATCH, EXACT, NO_MATCH, NO_MATCH, NO_MATCH), response);
        assertEquals("", message);
        displayCalled.set(true);
      }
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertTrue(displayCalled.get());
  }

  @Test
  void playThirdAttemptCorrectGuess() {
    var guesses = new LinkedList<String>(List.of("RAPID", "TESTS", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();

    var displayCallCount = new AtomicInteger(0);
    var expectedResults = new LinkedList<List<Object>>(List.of(
            List.of(1, Status.IN_PROGRESS, List.of(MATCH, EXACT, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(2, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(3, Status.WON, List.of(EXACT, EXACT, EXACT, EXACT, EXACT), "Awesome")
    ));

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      var expected = expectedResults.pop();

      assertEquals(expected.get(0), numberOfAttempts);
      assertEquals(expected.get(1), status);
      assertEquals(expected.get(2), response);
      assertEquals(expected.get(3), message);

      displayCallCount.incrementAndGet();
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertEquals(3, displayCallCount.get());
  }

  @Test
  void playFourthAttemptCorrectGuess() {
    var guesses = new LinkedList<String>(List.of("MAYOR", "RAPID", "TESTS", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();

    var displayCallCount = new AtomicInteger(0);
    var expectedResults = new LinkedList<List<Object>>(List.of(
            List.of(1, Status.IN_PROGRESS, List.of(NO_MATCH, EXACT, NO_MATCH, EXACT, EXACT), ""),
            List.of(2, Status.IN_PROGRESS, List.of(MATCH, EXACT, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(3, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(4, Status.WON, List.of(EXACT, EXACT, EXACT, EXACT, EXACT), "Yah")
    ));

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      var expected = expectedResults.pop();

      assertEquals(expected.get(0), numberOfAttempts);
      assertEquals(expected.get(1), status);
      assertEquals(expected.get(2), response);
      assertEquals(expected.get(3), message);

      displayCallCount.incrementAndGet();
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertEquals(4, displayCallCount.get());
  }

  @Test
  void playFifthAttemptCorrectGuess() {
    var guesses = new LinkedList<String>(List.of("RIVER", "MAYOR", "RAPID", "TESTS", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();

    var displayCallCount = new AtomicInteger(0);
    var expectedResults = new LinkedList<List<Object>>(List.of(
            List.of(1, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, EXACT, NO_MATCH, EXACT), ""),
            List.of(2, Status.IN_PROGRESS, List.of(NO_MATCH, EXACT, NO_MATCH, EXACT, EXACT), ""),
            List.of(3, Status.IN_PROGRESS, List.of(MATCH, EXACT, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(4, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(5, Status.WON, List.of(EXACT, EXACT, EXACT, EXACT, EXACT), "Yah")
    ));

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      var expected = expectedResults.pop();

      assertEquals(expected.get(0), numberOfAttempts);
      assertEquals(expected.get(1), status);
      assertEquals(expected.get(2), response);
      assertEquals(expected.get(3), message);

      displayCallCount.incrementAndGet();
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertEquals(5, displayCallCount.get());
  }

  @Test
  void playSixthAttemptCorrectGuess() {
    var guesses = new LinkedList<String>(List.of("AMASS", "RIVER", "MAYOR", "RAPID", "TESTS", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();

    var displayCallCount = new AtomicInteger(0);
    var expectedResults = new LinkedList<List<Object>>(List.of(
            List.of(1, Status.IN_PROGRESS, List.of(MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(2, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, EXACT, NO_MATCH, EXACT), ""),
            List.of(3, Status.IN_PROGRESS, List.of(NO_MATCH, EXACT, NO_MATCH, EXACT, EXACT), ""),
            List.of(4, Status.IN_PROGRESS, List.of(MATCH, EXACT, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(5, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(6, Status.WON, List.of(EXACT, EXACT, EXACT, EXACT, EXACT), "Yah")
    ));

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      var expected = expectedResults.pop();

      assertEquals(expected.get(0), numberOfAttempts);
      assertEquals(expected.get(1), status);
      assertEquals(expected.get(2), response);
      assertEquals(expected.get(3), message);

      displayCallCount.incrementAndGet();
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertEquals(6, displayCallCount.get());
  }

  @Test
  void playSixthAttemptInCorrectGuess() {
    var guesses = new LinkedList<String>(List.of("AMASS", "RIVER", "MAYOR", "RAPID", "TESTS", "AMASS"));
    Supplier<String> readGuess = () -> guesses.pop();

    var displayCallCount = new AtomicInteger(0);
    var expectedResults = new LinkedList<List<Object>>(List.of(
            List.of(1, Status.IN_PROGRESS, List.of(MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(2, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, EXACT, NO_MATCH, EXACT), ""),
            List.of(3, Status.IN_PROGRESS, List.of(NO_MATCH, EXACT, NO_MATCH, EXACT, EXACT), ""),
            List.of(4, Status.IN_PROGRESS, List.of(MATCH, EXACT, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(5, Status.IN_PROGRESS, List.of(NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), ""),
            List.of(6, Status.LOSE, List.of(MATCH, NO_MATCH, NO_MATCH, NO_MATCH, NO_MATCH), "It was FAVOR, better luck next time")
    ));

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      var expected = expectedResults.pop();

      assertEquals(expected.get(0), numberOfAttempts);
      assertEquals(expected.get(1), status);
      assertEquals(expected.get(2), response);
      assertEquals(expected.get(3), message);

      displayCallCount.incrementAndGet();
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertEquals(6, displayCallCount.get());
  }

  @Test
  void verifyReadGuessNotCalledAfterWonOnSecondAttempt() {
    var guesses = new LinkedList<String>(List.of("RAPID", "FAVOR", "TESTS"));

    var readGuessCallCount = new AtomicInteger(0);

    Supplier<String> readGuess = () -> {
      readGuessCallCount.incrementAndGet();
      return guesses.pop();
    };

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertFalse(guesses.isEmpty());
    assertEquals(2, readGuessCallCount.get());
  }

  @Test
  void verifyReadGuessNotCalledAfterLossOnSixthAttempt() {
    var guesses = new LinkedList<String>(List.of("AMASS", "RIVER", "MAYOR", "RAPID", "TESTS", "AMASS", "FAVOR"));

    var readGuessCallCount = new AtomicInteger(0);

    Supplier<String> readGuess = () -> {
      readGuessCallCount.incrementAndGet();
      return guesses.pop();
    };

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertFalse(guesses.isEmpty());
    assertEquals(6, readGuessCallCount.get());
  }

  @Test
  void playFirstAttemptInCorrectSpellingWithFAVRO() {
    var guesses = new LinkedList<String>(List.of("FAVRO", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();
    AtomicBoolean displayCalled = new AtomicBoolean(false);

    WordSpellingService wordSpellingService = Mockito.mock(WordSpellingService.class);
    when(wordSpellingService.isSpellingCorrect("FAVRO")).thenReturn(false);
    when(wordSpellingService.isSpellingCorrect("FAVOR")).thenReturn(true);

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      if (numberOfAttempts == 0) {
        assertEquals(0, numberOfAttempts);
        assertEquals(Status.ERROR, status);
        assertEquals("not a word", message);
        displayCalled.set(true);
      }
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertTrue(displayCalled.get());
  }

  @Test
  void playSecondAttemptInCorrectSpellingWithFVROA() {
    var guesses = new LinkedList<String>(List.of("TESTS", "FVROA", "FAVOR"));
    Supplier<String> readGuess = () -> guesses.pop();
    var displayCallCount = new AtomicInteger(0);

    var expectedResults = new LinkedList<List<Object>>(List.of(
      List.of(1, Status.IN_PROGRESS, ""),
      List.of(1, Status.ERROR, "not a word"),
      List.of(2, Status.WON, "Splendid")
    ));

    WordSpellingService wordSpellingService = Mockito.mock(WordSpellingService.class);
    when(wordSpellingService.isSpellingCorrect("TESTS")).thenReturn(true);
    when(wordSpellingService.isSpellingCorrect("FVROA")).thenReturn(false);
    when(wordSpellingService.isSpellingCorrect("FAVOR")).thenReturn(true);

    Display display = (int numberOfAttempts, Status status, List<Wordle.MatchResponse> response, String message) -> {
      var expected = expectedResults.pop();

      assertEquals(expected.get(0), numberOfAttempts);
      assertEquals(expected.get(1), status);
      assertEquals(expected.get(2), message);

      displayCallCount.incrementAndGet();
    };

    Wordle.play("FAVOR", readGuess, display, wordSpellingService);

    assertEquals(3, displayCallCount.get());
  }

  @Test
  void playFirstAttemptNetworkErrorWithFAVRO() {
    Supplier<String> readGuess = () -> "FAVRO";

    WordSpellingService wordSpellingServiceWithError = Mockito.mock(WordSpellingService.class);

    when(wordSpellingServiceWithError.isSpellingCorrect("FAVRO"))
      .thenThrow(new RuntimeException("Network error"));

    assertThrows(RuntimeException.class, () ->
      Wordle.play("FAVOR", readGuess, null, wordSpellingServiceWithError), "Error connecting to the URL: Network error");
  }

  @Test
  void getAWordFromWordListService() {

    WordListService wordListService = Mockito.mock(WordListService.class);
    when(wordListService.getListOfWords())
            .thenReturn(List.of("FAVOR", "RIGOR", "SUGAR", "POWER", "POINT", "PIOUS", "GRIND", "NASTY", "WATER", "AVOID", "PAINT", "ABBEY", "SHIRE", "CYCLE", "SHORT", "WHICH", "YIELD", "AGILE", "BUILD", "BRICK"));

    assertEquals("FAVOR", Wordle.getRandomWord(wordListService, 0));
  }

  @Test
  void getAnotherWordFromListService() {
    WordListService wordListService = Mockito.mock(WordListService.class);
    when(wordListService.getListOfWords())
            .thenReturn(List.of("FAVOR", "RIGOR", "SUGAR", "POWER", "POINT", "PIOUS", "GRIND", "NASTY", "WATER", "AVOID", "PAINT", "ABBEY", "SHIRE", "CYCLE", "SHORT", "WHICH", "YIELD", "AGILE", "BUILD", "BRICK"));

    assertEquals("CYCLE", Wordle.getRandomWord(wordListService, 20));

  }
}



