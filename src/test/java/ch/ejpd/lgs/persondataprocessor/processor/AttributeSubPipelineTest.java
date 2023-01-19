package ch.ejpd.lgs.persondataprocessor.processor;

import static org.junit.jupiter.api.Assertions.*;

import ch.ejpd.lgs.persondataprocessor.model.Attribute;
import ch.ejpd.lgs.persondataprocessor.model.EventType;
import ch.ejpd.lgs.persondataprocessor.model.GBPersonEvent;
import ch.ejpd.lgs.persondataprocessor.model.PersonType;
import java.util.UUID;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.banzai.pipeline.PipeLine;
import org.datarocks.banzai.processor.PassTroughProcessor;
import org.datarocks.banzai.transformer.PassTroughTransformer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AttributeSubPipelineTest {

  static AttributeSubPipeline.AttributeSubPipelineBuilder<?, ?> builder;
  static HandlerConfiguration handlerConfiguration;
  static PipeLine<Attribute, Attribute, Attribute> pipeLine;

  @BeforeAll
  static void setup() {
    builder = AttributeSubPipeline.builder();
    handlerConfiguration = HandlerConfiguration.builder().build();
    pipeLine =
        PipeLine.builder(handlerConfiguration, Attribute.class, Attribute.class, Attribute.class)
            .addHeadTransformer(
                PassTroughTransformer.<Attribute>builder()
                    .handlerConfiguration(handlerConfiguration)
                    .build())
            .addTailTransformer(
                PassTroughTransformer.<Attribute>builder()
                    .handlerConfiguration(handlerConfiguration)
                    .build())
            .build();
  }

  @Test
  void testExceptionOnPipelineAndHandlerConfigMissing() {
    assertThrows(NullPointerException.class, () -> builder.build());
  }

  @Test
  void testExceptionOnPipelineMissing() {
    final AttributeSubPipeline.AttributeSubPipelineBuilder<?, ?> builder =
        AttributeSubPipeline.builder().handlerConfiguration(handlerConfiguration);

    assertThrows(NullPointerException.class, builder::build);
  }

  @Test
  void testMandatoryParameters() {
    final AttributeSubPipeline.AttributeSubPipelineBuilder<?, ?> builder =
        AttributeSubPipeline.builder()
            .handlerConfiguration(handlerConfiguration)
            .attributePipeLine(pipeLine);

    assertDoesNotThrow(builder::build);
  }

  @Test
  void testAttributeSubPipeline() {
    PipeLine<Attribute, Attribute, Attribute> attributePipeLine =
        PipeLine.builder(handlerConfiguration, Attribute.class, Attribute.class, Attribute.class)
            .addHeadTransformer(PassTroughTransformer.<Attribute>builder().build())
            .addStep(
                PassTroughProcessor.<Attribute>builder()
                    .handlerConfiguration(handlerConfiguration)
                    .build())
            .addTailTransformer(PassTroughTransformer.<Attribute>builder().build())
            .build();

    final AttributeSubPipeline attributeSubPipeline =
        AttributeSubPipeline.builder()
            .handlerConfiguration(handlerConfiguration)
            .attributePipeLine(attributePipeLine)
            .build();

    final GBPersonEvent gbPersonEvent =
        GBPersonEvent.builder()
            .personType(PersonType.NATUERLICHE_PERSON)
            .eventType(EventType.INSERT)
            .build();
    Assertions.assertEquals(
        gbPersonEvent, attributeSubPipeline.process(UUID.randomUUID().toString(), gbPersonEvent));
  }
}
