package org.datarocks.lwgs.persondataprocessor.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.datarocks.lwgs.persondataprocessor.configuration.ProcessingFlag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AttributeTest {

  @Test
  void testMandatoryParameters() {
    final Attribute.AttributeBuilder attributeBuilder1 = Attribute.builder();
    assertThrows(NullPointerException.class, attributeBuilder1::build);

    final Attribute.AttributeBuilder attributeBuilder2 = Attribute.builder().attributeName("name");
    assertThrows(NullPointerException.class, attributeBuilder2::build);

    final Attribute.AttributeBuilder attributeBuilder3 =
        Attribute.builder().attributeSourceValue("value");
    assertThrows(NullPointerException.class, attributeBuilder3::build);

    assertDoesNotThrow(
        () -> Attribute.builder().attributeName("name").attributeSourceValue("value").build());
  }

  @Test
  @DisplayName(
      "That that the default ProcessingFlags NONE is set when no processing flags are passed to the builder.")
  void testDefaultProcessingFlagIsNONE() {
    Attribute attribute =
        Attribute.builder().attributeName("name").attributeSourceValue("sourceValue").build();
    assertEquals(EnumSet.of(ProcessingFlag.NONE), attribute.getProcessingFlags());

    attribute =
        Attribute.builder()
            .attributeName("name")
            .attributeSourceValue("sourceValue")
            .processingFlags(EnumSet.noneOf(ProcessingFlag.class))
            .build();
    assertEquals(EnumSet.of(ProcessingFlag.NONE), attribute.getProcessingFlags());
  }

  @Test
  @DisplayName(
      "That that the default ProcessingFlags NONE is not present when processing flags are passed to the builder.")
  void testNONENotPresentWhenProcessingFlagsAreSet() {
    Attribute attribute =
        Attribute.builder()
            .attributeName("name")
            .attributeSourceValue("sourceValue")
            .processingFlags(EnumSet.of(ProcessingFlag.ENCRYPTED))
            .build();
    assertEquals(EnumSet.of(ProcessingFlag.ENCRYPTED), attribute.getProcessingFlags());
  }

  @Test
  void testDefaultConstructor() {
    GBPersonEvent gbPersonEvent =
        GBPersonEvent.builder()
            .personType(PersonType.NATUERLICHE_PERSON)
            .eventType(EventType.INSERT)
            .build();

    final Attribute attribute = new Attribute("name", "sourceValue", gbPersonEvent);
    assertEquals("sourceValue", attribute.getAttributeValue());
    assertEquals(gbPersonEvent, attribute.getGbPersonEvent());
  }

  @Test
  void testAttributeValueCantBeNull() {
    final Attribute.AttributeBuilder builder = Attribute.builder().attributeName("abc");
    assertThrows(NullPointerException.class, () -> builder.attributeSourceValue(null));
  }

  @Test
  void testAttributeValueCantBeSetToNull() {
    Attribute attribute =
        Attribute.builder().attributeName("abc").attributeSourceValue("abc").build();
    assertThrows(NullPointerException.class, () -> attribute.setAttributeValue(null));
  }

  @Test
  void testAttributeSearchTermsNeverNull() {
    Attribute attribute =
        Attribute.builder().attributeName("abc").attributeSourceValue("abc").build();
    assertNotNull(attribute.getSearchTerms());

    assertThrows(NullPointerException.class, () -> attribute.setSearchTerms(null));
  }

  @Test
  void testAttributeSearchTermsInOutConsistency() {
    final List<String> attributeSearchTerms = Collections.singletonList("ABC");

    Attribute attribute =
        Attribute.builder()
            .attributeName("abc")
            .attributeSourceValue("abc")
            .searchTerms(attributeSearchTerms)
            .build();
    assertEquals(attributeSearchTerms, attribute.getSearchTerms());

    attribute = Attribute.builder().attributeName("abc").attributeSourceValue("abc").build();

    attribute.setSearchTerms(attributeSearchTerms);
    assertEquals(attributeSearchTerms, attribute.getSearchTerms());
  }

  @Test
  void testCopyConstructor() {
    final List<String> attributeSearchTerms = Collections.singletonList("ABC");

    Attribute attribute =
        Attribute.builder()
            .attributeName("abc")
            .attributeSourceValue("abc")
            .searchTerms(attributeSearchTerms)
            .processingFlags(EnumSet.of(ProcessingFlag.HASHED))
            .build();

    Attribute deepCopyAttribute = new Attribute(attribute);
    assertEquals(attribute, deepCopyAttribute);
  }
}
