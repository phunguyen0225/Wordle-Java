package game;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AgileCSSSampleWords implements SampleWords{
  public String getResponse() throws Exception {
    URL theURL = new URL("https://agilec.cs.uh.edu/words");

    return new Scanner(theURL.openStream(), StandardCharsets.UTF_8).useDelimiter("\\A").next();
  }

  public static List<String> extractWords(String theString) {
    Matcher matcher = Pattern.compile("[a-zA-Z]+").matcher(theString);

    return matcher.results()
      .map(value -> matcher.group())
      .collect(Collectors.toList());
  }

  @Override
  public List<String> fetchWords() {
    try {
      return extractWords(getResponse());

    } catch (Exception e) {
      throw new RuntimeException("Network Error");
    }
  }
}