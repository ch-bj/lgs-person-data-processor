package org.datarocks.lwgs.persondataprocessor.processor.stringprocessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.stream.Stream;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.banzai.exception.RequiredParameterMissing;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.exception.MessageDigestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HashingSingleItemProcessorTest {
  private static final String CORRELATION_ID = UUID.randomUUID().toString();

  static Stream<Arguments> hashingTest() throws NoSuchAlgorithmException {
    return Stream.of(
        arguments(
            "SHA-256", "Test", "532EAABD9574880DBF76B9B8CC00832C20A6EC113D682299550D7A6E0F345E25"),
        arguments(
            "SHA-512",
            "Test",
            "C6EE9E33CF5C6715A1D148FD73F7318884B41ADCB916021E2BC0E800A5C5DD97F5142178F6AE88C8FDD98E1AFB0CE4C8D2C54B5F37B30B7DA1997BB33B0B8A31"),
        arguments("SHA-1", "Test", "640AB2BAE07BEDC4C163F679A746F7AB7FB5D1FA"),
        arguments("MD5", "Test", "0CBC6611F5540BD0809A388DC95A615B"));
  }

  @Test
  void exceptionOnRequiredParameterMissing() {
    HandlerConfiguration handlerConfiguration = HandlerConfiguration.builder().build();

    HashingSingleItemProcessor hashingSingleItemProcessor =
        HashingSingleItemProcessor.builder().handlerConfiguration(handlerConfiguration).build();

    assertThrows(
        RequiredParameterMissing.class,
        () -> hashingSingleItemProcessor.process(CORRELATION_ID, "Test"));
  }

  @ParameterizedTest
  @DisplayName("Test hashing")
  @MethodSource("hashingTest")
  void testHashing(String messageDigest, String input, String hashed) {
    HandlerConfiguration handlerConfiguration =
        HandlerConfiguration.builder()
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_MESSAGE_DIGEST, messageDigest)
            .build();

    HashingSingleItemProcessor hashingSingleItemProcessor =
        HashingSingleItemProcessor.builder().handlerConfiguration(handlerConfiguration).build();

    assertEquals(hashed, hashingSingleItemProcessor.process(CORRELATION_ID, input));
  }

  @Test
  void unsupportedAlgorithmException() {
    HandlerConfiguration handlerConfiguration =
        HandlerConfiguration.builder()
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_MESSAGE_DIGEST, "WrongAlgorithm")
            .build();

    HashingSingleItemProcessor hashingSingleItemProcessor =
        HashingSingleItemProcessor.builder().handlerConfiguration(handlerConfiguration).build();

    assertThrows(
        MessageDigestException.class,
        () -> hashingSingleItemProcessor.process(CORRELATION_ID, "Test"));
  }
}
