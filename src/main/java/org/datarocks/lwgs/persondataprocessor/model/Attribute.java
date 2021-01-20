package org.datarocks.lwgs.persondataprocessor.model;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.datarocks.lwgs.persondataprocessor.configuration.ProcessingFlag;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@ToString
public class Attribute implements Comparable<Attribute> {

  @EqualsAndHashCode.Include @Expose private final String attributeName;

  @Expose(serialize = false)
  private final String attributeSourceValue;

  @Expose(serialize = false)
  private String attributeProcessingValue;

  @Expose(serialize = false)
  private final EnumSet<ProcessingFlag> processingFlags = EnumSet.of(ProcessingFlag.NONE);

  @Expose private final List<String> searchTerms;

  @Expose(serialize = false)
  private GBPersonEvent gbPersonEvent;

  protected Attribute(
      @NonNull final String attributeName,
      @NonNull final String attributeSourceValue,
      @NonNull final GBPersonEvent gbPersonEvent) {
    this.attributeName = attributeName;
    this.attributeSourceValue = attributeSourceValue;
    this.attributeProcessingValue = attributeSourceValue;
    this.gbPersonEvent = gbPersonEvent;
    this.searchTerms = new ArrayList<>();
  }

  @Builder
  public Attribute(
      @NonNull final String attributeName,
      @NonNull final String attributeSourceValue,
      final Set<ProcessingFlag> processingFlags,
      final List<String> searchTerms) {
    if (processingFlags == null || processingFlags.isEmpty()) {
      this.processingFlags.add(ProcessingFlag.NONE);
    } else {
      this.processingFlags.clear();
      this.processingFlags.addAll(processingFlags);
    }
    this.attributeName = attributeName;
    this.attributeProcessingValue = attributeSourceValue;
    this.attributeSourceValue = attributeSourceValue;
    if (searchTerms == null) {
      this.searchTerms = new ArrayList<>();
    } else {
      this.searchTerms = searchTerms;
    }
  }

  public Attribute(Attribute attribute) {
    this.attributeName = attribute.attributeName;
    this.attributeSourceValue = attribute.attributeSourceValue;
    this.attributeProcessingValue = attribute.attributeProcessingValue;
    this.processingFlags.addAll(attribute.processingFlags);
    this.searchTerms = new ArrayList<>(attribute.searchTerms);
    this.gbPersonEvent = getGbPersonEvent();
  }

  public GBPersonEvent getGbPersonEvent() {
    return gbPersonEvent;
  }

  protected void setGBPersonEvent(@NonNull final GBPersonEvent gbPersonEvent) {
    this.gbPersonEvent = gbPersonEvent;
  }

  public String getAttributeName() {
    return attributeName;
  }

  public String getAttributeValue() {
    return attributeProcessingValue;
  }

  public void setAttributeValue(@NonNull final String attributeValue) {
    this.attributeProcessingValue = attributeValue;
  }

  public Set<ProcessingFlag> getProcessingFlags() {
    return processingFlags;
  }

  public List<String> getSearchTerms() {
    return searchTerms;
  }

  public void setSearchTerms(@NonNull final List<String> searchTerms) {
    this.searchTerms.clear();
    this.searchTerms.addAll(searchTerms);
  }

  @Override
  public int compareTo(Attribute attribute) {
    return this.attributeName.compareTo(attribute.attributeName);
  }
}
