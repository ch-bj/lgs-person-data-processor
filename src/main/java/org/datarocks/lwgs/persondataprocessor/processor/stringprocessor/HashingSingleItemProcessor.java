package org.datarocks.lwgs.persondataprocessor.processor.stringprocessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.exception.RequiredParameterMissing;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.exception.MessageDigestException;

@SuperBuilder
public class HashingSingleItemProcessor extends AbstractSingleItemProcessor<String> {
  public MessageDigest getMessageDigestFromConfigurationParameter() {
    Optional<String> optionalMessageDigest =
        getHandlerConfiguration()
            .getConfigurationItem(
                String.class, LWGSPersonDataProcessorParameters.PARAM_KEY_MESSAGE_DIGEST);
    if (!optionalMessageDigest.isPresent()) {
      throw new RequiredParameterMissing(
          "HashingSingleItemProcessor requires parameter "
              + LWGSPersonDataProcessorParameters.PARAM_KEY_MESSAGE_DIGEST);
    }

    try {
      return MessageDigest.getInstance(optionalMessageDigest.get());
    } catch (NoSuchAlgorithmException e) {
      throw new MessageDigestException("Invalid message digest algorithm: " + e.getMessage());
    }
  }

  @Override
  public String processImpl(String correlationId, String input) {
    return EncryptionDecryptionAndMessageDigestHelper.bytesToHex(
        getMessageDigestFromConfigurationParameter()
            .digest(input.getBytes(StandardCharsets.UTF_8)));
  }
}
