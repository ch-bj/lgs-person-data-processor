package ch.ejpd.lgs.persondataprocessor.processor.stringprocessor;

import ch.ejpd.lgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.exception.MessageDigestException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.exception.RequiredParameterMissing;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;

@SuperBuilder
public class HashingSingleItemProcessor extends AbstractSingleItemProcessor<String> {
  public MessageDigest getMessageDigestFromConfigurationParameter() {
    Optional<String> optionalMessageDigest =
        getHandlerConfiguration()
            .getConfigurationItem(
                String.class, LWGSPersonDataProcessorParameters.PARAM_KEY_MESSAGE_DIGEST);
    if (optionalMessageDigest.isEmpty()) {
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
  public String processImpl(@NonNull final String correlationId, @NonNull final String input) {
    return EncryptionDecryptionAndMessageDigestHelper.bytesToHex(
        getMessageDigestFromConfigurationParameter()
            .digest(input.getBytes(StandardCharsets.UTF_8)));
  }
}
