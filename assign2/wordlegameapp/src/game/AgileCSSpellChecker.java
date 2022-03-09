package game;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class AgileCSSpellChecker implements SpellChecker {

  public String getResponse(String word) throws Exception {
    URL theURL = new URL("http://agilec.cs.uh.edu/spell?check=" + word);

    return new Scanner(theURL.openStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
  }

  static boolean parse(String text) {
    return Objects.equals(text, "true");
  }

  @Override
  public boolean isSpellingCorrect(String word) {
    try {
      return parse(getResponse(word));
    } catch (Exception e) {
      throw new RuntimeException("Network Error");
    }
  }
}
