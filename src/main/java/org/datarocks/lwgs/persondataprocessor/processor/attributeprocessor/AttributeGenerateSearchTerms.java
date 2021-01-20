package org.datarocks.lwgs.persondataprocessor.processor.attributeprocessor;

import java.util.Collections;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;
import org.datarocks.lwgs.persondataprocessor.configuration.ProcessingFlag;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.EditingDistanceProcessor;

@SuperBuilder
public class AttributeGenerateSearchTerms extends AbstractSingleItemProcessor<Attribute> {
  @Override
  public Attribute processImpl(
      @NonNull final String correlationId, @NonNull final Attribute attribute) {
    Attribute processingAttribute = new Attribute(attribute);

    if (processingAttribute.getProcessingFlags().contains(ProcessingFlag.EDITING_DISTANCE)) {
      EditingDistanceProcessor editingDistanceProcessor =
          EditingDistanceProcessor.builder()
              .handlerConfiguration(getHandlerConfiguration())
              .build();

      processingAttribute.setSearchTerms(
          editingDistanceProcessor.process(correlationId, processingAttribute.getAttributeValue()));
    } else {
      processingAttribute.setSearchTerms(
          Collections.singletonList(processingAttribute.getAttributeValue()));
    }
    return processingAttribute;
  }
}
