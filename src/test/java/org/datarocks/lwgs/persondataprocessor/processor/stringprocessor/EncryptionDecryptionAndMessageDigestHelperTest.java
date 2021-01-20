package org.datarocks.lwgs.persondataprocessor.processor.stringprocessor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import org.junit.jupiter.api.Test;

class EncryptionDecryptionAndMessageDigestHelperTest {
  @Test
  void createKeyPair() {
    assertDoesNotThrow(
        () -> EncryptionDecryptionAndMessageDigestHelper.generateKeyPair("RSA", 2048));
  }

  @Test
  void testkeyPairFromFiles() {
    assertDoesNotThrow(
        () ->
            EncryptionDecryptionAndMessageDigestHelper.readKeyPair(
                new File("src/test/resources/public_key.pem"),
                new File("src/test/resources/private_key.pem")));
  }
}
