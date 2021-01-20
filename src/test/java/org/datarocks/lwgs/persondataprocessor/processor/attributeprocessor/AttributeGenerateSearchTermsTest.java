package org.datarocks.lwgs.persondataprocessor.processor.attributeprocessor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.UUID;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.lwgs.persondataprocessor.configuration.ProcessingFlag;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;
import org.junit.jupiter.api.Test;

class AttributeGenerateSearchTermsTest {
  @Test
  void withEditingDistance() {
    AttributeGenerateSearchTerms attributeGenerateSearchTerms =
        AttributeGenerateSearchTerms.builder()
            .handlerConfiguration(HandlerConfiguration.builder().build())
            .build();

    Attribute result =
        attributeGenerateSearchTerms.processImpl(
            UUID.randomUUID().toString(),
            Attribute.builder()
                .attributeName("A Attribute")
                .attributeSourceValue("ABC")
                .processingFlags(EnumSet.of(ProcessingFlag.EDITING_DISTANCE))
                .build());

    assertEquals(8, result.getSearchTerms().size());
  }

  @Test
  void withoutEditingDistance() {
    AttributeGenerateSearchTerms attributeGenerateSearchTerms =
        AttributeGenerateSearchTerms.builder()
            .handlerConfiguration(HandlerConfiguration.builder().build())
            .build();

    Attribute result =
        attributeGenerateSearchTerms.processImpl(
            UUID.randomUUID().toString(),
            Attribute.builder()
                .attributeName("A Attribute")
                .attributeSourceValue("ABC")
                .processingFlags(EnumSet.of(ProcessingFlag.NONE))
                .build());

    assertEquals(1, result.getSearchTerms().size());
  }

  @Test
  void withoutMissingFlags() {
    AttributeGenerateSearchTerms attributeGenerateSearchTerms =
        AttributeGenerateSearchTerms.builder()
            .handlerConfiguration(HandlerConfiguration.builder().build())
            .build();

    Attribute result =
        attributeGenerateSearchTerms.processImpl(
            UUID.randomUUID().toString(),
            Attribute.builder().attributeName("A Attribute").attributeSourceValue("ABC").build());

    assertEquals(1, result.getSearchTerms().size());
  }
}
