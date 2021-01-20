package org.datarocks.lwgs.persondataprocessor.processor.stringprocessor;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import lombok.experimental.SuperBuilder;
import org.datarocks.banzai.exception.RequiredParameterMissing;
import org.datarocks.banzai.processor.AbstractSingleItemProcessor;
import org.datarocks.lwgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.exception.CipherException;

@SuperBuilder
public abstract class AbstractEncryptionSingleItemProcessor
    extends AbstractSingleItemProcessor<String> {
  private static final String REQUIRED_PARAMETER_MISSING_MESSAGE =
      "EncryptionSingleItemProcessor requires parameter ";
  private static final String INVALID_ENCRYPTION_ALGORITHM_MESSAGE =
      "Invalid encryption algorithm: ";

  public PublicKey getPublicKeyFromConfigurationParameter() {
    Optional<String> optionalPublicKey =
        getHandlerConfiguration()
            .getConfigurationItem(
                String.class, LWGSPersonDataProcessorParameters.PARAM_KEY_PUBLIC_KEY);
    if (!optionalPublicKey.isPresent()) {
      throw new RequiredParameterMissing(
          REQUIRED_PARAMETER_MISSING_MESSAGE
              + LWGSPersonDataProcessorParameters.PARAM_KEY_PUBLIC_KEY);
    }

    return EncryptionDecryptionAndMessageDigestHelper.getPublicKey(
        EncryptionDecryptionAndMessageDigestHelper.decodeBase64(optionalPublicKey.get()));
  }

  public PrivateKey getPrivateKeyFromConfigurationParameter() {
    Optional<String> optionalPrivateKey =
        getHandlerConfiguration()
            .getConfigurationItem(
                String.class, LWGSPersonDataProcessorParameters.PARAM_KEY_PRIVATE_KEY);
    if (!optionalPrivateKey.isPresent()) {
      throw new RequiredParameterMissing(
          REQUIRED_PARAMETER_MISSING_MESSAGE
              + LWGSPersonDataProcessorParameters.PARAM_KEY_PRIVATE_KEY);
    }

    return EncryptionDecryptionAndMessageDigestHelper.getPrivateKey(
        EncryptionDecryptionAndMessageDigestHelper.decodeBase64(optionalPrivateKey.get()));
  }

  public Cipher getCipherFromConfigurationParameter() {
    Optional<String> optionalCipher =
        getHandlerConfiguration()
            .getConfigurationItem(String.class, LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER);
    if (!optionalCipher.isPresent()) {
      throw new RequiredParameterMissing(
          REQUIRED_PARAMETER_MISSING_MESSAGE + LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER);
    }

    try {
      return Cipher.getInstance(optionalCipher.get());
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new CipherException(INVALID_ENCRYPTION_ALGORITHM_MESSAGE + e.getMessage());
    }
  }
}
