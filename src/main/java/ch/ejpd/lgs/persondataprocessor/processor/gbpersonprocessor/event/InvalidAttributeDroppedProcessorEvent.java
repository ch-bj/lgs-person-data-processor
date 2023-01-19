package ch.ejpd.lgs.persondataprocessor.processor.gbpersonprocessor.event;

import ch.ejpd.lgs.persondataprocessor.configuration.model.SupportedAttribute;
import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.event.AbstractProcessorEvent;

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
