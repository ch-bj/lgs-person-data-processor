package org.datarocks.lwgs.persondataprocessor.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import org.datarocks.banzai.processor.AbstractSplitProcessor;
import org.datarocks.banzai.processor.PassTroughProcessor;
import org.junit.jupiter.api.Test;

class AbstractSplitProcessorTest {
  @Test
  void testWithNullSingleItemProcessor() {
    AbstractSplitProcessor<String> abstractSplitProcessor =
        AbstractSplitProcessorImpl.builder().build();

    List<String> result = abstractSplitProcessor.process(UUID.randomUUID().toString(), "Test");
    assertEquals(1, result.size());
    assertEquals("Test", result.get(0));
  }

  @Test
  void testWithDefinedSingleItemProcessor() {
    AbstractSplitProcessor<String> abstractSplitProcessor1 =
        AbstractSplitProcessorImpl.builder().build();

    PassTroughProcessor<String> abstractSplitProcessor2 =
        PassTroughProcessor.<String>builder().build();

    abstractSplitProcessor1.setNextProcessor(abstractSplitProcessor2);

    List<String> result = abstractSplitProcessor1.process(UUID.randomUUID().toString(), "Test");
    assertEquals(1, result.size());
    assertEquals("Test", result.get(0));
  }
}
