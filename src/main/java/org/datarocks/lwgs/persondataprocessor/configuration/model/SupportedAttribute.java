package org.datarocks.lwgs.persondataprocessor.configuration.model;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import org.datarocks.lwgs.persondataprocessor.configuration.ProcessingFlag;

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
