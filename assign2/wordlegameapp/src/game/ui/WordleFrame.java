package game.ui;

import game.Display;
import game.Wordle;
import game.Wordle.MatchResponse;
import game.Wordle.Status;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.net.*;
import java.io.*;


public class WordleFrame extends JFrame {
  private static JFrame frame;
  private static final int ROW = 6;
  private static final int COLUMN = 5;
  private static int playingRowNumber = 0;
  private static String guessingWord = "";
  private static final JButton[][] boxes = new JButton[ROW][COLUMN];
  private static final JButton guessButton = new JButton("Guess");
  private static CountDownLatch countDownLatch = new CountDownLatch(1);

  public static boolean checkRowFilled(int row) {
    for (int i = 0; i < COLUMN; i++) {
      if (Objects.equals(boxes[row][i].getText(), "")) {
        return false;
      }
    }
    return true;
  }

  public static void rowClear(int row) {
    IntStream.range(0, COLUMN)
            .forEach(column -> boxes[row][column].setText(""));
  }

  public static String getWordAtPlayingRow() {
    return IntStream.range(0, COLUMN)
            .mapToObj(index -> boxes[playingRowNumber][index].getText())
            .collect(Collectors.joining());
  }

  public static void setWordToRow(int row) {
    String finalWord = guessingWord.toUpperCase();

    IntStream.range(0,  finalWord.length())
            .forEach(column -> boxes[row][column].setText(String.valueOf(finalWord.charAt(column))));
  }

  public static Color getBoxColor(MatchResponse response) {
    if (response == MatchResponse.EXACT) {
      return Color.GREEN;
    }

    return (response == MatchResponse.MATCH) ? Color.YELLOW : Color.GRAY;
  }

  public static boolean characterValidation(char letter) {

    return Character.isAlphabetic(letter) && guessingWord.length() < COLUMN;
  }

  public static boolean backSpaceValidation(char letter) {

    return letter == KeyEvent.VK_BACK_SPACE && guessingWord.length() > 0;
  }

  public static boolean getNewGuessingWord(char letter) {
    if (characterValidation(letter)) {
      guessingWord += letter;

      return true;
    }

    if (backSpaceValidation(letter)) {
      guessingWord = guessingWord.substring(0, guessingWord.length() - 1);

      return true;
    }

    return false;
  }

  @Override
  protected void frameInit() {
    super.frameInit();

    setLayout(new GridLayout(ROW + 1, COLUMN, 10, 10));
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    for (int i = 0; i < ROW; i++) {
      for (int j = 0; j < COLUMN; j++) {
        boxes[i][j] = new JButton();

        if (i != 0) {
          boxes[i][j].setEnabled(false);
        }

        getContentPane().add(boxes[i][j]);
      }
    }

    guessButton.setEnabled(false);
    guessButton.setFocusable(true);
    getContentPane().add(guessButton);
  }

  private static class CharacterHandler extends KeyAdapter {
    @Override
    public void keyTyped(KeyEvent event) {
      char letter = event.getKeyChar();

      if (getNewGuessingWord(letter)) {

        rowClear(playingRowNumber);

        setWordToRow(playingRowNumber);

        guessButton.setEnabled(checkRowFilled(playingRowNumber));
      }
    }
  }

  private static class PressButtonLatch implements Runnable {
    private final CountDownLatch countDownLatch;

    PressButtonLatch(CountDownLatch latch) {
      countDownLatch = latch;
    }

    @Override
    public void run() {
      guessButton.addActionListener(e -> {
        JButton button = (JButton) e.getSource();

        button.setEnabled(false);

        countDownLatch.countDown();
      });
    }
  }

  private static class ReadGuess implements Supplier<String> {
    @Override
    public String get() {
      new Thread(new PressButtonLatch(countDownLatch)).start();

      try {
        countDownLatch.await();
      } catch (InterruptedException ignored) {}

      return getWordAtPlayingRow();
    }
  }


  private static class DisplayWordle implements Display {
    @Override
    public void call(int numberOfAttempts, Status status, List<MatchResponse> response, String message) {
      int row = numberOfAttempts - 1;

      IntStream.range(0, COLUMN)
              .forEach(column -> {
                boxes[row][column].setBackground(getBoxColor(response.get(column)));
                boxes[row][column].setOpaque(true);
                boxes[row][column].setBorderPainted(false);
              });

      if (!Objects.equals(message, "")) {
        JOptionPane.showMessageDialog(boxes[3][2], message);
      }

      if ((status == Status.IN_PROGRESS)) {
        IntStream.range(0, COLUMN)
                .forEach(column -> boxes[row + 1][column].setEnabled(true));
      }
      else {
        frame.setFocusable(false);
      }

      playingRowNumber++;

      guessingWord = "";

      countDownLatch = new CountDownLatch(1);
    }
  }

  public static List<String> split(String str){

    return Stream.of(str.split(", "))
            .map (String::new)
            .collect(Collectors.toList());
  }

  public static List<String> getListOfWords() throws Exception {
    URL url = new URL("https://agilec.cs.uh.edu/words");
    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

    String inputLine;
    String line = null;

    while ((inputLine = in.readLine()) != null) {
      line = inputLine.substring(1, inputLine.length()-1);
    }

    in.close();

    if (line == null) {
      throw new RuntimeException("Empty list of words");
    }
    else {
      return split(line);
    }
  }

  public static void main(String[] args) throws Exception {
    String target = getListOfWords().get(new Random().nextInt(getListOfWords().size()));

    frame = new WordleFrame();

    frame.setTitle("Wordle Game");

    frame.setFocusable(true);
    frame.addKeyListener(new CharacterHandler());

    frame.setSize(500, 500);
    frame.setVisible(true);

    DisplayWordle display = new DisplayWordle();

    ReadGuess readGuess = new ReadGuess();

    Wordle.play(target, readGuess, display);
  }
}