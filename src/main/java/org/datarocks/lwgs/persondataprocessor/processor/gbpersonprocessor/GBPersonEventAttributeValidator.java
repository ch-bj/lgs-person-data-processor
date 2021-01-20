package org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.exception.RequiredParameterMissing;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.datarocks.lwgs.persondataprocessor.configuration.model.SupportedAttribute;
import org.datarocks.lwgs.persondataprocessor.configuration.model.SupportedAttributes;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;
import org.datarocks.lwgs.persondataprocessor.model.GBPersonEvent;
import org.datarocks.lwgs.persondataprocessor.model.PersonType;
import org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.event.DuplicatedAttributeDroppedProcessorEvent;
import org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.event.InvalidAttributeDroppedProcessorEvent;
import org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.event.RequiredAttributesMissingProcessorEvent;
import org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.exception.UnsupportedPersonTypeException;

@SuperBuilder
public class GBPersonEventAttributeValidator extends AbstractSingleItemProcessor<GBPersonEvent> {
  private static final boolean MERGE_ATTRIBUTES_DEFAULT = true;

  @Value
  @Builder
  static class ValidationPair {
    Attribute attribute;
    SupportedAttribute supportedAttribute;
  }

  @Override
  public GBPersonEvent processImpl(
      @NonNull final String correlationId, @NonNull final GBPersonEvent gbPersonEvent) {
    GBPersonEvent processingGBPersonEvent = new GBPersonEvent(gbPersonEvent);

    List<Attribute> eventAttributes = new ArrayList<>(gbPersonEvent.getAttributes());

    List<SupportedAttribute> supportedAttributesForPersonType =
        getSupportedAttributesForPersonType(processingGBPersonEvent.getPersonType());

    checkNoRequiredAttributesAreMissing(
        supportedAttributesForPersonType, eventAttributes, correlationId);

    eventAttributes = removeDuplicatedAttributes(eventAttributes, correlationId);

    List<ValidationPair> validationPairs =
        generateValidationPairs(supportedAttributesForPersonType, eventAttributes, correlationId);

    if (isMergeAttributes()) {
      validationPairs = mergeValidationPairs(validationPairs);
    }

    processingGBPersonEvent.clearAttributes();

    List<Attribute> resultAttributes = getAttributesWithProcessingFlags(validationPairs);
    Collections.sort(resultAttributes);
    processingGBPersonEvent.addAttributes(resultAttributes);

    return processingGBPersonEvent;
  }

  private void checkNoRequiredAttributesAreMissing(
      @NonNull final List<SupportedAttribute> supportedAttributes,
      @NonNull final List<Attribute> attributes,
      @NonNull final String correlationId) {
    if (processorEventListener == null) {
      return;
    }

    List<SupportedAttribute> requiredAttributes =
        supportedAttributes.stream()
            .filter(SupportedAttribute::isRequired)
            .collect(Collectors.toList());

    requiredAttributes.forEach(
        supportedAttribute -> {
          if (attributes.stream()
              .noneMatch(
                  attribute ->
                      attribute.getAttributeName().equals(supportedAttribute.getAttributeName()))) {
            processorEventListener.processorEvent(
                RequiredAttributesMissingProcessorEvent.builder()
                    .supportedAttribute(supportedAttribute)
                    .correlationId(correlationId)
                    .build());
          }
        });
  }

  private List<Attribute> removeDuplicatedAttributes(
      @NonNull final List<Attribute> attributes, @NonNull final String correlationId) {
    Map<String, Attribute> processedAttributes = new HashMap<>();
    attributes.forEach(
        attribute -> {
          if (processedAttributes.get(attribute.getAttributeName()) != null) {
            processorEventListener.processorEvent(
                DuplicatedAttributeDroppedProcessorEvent.builder()
                    .invalidAttribute(attribute)
                    .correlationId(correlationId)
                    .build());
          } else {
            processedAttributes.put(attribute.getAttributeName(), attribute);
          }
        });

    return new ArrayList<>(processedAttributes.values());
  }

  private List<ValidationPair> generateValidationPairs(
      @NonNull final List<SupportedAttribute> supportedAttributes,
      @NonNull final List<Attribute> attributes,
      @NonNull final String correlationId) {
    return attributes.stream()
        .map(
            attribute ->
                ValidationPair.builder()
                    .attribute(new Attribute(attribute))
                    .supportedAttribute(
                        getSupportedAttributeForAttribute(supportedAttributes, attribute))
                    .build())
        .filter(
            validationPair -> {
              if (validationPair.supportedAttribute == null) {
                if (processorEventListener != null) {
                  processorEventListener.processorEvent(
                      InvalidAttributeDroppedProcessorEvent.builder()
                          .invalidAttribute(validationPair.getAttribute())
                          .supportedAttribute(validationPair.getSupportedAttribute())
                          .correlationId(correlationId)
                          .build());
                }
                return false;
              }
              return true;
            })
        .collect(Collectors.toList());
  }

