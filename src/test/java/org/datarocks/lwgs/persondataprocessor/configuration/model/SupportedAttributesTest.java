package org.datarocks.lwgs.persondataprocessor.configuration.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.datarocks.lwgs.persondataprocessor.TestHelper;
import org.datarocks.lwgs.persondataprocessor.configuration.exception.SupportedAttributeJsonDefinitionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SupportedAttributesTest {

  private static SupportedAttributes supportedAttributes;

  @BeforeAll
  static void setup() throws IOException {
    supportedAttributes =
        SupportedAttributes.fromJson(
            TestHelper.readSupportedAttributesScheme(), TestHelper.readSupportedAttributes());
  }

  @Test
  @DisplayName("Check if main structures exist.")
  void validateMainStructures() {
    assertNotNull(supportedAttributes.getNatuerlichePerson());
    assertNotNull(supportedAttributes.getJuristischePerson());
    assertNotNull(supportedAttributes.getGemeinschaft());
  }

  @Test
  @DisplayName("Validate SupportedAttributes.json - names not empty")
  void validateNamesNotEmpty() {
    assertFalse(
        supportedAttributes.getNatuerlichePerson().stream()
            .anyMatch(
                supportedAttribute ->
                    supportedAttribute.getAttributeName() == null
                        || supportedAttribute.getAttributeName().isEmpty()));
    assertFalse(
        supportedAttributes.getJuristischePerson().stream()
            .anyMatch(
                supportedAttribute ->
                    supportedAttribute.getAttributeName() == null
                        || supportedAttribute.getAttributeName().isEmpty()));
    assertFalse(
        supportedAttributes.getGemeinschaft().stream()
            .anyMatch(
                supportedAttribute ->
                    supportedAttribute.getAttributeName() == null
                        || supportedAttribute.getAttributeName().isEmpty()));
  }

  @Test
  @DisplayName("Validate SupportedAttributes.json - types not empty")
  void validateTypesNotEmpty() {
    assertFalse(
        supportedAttributes.getNatuerlichePerson().stream()
            .anyMatch(
                supportedAttribute ->
                    supportedAttribute.getAttributeType() == null
                        || supportedAttribute.getAttributeType().isEmpty()));
    assertFalse(
        supportedAttributes.getJuristischePerson().stream()
            .anyMatch(
                supportedAttribute ->
                    supportedAttribute.getAttributeType() == null
                        || supportedAttribute.getAttributeType().isEmpty()));
    assertFalse(
        supportedAttributes.getGemeinschaft().stream()
            .anyMatch(
                supportedAttribute ->
                    supportedAttribute.getAttributeType() == null
                        || supportedAttribute.getAttributeType().isEmpty()));
  }

  @Test
  @DisplayName("Test exceptions is raised when SupportedAttributes.json is invalid")
  void tesExceptionOnInvalidJson() throws IOException {
    String supportedAttributesScheme = TestHelper.readSupportedAttributesScheme();
    assertThrows(
        SupportedAttributeJsonDefinitionException.class,
        () -> SupportedAttributes.fromJson(supportedAttributesScheme, "}"));
  }
}
