package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Wordle {
  final int WORD_SIZE = 5;
  final int MAX_TRIES = 6;
  enum MatchResponse {EXACT, MATCH, NO_MATCH}
  enum Status {WON, LOSE, IN_PROGRESS, ERROR}

  private static long countPositionalMatches(String target, String guess, char letter) {
    return  IntStream.range(0, WORD_SIZE)
      .filter(index -> target.charAt(index) == letter)
      .filter(index -> guess.charAt(index) == target.charAt(index))
      .count();
  }

  private static long countNumberOfOccurrencesUntilPosition(int position, String word, char letter) {
    return  IntStream.rangeClosed(0, position)
      .filter(index -> word.charAt(index) == letter)
      .count();
  }

  private static MatchResponse tallyForPosition(int position, String target, String guess) {
    if (target.charAt(position) == guess.charAt(position)) {
      return MatchResponse.EXACT;
    }

    char theLetter = guess.charAt(position);
    long nonExactPosition = WORD_SIZE - countPositionalMatches(target, guess, theLetter) - 1;

    return (countNumberOfOccurrencesUntilPosition((int) nonExactPosition, target, theLetter) >= countNumberOfOccurrencesUntilPosition(position, guess, theLetter))
      ? MatchResponse.MATCH : MatchResponse.NO_MATCH;
  }

  private static void verifyLength(String target, String guess) {
    if (target.length() != guess.length()) {
      throw new RuntimeException("Invalid guess");
    }
  }

  static List<MatchResponse> tally(String target, String guess) {
    verifyLength(target, guess);

    return IntStream.range(0, WORD_SIZE)
      .mapToObj(index -> tallyForPosition(index, target, guess))
      .collect(Collectors.toList());
  }

  private static Status determineStatus(int attempt, List<MatchResponse> response) {
    if (Collections.frequency(response, MatchResponse.EXACT) == WORD_SIZE) {
      return Status.WON;
    }

    return attempt == MAX_TRIES ? Status.LOSE : Status.IN_PROGRESS;
  }

  private static String getMessage(int attempt, Status status, String target) {
    List<String> winMessage = List.of("Amazing", "Splendid", "Awesome", "Yah", "Yah", "Yah");

    if (status == Status.WON) {
      return winMessage.get(attempt - 1);
    }

    return status == Status.LOSE ? String.format("It was %s, better luck next time", target) : "";
  }

  static void play(String target, Supplier<String> readGuess, Display display, SpellChecker spellChecker) {
    for (int attempts = 1; attempts <= MAX_TRIES; attempts++) {

      String guess = readGuess.get();

      List<MatchResponse> response = null;
      Status status = Status.ERROR;
      String message = "not a word";
      boolean checkSpellingCorrectFlag = false;
      boolean checkNetworkErrorFlag = false;

      try {
        checkSpellingCorrectFlag = spellChecker.isSpellingCorrect(guess);

      } catch (Exception e) {
        attempts = attempts - 1;
        message = e.getMessage();
        checkNetworkErrorFlag = true;
      }

      if (!checkNetworkErrorFlag) {
        if (checkSpellingCorrectFlag) {

          response = tally(target, guess);
          status = determineStatus(attempts, response);
          message = getMessage(attempts, status, target);

        } else {
          attempts = attempts - 1;
        }
      }

      display.call(attempts, status, response, message);

      if (status == Status.WON) {
        break;
      }
    }
  }

  static String getRandomWord(SampleWords wordListService) {
    List<String> words = new ArrayList<>(wordListService.fetchWords());
    Collections.shuffle(words);

    return words.get(0);
  }
}

