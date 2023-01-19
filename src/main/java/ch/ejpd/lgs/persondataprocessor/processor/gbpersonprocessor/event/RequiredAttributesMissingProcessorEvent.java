package ch.ejpd.lgs.persondataprocessor.processor.gbpersonprocessor.event;

import ch.ejpd.lgs.persondataprocessor.configuration.model.SupportedAttribute;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.event.AbstractProcessorEvent;

@SuperBuilder
@Getter
public class RequiredAttributesMissingProcessorEvent extends AbstractProcessorEvent {
  @NonNull private final SupportedAttribute supportedAttribute;

  @Override
  public String getMessage() {
    return "Required attributes missing. CorrelationId("
        + getCorrelationId()
        + "), "
        + supportedAttribute;
  }
}
