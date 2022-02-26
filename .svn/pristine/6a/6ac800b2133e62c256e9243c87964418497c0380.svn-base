package game;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public interface Wordle {
  final int WORD_SIZE = 5;
  enum MatchResponse {EXACT, MATCH, NO_MATCH}
  enum Status {WON, LOSE, IN_PROGRESS}
  final List<String> winMessage = List.of("Amazing", "Splendid", "Awesome", "Yah", "Yah", "Yah");

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

  public static List<MatchResponse> tally(String target, String guess) {
    if (target.length() != guess.length()) {
      throw new RuntimeException("Invalid guess");
    }

    return IntStream.range(0, WORD_SIZE)
      .mapToObj(index -> tallyForPosition(index, target, guess))
      .collect(Collectors.toList());
  }

  private static Status getStatus(List<MatchResponse> response) {
    return Collections.frequency(response, MatchResponse.EXACT) == WORD_SIZE ? Status.WON : Status.IN_PROGRESS;
  }

  private static String getMessage(int attempt, Status status) {
    if (status == Status.WON) {
      return winMessage.get(attempt - 1);
    }

    return status == Status.LOSE ? "It was FAVOR, better luck next time" : "";
  }

  public static void play(String target, Supplier<String> readGuess, Display display) {
    int attemptCount = 0;
    Status status = Status.IN_PROGRESS;
    
    while (status == Status.IN_PROGRESS) {
      attemptCount++;

      String guess = readGuess.get();
      var response = tally(target, guess);
      status = getStatus(response);

      if (attemptCount == 6) {
        status = status == Status.IN_PROGRESS ? Status.LOSE : status;
      }

      display.call(attemptCount, status, response, getMessage(attemptCount, status));
    }
  }
}

