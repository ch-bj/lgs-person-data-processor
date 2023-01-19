package ch.ejpd.lgs.persondataprocessor.processor.attributeprocessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import java.util.UUID;
import java.util.stream.Stream;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AttributePhoneticallyNormalizeAttributeValueTest {
  static Stream<Arguments> phoneticalNormalizationTests() {
    return Stream.of(
        arguments("", ""),
        arguments("ABC", "ABC"),
        arguments("Müller", "MULLER"),
        arguments("Möller", "MOLLER"),
        arguments("Ütrecht", "UTRECHT"),
        arguments("Räbe", "RABE"),
        arguments("Äsche", "ASCHE"),
        arguments("écrire", "ECRIRE"),
        arguments("vérité", "VERITE"),
        arguments("père", "PERE"),
        arguments("traître", "TRAITRE"),
        arguments("traître", "TRAITRE"),
        arguments("Äbi Müller", "ABI MULLER"));
  }

  private static Attribute buildAttribute(String attributeValue) {
    return Attribute.builder().attributeName("A name").attributeSourceValue(attributeValue).build();
  }

  @ParameterizedTest
  @DisplayName("Test phonetical normalization")
  @MethodSource("phoneticalNormalizationTests")
  void testPhoneticalNormalization(String beforeNormalization, String afterNormalization) {
    AttributePhoneticallyNormalizeAttributeValue attributePhoneticallyNormalizeAttributeValue =
        AttributePhoneticallyNormalizeAttributeValue.builder()
            .handlerConfiguration(HandlerConfiguration.builder().build())
            .build();

    Attribute attributeBeforeNormalization = buildAttribute(beforeNormalization);
    Attribute result =
        attributePhoneticallyNormalizeAttributeValue.processImpl(
            UUID.randomUUID().toString(), attributeBeforeNormalization);

    assertEquals(afterNormalization, result.getAttributeValue());
  }
}
