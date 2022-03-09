package game;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class SampleWordsTest {
  @Test
  void getResponseFromURLAndReturnStringSampleWords() throws Exception {
    AgileCSSSampleWords spy = Mockito.spy(new AgileCSSSampleWords());

    assertNotSame("", spy.getResponse());
  }

  @Test
  void splitStringReturnListFromSampleWords() {
    assertEquals(List.of("FAVOR", "TESTS", "SKILL"), AgileCSSSampleWords.extractWords("[FAVOR, TESTS, SKILL]"));
  }

  @Test
  void fetchWordsReturnListSampleWords() throws Exception {
    AgileCSSSampleWords spy = Mockito.spy(new AgileCSSSampleWords());
    doReturn("[FAVOR, RIGOR, SUGAR, POWER, POINT, PIOUS]").when(spy).getResponse();

    assertEquals(List.of("FAVOR", "RIGOR", "SUGAR", "POWER", "POINT", "PIOUS"), spy.fetchWords());
  }

  @Test
  void fetchWordsReturnExceptionNetworkErrorSampleWords() throws Exception {
    AgileCSSSampleWords spy = Mockito.spy(new AgileCSSSampleWords());
    doThrow(new RuntimeException("Network Error")).when(spy).getResponse();

    assertEquals("Network Error", assertThrows(RuntimeException.class, spy::fetchWords).getMessage());
  }
}
