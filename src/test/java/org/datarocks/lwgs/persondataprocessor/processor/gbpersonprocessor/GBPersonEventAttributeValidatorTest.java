package org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.banzai.event.ProcessorEventListener;
import org.datarocks.banzai.exception.HandlerConfigurationMissingException;
import org.datarocks.banzai.exception.RequiredParameterMissing;
import org.datarocks.lwgs.persondataprocessor.TestHelper;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.datarocks.lwgs.persondataprocessor.configuration.ProcessingFlag;
import org.datarocks.lwgs.persondataprocessor.configuration.model.SupportedAttributes;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;
import org.datarocks.lwgs.persondataprocessor.model.EventType;
import org.datarocks.lwgs.persondataprocessor.model.GBPersonEvent;
import org.datarocks.lwgs.persondataprocessor.model.PersonType;
import org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.event.InvalidAttributeDroppedProcessorEvent;
import org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.exception.UnsupportedPersonTypeException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

class GBPersonEventAttributeValidatorTest {
  private static String CORRELATION_ID = UUID.randomUUID().toString();

  static ProcessorEventListener processorEventListener;
  static HandlerConfiguration handlerConfigurationMerge;
  static HandlerConfiguration handlerConfigurationNoMerge;

  static List<Attribute> validAttributes = new ArrayList<>();
  static List<Attribute> invalidAttributes = new ArrayList<>();

  static @Captor ArgumentCaptor<InvalidAttributeDroppedProcessorEvent> processorEventArgumentCaptor;

  @BeforeAll
  static void setup() throws IOException {
    processorEventListener = Mockito.mock(ProcessorEventListener.class);
    handlerConfigurationMerge =
        HandlerConfiguration.builder()
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_SUPPORTED_ATTRIBUTES,
                SupportedAttributes.fromJson(
                    TestHelper.readSupportedAttributesScheme(),
                    TestHelper.readSupportedAttributes()))
            .build();

