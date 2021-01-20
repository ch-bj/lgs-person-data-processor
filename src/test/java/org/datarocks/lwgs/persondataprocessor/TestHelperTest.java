package org.datarocks.lwgs.persondataprocessor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class TestHelperTest {
  @Test
  void testGenerateRandomArguments() {
    List<String> argumentList = TestHelper.generateRandomArguments(10, 10, 1024);
    assertEquals(10, argumentList.size());

    argumentList = TestHelper.generateRandomArguments(1, 100, 1024);
    assertTrue(argumentList.size() > 0);
    assertTrue(argumentList.size() < 100);
  }
}
