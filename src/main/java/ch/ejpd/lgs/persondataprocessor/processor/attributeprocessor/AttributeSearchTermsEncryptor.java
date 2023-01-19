package ch.ejpd.lgs.persondataprocessor.processor.attributeprocessor;

import ch.ejpd.lgs.persondataprocessor.configuration.ProcessingFlag;
import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.EncryptionSingleItemProcessor;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;

@SuperBuilder
public class AttributeSearchTermsEncryptor extends AbstractSingleItemProcessor<Attribute> {
  @Override
  public Attribute processImpl(
      @NonNull final String correlationId, @NonNull final Attribute attribute) {
    Attribute processingAttribute = new Attribute(attribute);

    EncryptionSingleItemProcessor encryptionSingleItemProcessor =
        EncryptionSingleItemProcessor.builder()
            .handlerConfiguration(getHandlerConfiguration())
            .build();

    if (processingAttribute.getProcessingFlags().contains(ProcessingFlag.ENCRYPTED)) {
      processingAttribute.setSearchTerms(
          processingAttribute.getSearchTerms().stream()
              .map(searchTerm -> encryptionSingleItemProcessor.process(correlationId, searchTerm))
              .collect(Collectors.toList()));
    }

    return processingAttribute;
  }
}