    handlerConfigurationNoMerge =
        HandlerConfiguration.builder()
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_MERGE_ATTRIBUTES, "false")
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_SUPPORTED_ATTRIBUTES,
                SupportedAttributes.fromJson(
                    TestHelper.readSupportedAttributesScheme(),
                    TestHelper.readSupportedAttributes()))
            .build();

    validAttributes.add(
        Attribute.builder()
            .attributeName("egpId")
            .attributeSourceValue("egpId")
            .processingFlags(EnumSet.of(ProcessingFlag.HASHED, ProcessingFlag.ENCRYPTED))
            .build());

    validAttributes.add(
        Attribute.builder()
            .attributeName("name")
            .attributeSourceValue("nameValue")
            .processingFlags(
                EnumSet.of(
                    ProcessingFlag.PHONETICALLY_NORMALIZED,
                    ProcessingFlag.EDITING_DISTANCE,
                    ProcessingFlag.HASHED,
                    ProcessingFlag.ENCRYPTED))
            .build());
    validAttributes.add(
        Attribute.builder()
            .attributeName("vorname")
            .attributeSourceValue("vornameValue")
            .processingFlags(
                EnumSet.of(
                    ProcessingFlag.PHONETICALLY_NORMALIZED,
                    ProcessingFlag.EDITING_DISTANCE,
                    ProcessingFlag.HASHED,
                    ProcessingFlag.ENCRYPTED))
            .build());

    invalidAttributes.add(
        Attribute.builder()
            .attributeName("invalidName")
            .attributeSourceValue("invalidNameValue")
            .processingFlags(EnumSet.of(ProcessingFlag.NONE))
            .build());
    invalidAttributes.add(
        Attribute.builder()
            .attributeName("invalidName2")
            .attributeSourceValue("invalidName2Value")
            .processingFlags(EnumSet.of(ProcessingFlag.NONE))
            .build());

    processorEventArgumentCaptor =
        ArgumentCaptor.forClass(InvalidAttributeDroppedProcessorEvent.class);
  }

  static Stream<Arguments> validatorTest() throws NoSuchAlgorithmException {
    return Stream.of(
        arguments(
            Lists.newArrayList(
                Attribute.builder()
                    .attributeName("egpId")
                    .attributeSourceValue("egpId")
                    .processingFlags(EnumSet.of(ProcessingFlag.HASHED, ProcessingFlag.ENCRYPTED))
                    .build(),
                Attribute.builder()
                    .attributeName("name")
                    .attributeSourceValue("name vorname")
                    .processingFlags(
                        EnumSet.of(
                            ProcessingFlag.PHONETICALLY_NORMALIZED,
                            ProcessingFlag.EDITING_DISTANCE,
                            ProcessingFlag.HASHED,
                            ProcessingFlag.ENCRYPTED))
                    .build(),
                Attribute.builder()
                    .attributeName("jahrgang")
                    .attributeSourceValue("jahrgang")
                    .build()),
            GBPersonEvent.builder()
                .personType(PersonType.NATUERLICHE_PERSON)
                .eventType(EventType.INSERT)
                .attributes(
                    Lists.newArrayList(
                        Attribute.builder()
                            .attributeName("egpId")
                            .attributeSourceValue("egpId")
                            .build(),
                        Attribute.builder()
                            .attributeName("name")
                            .attributeSourceValue("name")
                            .build(),
                        Attribute.builder()
                            .attributeName("vorname")
                            .attributeSourceValue("vorname")
                            .build(),
                        Attribute.builder()
                            .attributeName("jahrgang")
                            .attributeSourceValue("jahrgang")
                            .build()))
                .build()),
        arguments(
            Lists.newArrayList(
                Attribute.builder()
                    .attributeName("egpId")
                    .attributeSourceValue("egpId")
                    .processingFlags(EnumSet.of(ProcessingFlag.HASHED, ProcessingFlag.ENCRYPTED))
                    .build(),
                Attribute.builder()
                    .attributeName("name")
                    .attributeSourceValue("name")
                    .processingFlags(
                        EnumSet.of(
                            ProcessingFlag.PHONETICALLY_NORMALIZED,
                            ProcessingFlag.EDITING_DISTANCE,
                            ProcessingFlag.HASHED,
                            ProcessingFlag.ENCRYPTED))
                    .build(),
                Attribute.builder().attributeName("sitz").attributeSourceValue("sitz").build()),
            GBPersonEvent.builder()
                .personType(PersonType.JURISTISCHE_PERSON)
                .eventType(EventType.INSERT)
                .attributes(
                    Lists.newArrayList(
                        Attribute.builder()
                            .attributeName("egpId")
                            .attributeSourceValue("egpId")
                            .build(),
                        Attribute.builder()
                            .attributeName("name")
                            .attributeSourceValue("name")
                            .build(),
                        Attribute.builder()
                            .attributeName("sitz")
                            .attributeSourceValue("sitz")
                            .build()))
                .build()),
        arguments(
            Lists.newArrayList(
                Attribute.builder()
                    .attributeName("egpId")
                    .attributeSourceValue("egpId")
                    .processingFlags(EnumSet.of(ProcessingFlag.HASHED, ProcessingFlag.ENCRYPTED))
                    .build(),
                Attribute.builder()
                    .attributeName("name")
                    .attributeSourceValue("name")
                    .processingFlags(
                        EnumSet.of(
                            ProcessingFlag.PHONETICALLY_NORMALIZED,
                            ProcessingFlag.EDITING_DISTANCE,
                            ProcessingFlag.HASHED,
                            ProcessingFlag.ENCRYPTED))
                    .build(),
                Attribute.builder()
                    .attributeName("gemeinschaftArt")
                    .attributeSourceValue("gemeinschaftArt")
                    .build()),
            GBPersonEvent.builder()
                .personType(PersonType.GEMEINSCHAFT)
                .eventType(EventType.INSERT)
                .attributes(
                    Lists.newArrayList(
                        Attribute.builder()
                            .attributeName("egpId")
                            .attributeSourceValue("egpId")
                            .build(),
                        Attribute.builder()
                            .attributeName("name")
                            .attributeSourceValue("name")
                            .build(),
                        Attribute.builder()
                            .attributeName("gemeinschaftArt")
                            .attributeSourceValue("gemeinschaftArt")
                            .build()))
                .build()));
  }

  @Test
  void testMissingSupportedAttributes() {
    GBPersonEventAttributeValidator gbPersonEventAttributeValidator =
        GBPersonEventAttributeValidator.builder()
            .handlerConfiguration(HandlerConfiguration.builder().build())
            .build();

    GBPersonEvent gbPersonEvent =
        GBPersonEvent.builder()
            .personType(PersonType.NATUERLICHE_PERSON)
            .eventType(EventType.INSERT)
            .attributes(
                Collections.singletonList(
                    Attribute.builder().attributeName("name").attributeSourceValue("name").build()))
            .build();

    assertThrows(
        RequiredParameterMissing.class,
        () -> gbPersonEventAttributeValidator.processImpl(CORRELATION_ID, gbPersonEvent));
  }

  @Test
  void testInvalidAttributeDroppedProcessorEvent() {
    GBPersonEventAttributeValidator gbPersonEventAttributeValidator =
        GBPersonEventAttributeValidator.builder()
            .handlerConfiguration(handlerConfigurationNoMerge)
            .processorEventListener(processorEventListener)
            .build();

    List<Attribute> merged = new ArrayList<>();
    merged.addAll(validAttributes);
    merged.addAll(invalidAttributes);

    GBPersonEvent gbPersonEvent =
        GBPersonEvent.builder()
            .personType(PersonType.NATUERLICHE_PERSON)
            .eventType(EventType.INSERT)
            .attributes(merged)
            .build();

    Mockito.reset(processorEventListener);
    GBPersonEvent resultGBPersonEvent =
        gbPersonEventAttributeValidator.processImpl(CORRELATION_ID, gbPersonEvent);

    Mockito.verify(processorEventListener, Mockito.times(2))
        .processorEvent(processorEventArgumentCaptor.capture());

    List<Attribute> invalidAttributeResult =
        processorEventArgumentCaptor.getAllValues().stream()
            .map(InvalidAttributeDroppedProcessorEvent::getInvalidAttribute)
            .collect(Collectors.toList());

    assertTrue(invalidAttributes.containsAll(invalidAttributeResult));

    assertEquals(validAttributes, resultGBPersonEvent.getAttributes());
  }

  @Test
  void testHandlerConfigurationMissingException() {
    GBPersonEvent gbPersonEvent =
        GBPersonEvent.builder()
            .personType(PersonType.NATUERLICHE_PERSON)
            .eventType(EventType.INSERT)
            .build();

    GBPersonEventAttributeValidator gbPersonEventAttributeValidator =
        GBPersonEventAttributeValidator.builder()
            .processorEventListener(processorEventListener)
            .build();
    assertThrows(
        HandlerConfigurationMissingException.class,
        () -> gbPersonEventAttributeValidator.process(CORRELATION_ID, gbPersonEvent));
  }

  @Test
  void testSetHandlerConfigurationAfterBuilder() {
    GBPersonEvent gbPersonEvent =
        GBPersonEvent.builder()
            .personType(PersonType.NATUERLICHE_PERSON)
            .eventType(EventType.INSERT)
            .build();

    GBPersonEventAttributeValidator gbPersonEventAttributeValidator =
        GBPersonEventAttributeValidator.builder()
            .processorEventListener(processorEventListener)
            .build();
    gbPersonEventAttributeValidator.setHandlerConfiguration(handlerConfigurationNoMerge);
    assertDoesNotThrow(
        () -> gbPersonEventAttributeValidator.process(CORRELATION_ID, gbPersonEvent));
  }

  @Test
  void testUnsupportedPersonType() {
    GBPersonEvent gbPersonEvent =
        GBPersonEvent.builder().personType(PersonType.NONE).eventType(EventType.INSERT).build();

    GBPersonEventAttributeValidator gbPersonEventAttributeValidator =
        GBPersonEventAttributeValidator.builder()
            .processorEventListener(processorEventListener)
            .handlerConfiguration(handlerConfigurationNoMerge)
            .build();
    assertThrows(
        UnsupportedPersonTypeException.class,
        () -> gbPersonEventAttributeValidator.process(CORRELATION_ID, gbPersonEvent));
  }

  @ParameterizedTest
  @DisplayName("Test validator on different GBPersonEventTypes")
  @MethodSource("validatorTest")
  void testGBPersonEventVariations(
      final List<Attribute> attributes, final GBPersonEvent gbPersonEvent) {
    Collections.sort(attributes);

    GBPersonEventAttributeValidator gbPersonEventAttributeValidator =
        GBPersonEventAttributeValidator.builder()
            .processorEventListener(processorEventListener)
            .handlerConfiguration(handlerConfigurationMerge)
            .build();
    GBPersonEvent resultGBPersonEvent =
        gbPersonEventAttributeValidator.process(CORRELATION_ID, gbPersonEvent);
    assertEquals(attributes, resultGBPersonEvent.getAttributes());
  }
}
