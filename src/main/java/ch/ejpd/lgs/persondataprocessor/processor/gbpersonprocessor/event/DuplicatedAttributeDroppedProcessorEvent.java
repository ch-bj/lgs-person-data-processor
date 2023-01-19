package ch.ejpd.lgs.persondataprocessor.processor.gbpersonprocessor.event;

import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.event.AbstractProcessorEvent;

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
