package org.datarocks.lwgs.persondataprocessor.transformer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.lwgs.persondataprocessor.TestHelper;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.datarocks.lwgs.persondataprocessor.configuration.model.SupportedAttributes;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;
import org.datarocks.lwgs.persondataprocessor.model.EventType;
import org.datarocks.lwgs.persondataprocessor.model.GBPersonEvent;
import org.datarocks.lwgs.persondataprocessor.model.PersonType;
import org.datarocks.lwgs.persondataprocessor.transformer.exception.InvalidJsonStructure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GBPersonRequestJsonDeserializerTest {
  private static final String SUPPORTED_ATTRIBUTES =
      "{\"naturalPerson\":[{\"attributeName\":\"vornamen\",\"attributeType\":\"String\",\"required\":true},{\"attributeName\":\"rufname\",\"attributeType\":\"String\",\"required\":true},{\"attributeName\":\"ledigerName\",\"attributeType\":\"String\",\"required\":true},{\"attributeName\":\"geburtsjahr\",\"attributeType\":\"Integer\",\"required\":true},{\"attributeName\":\"geburtsmonat\",\"attributeType\":\"Integer\",\"required\":true},{\"attributeName\":\"geburtstag\",\"attributeType\":\"Integer\",\"required\":true},{\"attributeName\":\"buergerort\",\"attributeType\":\"String\",\"required\":true}],\"legalPerson\":[]}";

  private static final String CORRELATION_ID = UUID.randomUUID().toString();

  private static HandlerConfiguration handlerConfiguration;
  private static GBPersonRequestJsonDeserializer gbPersonRequestJsonDeserializer;

  static Stream<Arguments> personReaderTest() {
    return Stream.of(
        arguments(
            "{\"metaData\":{\"personType\":\"NATUERLICHE_PERSON\",\"eventType\":\"INSERT\"},\"natuerlichePerson\":{\"name\":\"Smith\",\"vorname\":\"John\",\"jahrgang\":\"1970\"}}",
            GBPersonEvent.builder()
                .personType(PersonType.NATUERLICHE_PERSON)
                .eventType(EventType.INSERT)
                .attributes(
                    Arrays.asList(
                        Attribute.builder()
                            .attributeName("name")
                            .attributeSourceValue("Smith")
                            .build(),
                        Attribute.builder()
                            .attributeName("vorname")
                            .attributeSourceValue("John")
                            .build(),
                        Attribute.builder()
                            .attributeName("jahrgang")
                            .attributeSourceValue("1970")
                            .build()))
                .build()),
        arguments(
            "{\"metaData\":{\"personType\":\"JURISTISCHE_PERSON\",\"eventType\":\"INSERT\"},\"juristischePerson\":{\"name\":\"A company\",\"sitz\":\"Sometown\"}}",
            GBPersonEvent.builder()
                .personType(PersonType.JURISTISCHE_PERSON)
                .eventType(EventType.INSERT)
                .attributes(
                    Arrays.asList(
                        Attribute.builder()
                            .attributeName("name")
                            .attributeSourceValue("A company")
                            .build(),
                        Attribute.builder()
                            .attributeName("sitz")
                            .attributeSourceValue("Sometown")
                            .build()))
                .build()),
        arguments(
            "{\"metaData\":{\"personType\":\"GEMEINSCHAFT\",\"eventType\":\"INSERT\"},\"gemeinschaft\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}",
            GBPersonEvent.builder()
                .personType(PersonType.GEMEINSCHAFT)
                .eventType(EventType.INSERT)
                .attributes(
                    Arrays.asList(
                        Attribute.builder()
                            .attributeName("name")
                            .attributeSourceValue("A community")
                            .build(),
                        Attribute.builder()
                            .attributeName("gemeinschaftArt")
                            .attributeSourceValue("EinfacheGesellschaft")
                            .build()))
                .build()));
  }

  static Stream<Arguments> invalidJsonStructures() {
    return Stream.of(
        arguments(
            "{\"metaData\":{\"personType\":\"GEMEINSCHAFT\",\"eventType\":\"INSERT\"},\"gemeinschaftWrong\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{\"personType\":\"NATUERLICHE_PERSON\",\"eventType\":\"INSERT\"},\"natuerlichePersonWrong\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{\"personType\":\"GEMEINSCHAFT\",\"eventType\":\"INSERT\"},\"juristischePerson\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{\"personType\":\"JURISTISCHE_PERSON\",\"eventType\":\"INSERT\"},\"gemeinschaft\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{\"personType\":\"NATUERLICHE_PERSON\",\"eventType\":\"INSERT\"},\"gemeinschaft\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{\"personType\":\"NATUERLICHE_PERSON_WRONG\",\"eventType\":\"INSERT\"},\"natuerlichePerson\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{\"personType\":\"NATUERLICHE_PERSON\",\"eventType\":\"INSERT_WRONG\"},\"natuerlichePerson\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{},\"natuerlichePerson\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":null,\"natuerlichePerson\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{\"eventType\":\"INSERT\"},\"natuerlichePerson\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(
            "{\"metaData\":{\"personType\":\"NATUERLICHE_PERSON\"},\"natuerlichePerson\":{\"name\":\"A community\",\"gemeinschaftArt\":\"EinfacheGesellschaft\"}}"),
        arguments(""),
        arguments("{"),
        arguments("abc"),
        arguments("[\"a\",\"b\"]"));
  }

  @BeforeAll
  static void setup() throws IOException {
    handlerConfiguration =
        HandlerConfiguration.builder()
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_SUPPORTED_ATTRIBUTES,
                SupportedAttributes.fromJson(
                    TestHelper.readSupportedAttributesScheme(),
                    TestHelper.readSupportedAttributes()))
            .build();

    gbPersonRequestJsonDeserializer =
        GBPersonRequestJsonDeserializer.builder()
            .handlerConfiguration(handlerConfiguration)
            .build();
  }

  @ParameterizedTest
  @DisplayName("Test data model deserialization.")
  @MethodSource("personReaderTest")
  void readDataModel(String json, GBPersonEvent gbPersonEvent) {
    GBPersonEvent parsedGBPersonEvent =
        gbPersonRequestJsonDeserializer.processImpl(CORRELATION_ID, json);
    assertEquals(gbPersonEvent, parsedGBPersonEvent);
  }

  @ParameterizedTest
  @DisplayName("Test exception on invalid json structure-")
  @MethodSource("invalidJsonStructures")
  void testExceptionOnInvalidJasonStructure(String json) {
    assertThrows(
        InvalidJsonStructure.class,
        () -> gbPersonRequestJsonDeserializer.process(CORRELATION_ID, json));
  }
}
