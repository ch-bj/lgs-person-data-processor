package org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.event.AbstractProcessorEvent;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;

@SuperBuilder
@Getter
public class DuplicatedAttributeDroppedProcessorEvent extends AbstractProcessorEvent {
  @NonNull private final Attribute invalidAttribute;

  @Override
  public String getMessage() {
    return "Duplicated attribute dropped. CorrelationId("
        + getCorrelationId()
        + "), "
        + invalidAttribute.toString();
  }
}
