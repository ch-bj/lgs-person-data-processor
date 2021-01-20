package org.datarocks.lwgs.persondataprocessor.processor.attributeprocessor;

import static org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters.PARAM_KEY_PRIVATE_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.lwgs.persondataprocessor.TestHelper;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.datarocks.lwgs.persondataprocessor.configuration.ProcessingFlag;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.EncryptionDecryptionAndMessageDigestHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AttributeSearchTermsEncryptorTest {
  static HandlerConfiguration handlerConfiguration;

  static Stream<Arguments> encryptionTests() {
    return Stream.of(
        arguments(
            Attribute.builder().attributeName("AttrName").attributeSourceValue("Value").build()),
        arguments(
            Attribute.builder()
                .attributeName("AttrName")
                .attributeSourceValue("Value")
                .processingFlags(EnumSet.of(ProcessingFlag.ENCRYPTED))
                .searchTerms(Lists.newArrayList("Term"))
                .build()),
        arguments(
            Attribute.builder()
                .attributeName("AttrName")
                .attributeSourceValue("Value")
                .processingFlags(EnumSet.of(ProcessingFlag.ENCRYPTED))
                .searchTerms(Lists.newArrayList("Term", "Term", "Term"))
                .build()),
        arguments(
            Attribute.builder()
                .attributeName("AttrName")
                .attributeSourceValue("Value")
                .processingFlags(EnumSet.of(ProcessingFlag.NONE))
                .searchTerms(Lists.newArrayList(RandomStringUtils.random(4096, true, true)))
                .build()),
        arguments(
            Attribute.builder()
                .attributeName("AttrName")
                .attributeSourceValue("Value")
                .processingFlags(EnumSet.of(ProcessingFlag.NONE))
                .searchTerms(TestHelper.generateRandomArguments(1, 100, 4096))
                .build()));
  }

  @BeforeAll
  static void setup() throws NoSuchAlgorithmException {
    KeyPair keyPair = EncryptionDecryptionAndMessageDigestHelper.generateKeyPair("RSA", 2048);
    handlerConfiguration =
        HandlerConfiguration.builder()
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_PUBLIC_KEY,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()))
            .handlerConfigurationItem(
                PARAM_KEY_PRIVATE_KEY,
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()))
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER, "RSA/ECB/PKCS1Padding")
            .build();
  }

  @Test
  void testAttributeSearchTermsEncryptorReturnsNewObject() {
    Attribute attribute =
        Attribute.builder()
            .attributeName("Name")
            .attributeSourceValue("Value")
            .searchTerms(Arrays.asList("aaa", "bbb"))
            .build();

    AttributeSearchTermsEncryptor attributeSearchTermsEncryptor =
        AttributeSearchTermsEncryptor.builder().handlerConfiguration(handlerConfiguration).build();

    Attribute processedAttribute =
        attributeSearchTermsEncryptor.process(UUID.randomUUID().toString(), attribute);

    assertNotSame(attribute, processedAttribute);
  }

  @Test
  void testAttributeSearchTermsDecryptorReturnsNewObject() {
    Attribute attribute =
        Attribute.builder()
            .attributeName("Name")
            .attributeSourceValue("Value")
            .searchTerms(Arrays.asList("aaa", "bbb"))
            .build();

    AttributeSearchTermsDecryptor attributeSearchTermsDecryptor =
        AttributeSearchTermsDecryptor.builder().handlerConfiguration(handlerConfiguration).build();

    Attribute processedAttribute =
        attributeSearchTermsDecryptor.process(UUID.randomUUID().toString(), attribute);

    assertNotSame(attribute, processedAttribute);
  }

  @ParameterizedTest
  @DisplayName("Test encryption / decryption process")
  @MethodSource("encryptionTests")
  void testHappyPath(@NonNull final Attribute attribute) {
    AttributeSearchTermsEncryptor attributeSearchTermsEncryptor =
        AttributeSearchTermsEncryptor.builder().handlerConfiguration(handlerConfiguration).build();

    final Attribute attributeAfterEncryption =
        attributeSearchTermsEncryptor.process(UUID.randomUUID().toString(), attribute);

    assertNotNull(attributeAfterEncryption.getSearchTerms());
    assertEquals(
        attribute.getSearchTerms().size(), attributeAfterEncryption.getSearchTerms().size());

    AttributeSearchTermsDecryptor attributeSearchTermsDecryptor =
        AttributeSearchTermsDecryptor.builder().handlerConfiguration(handlerConfiguration).build();

    Attribute attributeAfterDecryption =
        attributeSearchTermsDecryptor.process(
            UUID.randomUUID().toString(), attributeAfterEncryption);

    assertNotNull(attributeAfterDecryption.getSearchTerms());

    assertEquals(attribute, attributeAfterDecryption);
  }
}
