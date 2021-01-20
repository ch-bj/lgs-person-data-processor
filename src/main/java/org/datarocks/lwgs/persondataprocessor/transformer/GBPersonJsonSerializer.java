package org.datarocks.lwgs.persondataprocessor.transformer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.transformer.AbstractTransformer;
import org.datarocks.lwgs.persondataprocessor.model.GBPersonEvent;

@SuperBuilder
public class GBPersonJsonSerializer extends AbstractTransformer<GBPersonEvent, String> {
  @Override
  public String processImpl(
      @NonNull final String correlationId, @NonNull final GBPersonEvent input) {
    Gson gson = (new GsonBuilder()).excludeFieldsWithoutExposeAnnotation().create();
    return gson.toJson(input);
  }
}
