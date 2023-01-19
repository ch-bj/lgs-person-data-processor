package ch.ejpd.lgs.persondataprocessor.processor.attributeprocessor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import ch.ejpd.lgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import ch.ejpd.lgs.persondataprocessor.configuration.ProcessingFlag;
import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.UUID;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import lombok.NonNull;
import org.assertj.core.util.Lists;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AttributeSearchTermsHashingTest {
  static Stream<Arguments> hashingTest() {
    return Stream.of(
        arguments(
            "Term",
            Attribute.builder().attributeName("AttrName").attributeSourceValue("Value").build()),
        arguments(
            "Term",
            Attribute.builder()
                .attributeName("AttrName")
                .attributeSourceValue("Value")
                .processingFlags(EnumSet.of(ProcessingFlag.HASHED))
                .searchTerms(Lists.newArrayList("Term"))
                .build()),
        arguments(
            "Term",
            Attribute.builder()
                .attributeName("AttrName")
                .attributeSourceValue("Value")
                .processingFlags(EnumSet.of(ProcessingFlag.HASHED))
                .searchTerms(Lists.newArrayList("Term", "Term", "Term"))
                .build()),
        arguments(
            "Term",
            Attribute.builder()
                .attributeName("AttrName")
                .attributeSourceValue("Value")
                .processingFlags(EnumSet.of(ProcessingFlag.NONE))
                .searchTerms(Lists.newArrayList("Term", "Term", "Term"))
                .build()));
  }

  @ParameterizedTest
  @DisplayName("Test encryption / decryption process")
  @MethodSource("hashingTest")
  void testHappyPath(@NonNull final String term, @NonNull final Attribute attribute)
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    String cipherSpec = "RSA/ECB/PKCS1Padding";
    Cipher cipher = Cipher.getInstance(cipherSpec);

    AttributeSearchTermsHashing attributeSearchTermsHashing =
        AttributeSearchTermsHashing.builder()
            .handlerConfiguration(
                HandlerConfiguration.builder()
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_MESSAGE_DIGEST, "SHA-256")
                    .build())
            .build();

    Attribute attributeAfterHashing =
        attributeSearchTermsHashing.process(UUID.randomUUID().toString(), attribute);

    if (attribute.getSearchTerms() != null) {
      assertNotNull(attributeAfterHashing.getSearchTerms());
      assertEquals(
          attribute.getSearchTerms().size(), attributeAfterHashing.getSearchTerms().size());
    }

    String sha256hexTerm =
        Hashing.sha256().hashString(term, StandardCharsets.UTF_8).toString().toUpperCase();

    if (attributeAfterHashing.getSearchTerms() != null) {
      if (attribute.getProcessingFlags().contains(ProcessingFlag.HASHED)) {
        assertTrue(
            attributeAfterHashing.getSearchTerms().stream()
                .allMatch(
                    searchTerm -> {
                      return sha256hexTerm.equals(searchTerm);
                    }));
      } else {
        assertEquals(attribute, attributeAfterHashing);
      }
    }
  }
}
