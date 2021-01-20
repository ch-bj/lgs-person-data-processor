package org.datarocks.lwgs.persondataprocessor.processor;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.EditingDistanceProcessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EditingDistanceProcessorTest {
  private static EditingDistanceProcessor editingDistanceProcessor;

  static Stream<Arguments> editingDistanceTests() {
    return Stream.of(
        arguments(
            "ABC", Arrays.asList("ABC", "*ABC", "*BC", "A*BC", "A*C", "AB*C", "AB*", "ABC*")));
  }

  @BeforeAll
  static void setup() {
    HandlerConfiguration handlerConfiguration = HandlerConfiguration.builder().build();
    editingDistanceProcessor =
        EditingDistanceProcessor.builder().handlerConfiguration(handlerConfiguration).build();
  }

  @ParameterizedTest
  @DisplayName("Test editing distance term expansion")
  @MethodSource("editingDistanceTests")
  void testEditingDistance(String inputTerm, List<String> expectedExpandedTerms) {
    List<String> expandedTerms =
        editingDistanceProcessor.process(UUID.randomUUID().toString(), inputTerm);
    assertThat(expandedTerms).hasSameElementsAs(expectedExpandedTerms);
  }
}
