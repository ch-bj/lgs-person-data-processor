package org.datarocks.lwgs.persondataprocessor.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProcessingFlagTest {

  @Test
  void testProcessingFlag() {
    ProcessingFlag processingFlag = ProcessingFlag.ENCRYPTED;
    assertEquals(
        processingFlag.getAttributeClassValue(),
        ProcessingFlag.valueOf(processingFlag.name()).getAttributeClassValue());
  }
}
