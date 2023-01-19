package ch.ejpd.lgs.persondataprocessor.processor.stringprocessor;

import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.exception.EncryptionException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DecryptionSingleItemProcessor extends AbstractEncryptionSingleItemProcessor {
  @Override
  public String processImpl(String correlationId, String input) {
    String decrypted = null;
    try {
      decrypted =
          EncryptionDecryptionAndMessageDigestHelper.decrypt(
              getCipherFromConfigurationParameter(),
              getPrivateKeyFromConfigurationParameter(),
              Base64.getDecoder().decode(input.getBytes(StandardCharsets.UTF_8)));
    } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
      throw new EncryptionException();
    }
    return decrypted;
  }
}
