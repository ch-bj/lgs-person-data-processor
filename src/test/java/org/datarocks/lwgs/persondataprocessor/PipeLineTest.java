package org.datarocks.lwgs.persondataprocessor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.banzai.pipeline.PipeLine;
import org.datarocks.banzai.pipeline.exception.HeadTransformerRequiredException;
import org.datarocks.banzai.pipeline.exception.TailsTransformerRequiredException;
import org.datarocks.banzai.processor.PassTroughProcessor;
import org.datarocks.banzai.transformer.PassTroughTransformer;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.datarocks.lwgs.persondataprocessor.configuration.model.SupportedAttributes;
import org.datarocks.lwgs.persondataprocessor.model.Attribute;
import org.datarocks.lwgs.persondataprocessor.model.EventType;
import org.datarocks.lwgs.persondataprocessor.model.GBPersonEvent;
import org.datarocks.lwgs.persondataprocessor.model.PersonType;
import org.datarocks.lwgs.persondataprocessor.processor.AttributeSubPipeline;
import org.datarocks.lwgs.persondataprocessor.processor.attributeprocessor.AttributeGenerateSearchTerms;
import org.datarocks.lwgs.persondataprocessor.processor.attributeprocessor.AttributePhoneticallyNormalizeAttributeValue;
import org.datarocks.lwgs.persondataprocessor.processor.attributeprocessor.AttributeSearchTermsDecryptor;
import org.datarocks.lwgs.persondataprocessor.processor.attributeprocessor.AttributeSearchTermsEncryptor;
import org.datarocks.lwgs.persondataprocessor.processor.attributeprocessor.AttributeSearchTermsHashing;
import org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.GBPersonEventAttributeValidator;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.EncryptionDecryptionAndMessageDigestHelper;
import org.datarocks.lwgs.persondataprocessor.transformer.GBPersonRequestJsonDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PipeLineTest {
  private static final String CIPHER_SPEC = "RSA/ECB/PKCS1Padding";
  private static final String MESSAGE_DIGEST = "SHA-512";

  private static final String NATURAL_PERSON_JSON =
      "{\"metaData\":{\"personType\":\"NATUERLICHE_PERSON\",\"eventType\":\"INSERT\", \"EGBPID\":\"CH000000000000\"},\"natuerlichePerson\":{\"name\":\"Smith\",\"vorname\":\"John\",\"jahrgang\":\"1970\",\"geburtsdatum\":\"01.01.1970\",\"ahvStatus\":\"UNKNOWN\",\"ahv\":\"123456\"}}";

  private static HandlerConfiguration handlerConfiguration;

  @BeforeAll
  static void setup() throws IOException, NoSuchAlgorithmException {
    KeyPair keyPair = EncryptionDecryptionAndMessageDigestHelper.generateKeyPair("RSA", 2048);

    handlerConfiguration =
        HandlerConfiguration.builder()
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_SUPPORTED_ATTRIBUTES,
                SupportedAttributes.fromJson(
                    TestHelper.readSupportedAttributesScheme(),
                    TestHelper.readSupportedAttributes()))
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_PUBLIC_KEY,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()))
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_PRIVATE_KEY,
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()))
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER, CIPHER_SPEC)
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_MESSAGE_DIGEST, MESSAGE_DIGEST)
            .handlerConfigurationItem(
                LWGSPersonDataProcessorParameters.PARAM_KEY_MERGE_ATTRIBUTES, "false")
            .build();
  }

  @Test
  void dummyPipelineTestHappyPath() {
    PipeLine<Object, Object, Object> objectPipeline =
        PipeLine.builder(handlerConfiguration, Object.class, Object.class, Object.class)
            .addHeadTransformer(PassTroughTransformer.builder().build())
            .addStep(PassTroughProcessor.builder().build())
            .addTailTransformer(PassTroughTransformer.builder().build())
            .build();
    assertDoesNotThrow(() -> objectPipeline.process(UUID.randomUUID().toString(), "Test"));
  }

  @Test
  void dummyPipelineTestNoHeadTransformer() {
    PipeLine.PipeLineBuilder<Object, Object, Object> builder =
        PipeLine.builder(handlerConfiguration, Object.class, Object.class, Object.class);
    assertThrows(HeadTransformerRequiredException.class, builder::build);
  }

  @Test
  void dummyPipelineTestNoTailsTransformer() {
    PipeLine.PipeLineBuilder<Object, Object, Object> builder =
        PipeLine.builder(handlerConfiguration, Object.class, Object.class, Object.class)
            .addHeadTransformer(PassTroughTransformer.builder().build());
    assertThrows(TailsTransformerRequiredException.class, builder::build);
  }

  @Test
  void dummyPipelineTestNoProcessorSteps() {
    PipeLine<Object, Object, Object> pipeLine =
        PipeLine.builder(handlerConfiguration, Object.class, Object.class, Object.class)
            .addHeadTransformer(PassTroughTransformer.builder().build())
            .addTailTransformer(PassTroughTransformer.builder().build())
            .build();
    assertDoesNotThrow(() -> pipeLine.process(UUID.randomUUID().toString(), "Test"));
  }

  PipeLine<String, GBPersonEvent, GBPersonEvent> buildGBPersonForwardEventPipelineFromBuilder() {
    PipeLine<Attribute, Attribute, Attribute> attributePipeLine =
        PipeLine.builder(handlerConfiguration, Attribute.class, Attribute.class, Attribute.class)
            .addHeadTransformer(PassTroughTransformer.<Attribute>builder().build())
            .addStep(AttributePhoneticallyNormalizeAttributeValue.builder().build())
            .addStep(AttributeGenerateSearchTerms.builder().build())
            .addStep(AttributeSearchTermsHashing.builder().build())
            .addStep(AttributeSearchTermsEncryptor.builder().build())
            .addTailTransformer(PassTroughTransformer.<Attribute>builder().build())
            .build();

    return PipeLine.builder(
            handlerConfiguration, String.class, GBPersonEvent.class, GBPersonEvent.class)
        .addHeadTransformer(GBPersonRequestJsonDeserializer.builder().build())
        .addStep(GBPersonEventAttributeValidator.builder().build())
        .addStep(AttributeSubPipeline.builder().attributePipeLine(attributePipeLine).build())
        .addTailTransformer(PassTroughTransformer.<GBPersonEvent>builder().build())
        .build();
  }

  PipeLine<GBPersonEvent, GBPersonEvent, GBPersonEvent>
      buildGBPersonReversEventPipelineFromBuilder() {
    PipeLine<Attribute, Attribute, Attribute> attributePipeLine =
        PipeLine.builder(handlerConfiguration, Attribute.class, Attribute.class, Attribute.class)
            .addHeadTransformer(PassTroughTransformer.<Attribute>builder().build())
            .addStep(AttributeSearchTermsDecryptor.<Attribute>builder().build())
            .addTailTransformer(PassTroughTransformer.<Attribute>builder().build())
            .build();

    return PipeLine.builder(
            handlerConfiguration, GBPersonEvent.class, GBPersonEvent.class, GBPersonEvent.class)
        .addHeadTransformer(PassTroughTransformer.<GBPersonEvent>builder().build())
        .addStep(AttributeSubPipeline.builder().attributePipeLine(attributePipeLine).build())
        .addTailTransformer(PassTroughTransformer.<GBPersonEvent>builder().build())
        .build();
  }

  @Test
  void testForwardAndReversePipeline() {
    final PipeLine<String, GBPersonEvent, GBPersonEvent> forwardPipeline =
        buildGBPersonForwardEventPipelineFromBuilder();

    final PipeLine<GBPersonEvent, GBPersonEvent, GBPersonEvent> reversePipeline =
        buildGBPersonReversEventPipelineFromBuilder();

    GBPersonEvent processedGBPersonEvent =
        forwardPipeline.process(UUID.randomUUID().toString(), NATURAL_PERSON_JSON);

    assertEquals(PersonType.NATUERLICHE_PERSON, processedGBPersonEvent.getPersonType());
    assertEquals(EventType.INSERT, processedGBPersonEvent.getEventType());
    assertNotNull(processedGBPersonEvent.getAttributes());
    assertEquals(6, processedGBPersonEvent.getAttributes().size());
    assertTrue(processedGBPersonEvent.getAttributes().stream().map(Attribute::getAttributeName).collect(
        Collectors.toSet()).containsAll(Set.of("vorname", "name", "jahrgang", "geburtsdatum", "ahv", "ahvStatus")));

    GBPersonEvent decryptedGBPersonEvent =
        reversePipeline.process(UUID.randomUUID().toString(), processedGBPersonEvent);
    assertNotNull(decryptedGBPersonEvent);

    assertEquals(processedGBPersonEvent, decryptedGBPersonEvent);
  }
}
