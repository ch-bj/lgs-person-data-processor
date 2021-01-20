package org.datarocks.lwgs.persondataprocessor.model;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
public class GBPersonEvent {

  @Expose @NonNull PersonType personType;
  @Expose @NonNull EventType eventType;
  @Expose List<Attribute> attributes;

  @Builder
  public GBPersonEvent(
      @NonNull final PersonType personType,
      @NonNull final EventType eventType,
      final List<Attribute> attributes) {
    this.personType = personType;
    this.eventType = eventType;
    if (attributes == null) {
      this.attributes = new ArrayList<>();
    } else {
      this.attributes = attributes;
    }
  }

  public GBPersonEvent(GBPersonEvent gbPersonEvent) {
    personType = gbPersonEvent.personType;
    eventType = gbPersonEvent.eventType;
    attributes = gbPersonEvent.attributes.stream().map(Attribute::new).collect(Collectors.toList());
  }

  public void clearAttributes() {
    attributes.clear();
  }

  public PersonType getPersonType() {
    return personType;
  }

  public EventType getEventType() {
    return eventType;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void addAttribute(@NonNull final Attribute attribute) {
    attribute.setGBPersonEvent(this);
    attributes.remove(attribute);
    attributes.add(attribute);
  }

  public void addAttributes(List<Attribute> attributes) {
    attributes.forEach(this::addAttribute);
  }
}
