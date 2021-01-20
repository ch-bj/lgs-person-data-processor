package org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.event.AbstractProcessorEvent;
import org.datarocks.lwgs.persondataprocessor.configuration.model.SupportedAttribute;

@SuperBuilder
@Getter
public class RequiredAttributesMissingProcessorEvent extends AbstractProcessorEvent {
  @NonNull private final SupportedAttribute supportedAttribute;

  @Override
  public String getMessage() {
    return "Required attributes missing. CorrelationId("
        + getCorrelationId()
        + "), "
        + supportedAttribute.toString();
  }
}
