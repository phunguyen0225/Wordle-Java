package game.ui;

import game.Display;
import game.Wordle;
import game.Wordle.MatchResponse;
import game.Wordle.Status;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class WordleFrame extends JFrame {
  private static final int ROW = 6;
  private static final int COLUMN = 5;
  private static int playingRowNumber = 0;
  private static final JButton[][] boxes = new JButton[ROW][COLUMN];
  private static final JButton guessButton = new JButton();
  private static final JTextField textBox = new JTextField();
  private static CountDownLatch countDownLatch = new CountDownLatch(1);

  public static boolean checkRowFilled(int row) {
    for (int i = 0; i < COLUMN; i++) {
      if (boxes[row][i].getText() == "") {
        return false;
      }
    }
    return true;
  }

  public static void rowClear(int row) {
    IntStream.range(0, COLUMN)
      .forEach(column -> boxes[row][column].setText(""));
  }

  public static String getWord(int row) {
    return IntStream.range(0, COLUMN)
      .mapToObj(index -> boxes[row][index].getText())
      .collect(Collectors.joining());
  }

  public static void colorBox(int row, int column, MatchResponse response) {
    if (response == MatchResponse.EXACT) {
      boxes[row][column].setBackground(Color.GREEN);
      return;
    }

    if ((response == MatchResponse.MATCH)) {
      boxes[row][column].setBackground(Color.YELLOW);
    } else {
      boxes[row][column].setBackground(Color.GRAY);
    }
  }

  @Override
  protected void frameInit() {
    super.frameInit();

    setTitle("Wordle Game");

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

    textBox.addKeyListener(new CharacterHandler());
    textBox.addActionListener(new TextBoxHandler());
    getContentPane().add(textBox);

    guessButton.setEnabled(false);
    guessButton.setText("Guess");
    getContentPane().add(guessButton);
   }

   private static class CharacterHandler extends KeyAdapter {
     @Override
     public void keyTyped(KeyEvent e) {
       char c = e.getKeyChar();

       if(textBox.getText().length() > 4 || !(Character.isAlphabetic(c) || c==KeyEvent.VK_BACK_SPACE)) {
         e.consume();
       }
     }
   }

  private static class TextBoxHandler implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
      JTextField text = (JTextField) actionEvent.getSource();

      int row = playingRowNumber;
      rowClear(row);

      String word = text.getText().toUpperCase();

      IntStream.range(0,  word.length())
        .forEach(column -> boxes[row][column].setText(String.valueOf(word.charAt(column))));

      guessButton.setEnabled(checkRowFilled(row));
    }
  }

  static class PressButtonLatch implements Runnable {
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

  static Supplier<String> readGuess = () -> {
    new Thread(new PressButtonLatch(countDownLatch)).start();

    try {
      countDownLatch.await();

    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return getWord(playingRowNumber);
  };

  static Display display = (int numberOfAttempts, Wordle.Status status, List<Wordle.MatchResponse> response, String message) -> {
    int row = numberOfAttempts - 1;

    IntStream.range(0, COLUMN)
      .forEach(column -> colorBox(row, column, response.get(column)));

    if (message != "") {
      JOptionPane.showMessageDialog(boxes[3][2], message);
    }

    if ((status == Status.IN_PROGRESS)) {
      IntStream.range(0, COLUMN)
        .forEach(column -> boxes[row + 1][column].setEnabled(true));
    } else {
      textBox.setEnabled(false);
    }

    textBox.setText("");
    playingRowNumber++;

    countDownLatch = new CountDownLatch(1);
  };

  public static void main(String[] args) {
    JFrame frame = new WordleFrame();

    frame.setSize(500, 500);
    frame.setVisible(true);

    Wordle.play("FAVOR", readGuess, display);
  }
}