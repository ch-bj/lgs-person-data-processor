package org.datarocks.lwgs.persondataprocessor.processor.stringprocessor;

import java.security.InvalidKeyException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import lombok.experimental.SuperBuilder;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.exception.EncryptionException;

@SuperBuilder
public class EncryptionSingleItemProcessor extends AbstractEncryptionSingleItemProcessor {
  @Override
  public String processImpl(String correlationId, String input) {
    String encrypted = null;
    try {
      encrypted =
          Base64.getEncoder()
              .encodeToString(
                  EncryptionDecryptionAndMessageDigestHelper.encrypt(
                      getCipherFromConfigurationParameter(),
                      getPublicKeyFromConfigurationParameter(),
                      input));
    } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
      throw new EncryptionException();
    }
    return encrypted;
  }
}
