package game;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class SpellCheckerTest {
  @Test
  void getResponseFromURLAndReturnTextSpellChecker() throws Exception {
    AgileCSSpellChecker spy = Mockito.spy(new AgileCSSpellChecker());

    assertNotSame("", spy.getResponse("FAVOR"));
  }

  @Test
  void parseTrueSpellChecker() {
    assertTrue(AgileCSSpellChecker.parse("true"));
  }

  @Test
  void parseFalseSpellChecker() {
    assertFalse(AgileCSSpellChecker.parse("false"));
  }

  @Test
  void isSpellingCorrectReturnResultSpellChecker() throws Exception {
    AgileCSSpellChecker spy = Mockito.spy(new AgileCSSpellChecker());
    doReturn("true").when(spy).getResponse("FAVOR");

    assertTrue(spy.isSpellingCorrect("FAVOR"));
  }

  @Test
  void isSpellingCorrectReturnExceptionNetworkErrorSpellChecker() throws Exception {
    AgileCSSpellChecker spy = Mockito.spy(new AgileCSSpellChecker());
    doThrow(new RuntimeException("Network Error")).when(spy).getResponse("FAVOR");

    assertEquals("Network Error", assertThrows(RuntimeException.class, () -> spy.isSpellingCorrect("FAVOR")).getMessage());
  }
}
