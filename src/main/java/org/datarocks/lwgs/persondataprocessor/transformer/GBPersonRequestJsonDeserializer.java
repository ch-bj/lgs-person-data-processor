package org.datarocks.lwgs.persondataprocessor.transformer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.transformer.AbstractTransformer;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;
import org.datarocks.lwgs.persondataprocessor.model.EventType;
import org.datarocks.lwgs.persondataprocessor.model.GBPersonEvent;
import org.datarocks.lwgs.persondataprocessor.model.PersonType;
import org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.exception.UnsupportedPersonTypeException;
import org.datarocks.lwgs.persondataprocessor.transformer.exception.InvalidJsonStructure;

@SuperBuilder
public class GBPersonRequestJsonDeserializer extends AbstractTransformer<String, GBPersonEvent> {

  private static final String JSON_ELEMENT_METADATA = "metaData";
  private static final String JSON_ELEMENT_PERSON_TYPE = "personType";
  private static final String JSON_ELEMENT_EVENT_TYPE = "eventType";
  private static final String IS_MANDATORY_MSG = " is mandatory.";
  private static final String JSON_ELEMENT_NATUERLICHE_PERSON = "natuerlichePerson";
  private static final String JSON_ELEMENT_JURISTISCHE_PERSON = "juristischePerson";
  private static final String JSON_ELEMENT_GEMEINSCHAFT = "gemeinschaft";

  private static List<Attribute> convertToAttributes(@NonNull final JsonObject personObject) {
    return personObject.entrySet().stream()
        .map(
            entry ->
                Attribute.builder()
                    .attributeName(entry.getKey())
                    .attributeSourceValue(entry.getValue().getAsString())
                    .build())
        .collect(Collectors.toList());
  }

  private static void validateMetaData(JsonObject metaData) {
    if (!metaData.has(JSON_ELEMENT_PERSON_TYPE)) {
      throw new InvalidJsonStructure(
          JSON_ELEMENT_METADATA + "." + JSON_ELEMENT_PERSON_TYPE + IS_MANDATORY_MSG);
    }

    if (!metaData.has(JSON_ELEMENT_EVENT_TYPE)) {
      throw new InvalidJsonStructure(
          JSON_ELEMENT_METADATA + "." + JSON_ELEMENT_EVENT_TYPE + IS_MANDATORY_MSG);
    }
  }

  private static PersonType getPersonTypeFromMetaData(@NonNull final JsonObject metaData) {
    try {
      return PersonType.valueOf(metaData.get(JSON_ELEMENT_PERSON_TYPE).getAsString());
    } catch (IllegalArgumentException e) {
      throw new InvalidJsonStructure(
          metaData.get(JSON_ELEMENT_PERSON_TYPE).getAsString()
              + "is an invalid value for "
              + JSON_ELEMENT_PERSON_TYPE
              + ". Supported values: "
              + Arrays.toString(PersonType.values()));
    }
  }

  private static EventType getEventTypeFromMetaData(@NonNull final JsonObject metaData) {
    try {
      return EventType.valueOf(metaData.get(JSON_ELEMENT_EVENT_TYPE).getAsString());
    } catch (IllegalArgumentException e) {
      throw new InvalidJsonStructure(
          metaData.get(JSON_ELEMENT_EVENT_TYPE).getAsString()
              + "is an invalid value for "
              + JSON_ELEMENT_EVENT_TYPE
              + ". Supported values: "
              + Arrays.toString(EventType.values()));
    }
  }

  private static List<Attribute> getAttributes(
      @NonNull final PersonType personType, final JsonObject personObject) {
    if (personObject == null) {
      throw new InvalidJsonStructure(
          personType.toString()
              + " is required if "
              + JSON_ELEMENT_PERSON_TYPE
              + " is "
              + personType.toString());
    }
    return convertToAttributes(personObject);
  }

  @Override
  public GBPersonEvent processImpl(
      @NonNull final String correlationId, @NonNull final String input) {
    Gson gson = new Gson();

    JsonElement jsonElement;
    try {
      jsonElement = gson.fromJson(input, JsonElement.class);
    } catch (JsonSyntaxException e) {
      throw new InvalidJsonStructure();
    }

    if (jsonElement == null) {
      throw new InvalidJsonStructure();
    }

    if (!jsonElement.isJsonObject()) {
      throw new InvalidJsonStructure();
    }

    JsonObject gbPersonEventJson;
    JsonObject metaData;
    JsonObject naturalPerson;
    JsonObject legalPerson;
    JsonObject community;

    try {
      gbPersonEventJson = jsonElement.getAsJsonObject();
      metaData = gbPersonEventJson.getAsJsonObject(JSON_ELEMENT_METADATA);
      naturalPerson = gbPersonEventJson.getAsJsonObject(JSON_ELEMENT_NATUERLICHE_PERSON);
      legalPerson = gbPersonEventJson.getAsJsonObject(JSON_ELEMENT_JURISTISCHE_PERSON);
      community = gbPersonEventJson.getAsJsonObject(JSON_ELEMENT_GEMEINSCHAFT);
    } catch (ClassCastException e) {
      throw new InvalidJsonStructure();
    }

    validateMetaData(metaData);

    PersonType personType = getPersonTypeFromMetaData(metaData);
    EventType eventType = getEventTypeFromMetaData(metaData);

    List<Attribute> attributeList;

    switch (personType) {
      case NATUERLICHE_PERSON:
        attributeList = getAttributes(personType, naturalPerson);
        break;
      case JURISTISCHE_PERSON:
        attributeList = getAttributes(personType, legalPerson);
        break;
      case GEMEINSCHAFT:
        attributeList = getAttributes(personType, community);
        break;
      default:
        throw new UnsupportedPersonTypeException(personType);
    }

    return GBPersonEvent.builder()
        .eventType(eventType)
        .personType(personType)
        .attributes(attributeList)
        .build();
  }
}