  private List<SupportedAttribute> getSupportedAttributesForPersonType(
      @NonNull final PersonType personType) {
    Optional<SupportedAttributes> optionalSupportedAttributes =
        getHandlerConfiguration()
            .getConfigurationItem(
                SupportedAttributes.class,
                LWGSPersonDataProcessorParameters.PARAM_KEY_SUPPORTED_ATTRIBUTES);
    if (optionalSupportedAttributes.isPresent()) {
      switch (personType) {
        case NATUERLICHE_PERSON:
          return optionalSupportedAttributes.get().getNatuerlichePerson();
        case JURISTISCHE_PERSON:
          return optionalSupportedAttributes.get().getJuristischePerson();
        case GEMEINSCHAFT:
          return optionalSupportedAttributes.get().getGemeinschaft();
        default:
          throw new UnsupportedPersonTypeException(personType);
      }
    } else {
      throw new RequiredParameterMissing(
          "SupportedAttributes is a required parameter for GBPersonEventAttributeValidator.");
    }
  }

  private boolean isMergeAttributes() {
    Optional<String> mergeAttributesParam =
        getHandlerConfiguration()
            .getConfigurationItem(
                String.class, LWGSPersonDataProcessorParameters.PARAM_KEY_MERGE_ATTRIBUTES);

    return mergeAttributesParam.map(Boolean::parseBoolean).orElse(MERGE_ATTRIBUTES_DEFAULT);
  }

  private List<Attribute> getAttributesWithProcessingFlags(List<ValidationPair> validationPairs) {
    return validationPairs.stream()
        .map(
            validationPair ->
                Attribute.builder()
                    .attributeName(validationPair.getAttribute().getAttributeName())
                    .attributeSourceValue(validationPair.getAttribute().getAttributeValue())
                    .processingFlags(validationPair.getSupportedAttribute().getProcessingFlags())
                    .searchTerms(validationPair.getAttribute().getSearchTerms())
                    .build())
        .collect(Collectors.toList());
  }

  private List<ValidationPair> mergeValidationPairs(
      @NonNull final List<ValidationPair> validationPairs) {
    Queue<ValidationPair> validationPairStack = new LinkedList<>(validationPairs);

    LinkedList<LinkedList<ValidationPair>> validationPairListOfLinkedLists = new LinkedList<>();

    while (!validationPairStack.isEmpty()) {
      ValidationPair validationPair = validationPairStack.remove();

      if (validationPair.supportedAttribute.getMergeWith() == null) {
        validationPairListOfLinkedLists.add(
            new LinkedList<>(Collections.singletonList(validationPair)));
      } else {
        String mergeAttributeName = validationPair.getSupportedAttribute().getMergeWith();
        LinkedList<ValidationPair> linkedList =
            getLinkedListWithValidationPairListOfLinkedLists(
                validationPairListOfLinkedLists, mergeAttributeName);
        if (linkedList != null) {
          linkedList.add(validationPair);
        } else if (!validationPairStack.isEmpty()) {
          validationPairStack.add(validationPair);
        }
      }
    }

    List<ValidationPair> validationPairList = new ArrayList<>();
    while (!validationPairListOfLinkedLists.isEmpty()) {
      LinkedList<ValidationPair> validationPairLinkedList = validationPairListOfLinkedLists.pop();
      mergeValidationPairs(validationPairLinkedList).ifPresent(validationPairList::add);
    }

    return validationPairList;
  }

  private Optional<ValidationPair> mergeValidationPairs(
      LinkedList<ValidationPair> validationPairLinkedList) {
    return validationPairLinkedList.stream()
        .reduce(
            (validationPair, validationPair2) ->
                ValidationPair.builder()
                    .attribute(
                        Attribute.builder()
                            .attributeName(validationPair.getAttribute().getAttributeName())
                            .attributeSourceValue(
                                validationPair.getAttribute().getAttributeValue()
                                    + " "
                                    + validationPair2.getAttribute().getAttributeValue())
                            .searchTerms(validationPair.getAttribute().getSearchTerms())
                            .processingFlags(validationPair.getAttribute().getProcessingFlags())
                            .build())
                    .supportedAttribute(validationPair.supportedAttribute)
                    .build());
  }

  private LinkedList<ValidationPair> getLinkedListWithValidationPairListOfLinkedLists(
      LinkedList<LinkedList<ValidationPair>> validationPairList, String attributeName) {
    return validationPairList.stream()
        .filter(
            linkedList ->
                linkedList.stream()
                    .anyMatch(
                        validationPair ->
                            validationPair
                                .supportedAttribute
                                .getAttributeName()
                                .equals(attributeName)))
        .findFirst()
        .orElse(null);
  }

  private SupportedAttribute getSupportedAttributeForAttribute(
      @NonNull final List<SupportedAttribute> supportedAttributes,
      @NonNull final Attribute attribute) {
    return supportedAttributes.stream()
        .filter(
            supportedAttribute ->
                supportedAttribute.getAttributeName().equals(attribute.getAttributeName()))
        .findFirst()
        .orElse(null);
  }
}
