package ch.ejpd.lgs.persondataprocessor.processor.attributeprocessor;

import ch.ejpd.lgs.persondataprocessor.configuration.ProcessingFlag;
import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.DecryptionSingleItemProcessor;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;

@SuperBuilder
public class AttributeSearchTermsDecryptor extends AbstractSingleItemProcessor<Attribute> {
  @Override
  public Attribute processImpl(
      @NonNull final String correlationId, @NonNull final Attribute attribute) {
    Attribute processingAttribute = new Attribute(attribute);

    DecryptionSingleItemProcessor decryptionSingleItemProcessor =
        DecryptionSingleItemProcessor.builder()
            .handlerConfiguration(getHandlerConfiguration())
            .build();

    if (processingAttribute.getProcessingFlags().contains(ProcessingFlag.ENCRYPTED)) {
      processingAttribute.setSearchTerms(
          processingAttribute.getSearchTerms().stream()
              .map(searTerm -> decryptionSingleItemProcessor.process(correlationId, searTerm))
              .collect(Collectors.toList()));
    }

    return processingAttribute;
  }
}
