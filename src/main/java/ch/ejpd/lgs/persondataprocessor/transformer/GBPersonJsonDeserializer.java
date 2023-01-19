package ch.ejpd.lgs.persondataprocessor.transformer;

import ch.ejpd.lgs.persondataprocessor.model.GBPersonEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.transformer.AbstractTransformer;

@SuperBuilder
public class GBPersonJsonDeserializer extends AbstractTransformer<String, GBPersonEvent> {
  @Override
  public GBPersonEvent processImpl(
      @NonNull final String correlationId, @NonNull final String input) {
    Gson gson = (new GsonBuilder()).excludeFieldsWithoutExposeAnnotation().create();
    return gson.fromJson(input, GBPersonEvent.class);
  }
}
