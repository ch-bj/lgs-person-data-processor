package ch.ejpd.lgs.persondataprocessor.processor;

import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import ch.ejpd.lgs.persondataprocessor.model.GBPersonEvent;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.pipeline.PipeLine;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;

@SuperBuilder
public class AttributeSubPipeline extends AbstractSingleItemProcessor<GBPersonEvent> {
  @NonNull private final PipeLine<Attribute, Attribute, Attribute> attributePipeLine;

  @Override
  public GBPersonEvent processImpl(
      @NonNull final String correlationId, @NonNull final GBPersonEvent gbPersonEvent) {
    List<Attribute> processedAttributes =
        gbPersonEvent.getAttributes().stream()
            .map(attribute -> attributePipeLine.process(correlationId, attribute))
            .collect(Collectors.toList());

    gbPersonEvent.clearAttributes();
    gbPersonEvent.addAttributes(processedAttributes);

    return gbPersonEvent;
  }
}
