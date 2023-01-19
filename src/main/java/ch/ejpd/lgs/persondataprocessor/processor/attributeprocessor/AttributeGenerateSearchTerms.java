package ch.ejpd.lgs.persondataprocessor.processor.attributeprocessor;

import ch.ejpd.lgs.persondataprocessor.configuration.ProcessingFlag;
import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.EditingDistanceProcessor;
import java.util.Collections;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;

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
