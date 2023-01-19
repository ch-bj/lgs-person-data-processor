package ch.ejpd.lgs.persondataprocessor.processor.attributeprocessor;

import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import java.text.Normalizer;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;

@SuperBuilder
public class AttributePhoneticallyNormalizeAttributeValue
    extends AbstractSingleItemProcessor<Attribute> {

  public static String flattenToAscii(String string) {
    StringBuilder sb = new StringBuilder(string.length());
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    for (char c : string.toCharArray()) {
      if (c <= '\u007F') sb.append(c);
    }
    return sb.toString();
  }

  @Override
  public Attribute processImpl(
      @NonNull final String correlationId, @NonNull final Attribute attribute) {
    Attribute processingAttribute = new Attribute(attribute);

    String value = processingAttribute.getAttributeValue();

    value = flattenToAscii(value);
    value = value.toUpperCase();

    processingAttribute.setAttributeValue(value);
    return processingAttribute;
  }
}
