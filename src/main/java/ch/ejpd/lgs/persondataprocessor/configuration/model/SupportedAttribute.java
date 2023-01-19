package ch.ejpd.lgs.persondataprocessor.configuration.model;

import ch.ejpd.lgs.persondataprocessor.configuration.ProcessingFlag;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
@ToString
public class SupportedAttribute {
  @NonNull String attributeName;
  @NonNull String attributeType;
  Set<ProcessingFlag> processingFlags;
  List<String> validMembers;
  boolean required;
  String mergeWith;
}
