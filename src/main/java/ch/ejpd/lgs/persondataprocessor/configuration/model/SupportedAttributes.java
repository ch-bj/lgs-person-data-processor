package ch.ejpd.lgs.persondataprocessor.configuration.model;

import ch.ejpd.lgs.persondataprocessor.configuration.exception.SupportedAttributeJsonDefinitionException;
import com.google.gson.Gson;
import java.util.List;
import lombok.Data;
import lombok.NonNull;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.Validator;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;

@Data
public class SupportedAttributes {
  @NonNull private List<SupportedAttribute> natuerlichePerson;
  @NonNull private List<SupportedAttribute> juristischePerson;
  @NonNull private List<SupportedAttribute> gemeinschaft;

  private static Gson gson = new Gson();

  public static SupportedAttributes fromJson(String schemaJson, String supportedAttributesJson) {
    try {
      JSONObject rawSchema = new JSONObject(schemaJson);
      Schema schema = SchemaLoader.load(rawSchema);

      Validator validator = Validator.builder().build();
      validator.performValidation(schema, new JSONObject(supportedAttributesJson));
    } catch (ValidationException | JSONException e) {
      throw new SupportedAttributeJsonDefinitionException(e);
    }

    return gson.fromJson(supportedAttributesJson, SupportedAttributes.class);
  }
}
