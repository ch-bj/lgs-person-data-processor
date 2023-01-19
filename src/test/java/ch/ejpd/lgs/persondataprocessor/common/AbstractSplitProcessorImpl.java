package ch.ejpd.lgs.persondataprocessor.common;

import java.util.Collections;
import java.util.List;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.processor.AbstractSplitProcessor;

@SuperBuilder
public class AbstractSplitProcessorImpl extends AbstractSplitProcessor<String> {
  @Override
  public List<String> processImpl(String input) {
    return Collections.singletonList(input);
  }
}
