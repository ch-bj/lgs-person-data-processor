package org.datarocks.lwgs.persondataprocessor.processor.stringprocessor;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.processor.AbstractSplitProcessor;

@SuperBuilder
public class EditingDistanceProcessor extends AbstractSplitProcessor<String> {
  private static final char PLACEHOLDER_CHAR = '*';

  @Override
  public List<String> processImpl(String input) {
    List<String> edtDstList = new ArrayList<>();
    edtDstList.add(input);
    for (int idx = 0; idx < input.length(); idx++) {
      edtDstList.add(insertPlaceholder(input, idx));
      edtDstList.add(replaceByPlaceholder(input, idx));
    }
    edtDstList.add(insertPlaceholder(input, input.length()));
    return edtDstList;
  }

  private String insertPlaceholder(String str, int position) {
    StringBuilder sb = new StringBuilder(str);
    sb.insert(position, PLACEHOLDER_CHAR);
    return sb.toString();
  }

  private String replaceByPlaceholder(String str, int position) {
    StringBuilder sb = new StringBuilder(str);
    sb.setCharAt(position, PLACEHOLDER_CHAR);
    return sb.toString();
  }
}
