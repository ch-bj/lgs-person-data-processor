package ch.ejpd.lgs.persondataprocessor.processor.stringprocessor;

import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.exception.EncryptionException;
import java.security.InvalidKeyException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class EncryptionSingleItemProcessor extends AbstractEncryptionSingleItemProcessor {
  @Override
  public String processImpl(@NonNull final String correlationId, @NonNull final String input) {
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
