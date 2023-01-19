package ch.ejpd.lgs.persondataprocessor.processor.attributeprocessor;

import ch.ejpd.lgs.persondataprocessor.configuration.ProcessingFlag;
import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.HashingSingleItemProcessor;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;

@SuperBuilder
public class AttributeSearchTermsHashing extends AbstractSingleItemProcessor<Attribute> {
  @Override
  public Attribute processImpl(
      @NonNull final String correlationId, @NonNull final Attribute attribute) {
    Attribute processingAttribute = new Attribute(attribute);

    HashingSingleItemProcessor hashingSingleItemProcessor =
        HashingSingleItemProcessor.builder()
            .handlerConfiguration(getHandlerConfiguration())
            .build();

    if (processingAttribute.getSearchTerms() != null
        && processingAttribute.getProcessingFlags().contains(ProcessingFlag.HASHED)) {
      processingAttribute.setSearchTerms(
          processingAttribute.getSearchTerms().stream()
              .map(searchTerms -> hashingSingleItemProcessor.process(correlationId, searchTerms))
              .collect(Collectors.toList()));
    }

    return processingAttribute;
  }
}
