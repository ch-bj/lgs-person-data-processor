package org.datarocks.lwgs.persondataprocessor.processor.stringprocessor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.banzai.exception.RequiredParameterMissing;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.junit.jupiter.api.Test;

class EncryptionSingleItemProcessorTest {

  private static final String CORRELATION_ID = UUID.randomUUID().toString();

  @Test
  void testExceptionOnAllMissingMandatoryParameter() {
    final EncryptionSingleItemProcessor encryptionSingleItemProcessor1 =
        EncryptionSingleItemProcessor.builder()
            .handlerConfiguration(HandlerConfiguration.builder().build())
            .build();
    assertThrows(
        RequiredParameterMissing.class,
        () -> encryptionSingleItemProcessor1.process(CORRELATION_ID, "String to be encrypted"));

    final EncryptionSingleItemProcessor encryptionSingleItemProcessor2 =
        EncryptionSingleItemProcessor.builder()
            .handlerConfiguration(
                HandlerConfiguration.builder()
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER, "RSA")
                    .build())
            .build();

    assertThrows(
        RequiredParameterMissing.class,
        () -> encryptionSingleItemProcessor2.process(CORRELATION_ID, "String to be encrypted"));

    final EncryptionSingleItemProcessor encryptionSingleItemProcessor3 =
        EncryptionSingleItemProcessor.builder()
            .handlerConfiguration(
                HandlerConfiguration.builder()
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER, "RSA")
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_PUBLIC_KEY, "")
                    .build())
            .build();

    assertThrows(
        RequiredParameterMissing.class,
        () -> encryptionSingleItemProcessor2.process(CORRELATION_ID, "String to be encrypted"));
  }
}
