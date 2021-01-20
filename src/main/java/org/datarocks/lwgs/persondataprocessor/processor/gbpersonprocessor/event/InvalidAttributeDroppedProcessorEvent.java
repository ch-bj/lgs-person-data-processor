package org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.event.AbstractProcessorEvent;
import org.datarocks.lwgs.persondataprocessor.configuration.model.SupportedAttribute;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;

@SuperBuilder
@Getter
public class InvalidAttributeDroppedProcessorEvent extends AbstractProcessorEvent {
  @NonNull private final Attribute invalidAttribute;
  private final SupportedAttribute supportedAttribute;

  @Override
  public String getMessage() {
    String errorMessage =
        "Invalid attribute dropped. CorrelationId("
            + getCorrelationId()
            + "), "
            + invalidAttribute.toString();
    if (supportedAttribute != null) {
      errorMessage += ", " + supportedAttribute.toString();
    }
    return errorMessage;
  }
}
