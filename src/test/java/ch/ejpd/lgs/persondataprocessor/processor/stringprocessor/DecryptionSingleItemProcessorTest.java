package ch.ejpd.lgs.persondataprocessor.processor.stringprocessor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import ch.ejpd.lgs.persondataprocessor.configuration.LWGSPersonDataProcessorParameters;
import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.exception.CipherException;
import ch.ejpd.lgs.persondataprocessor.processor.stringprocessor.exception.EncryptionException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Stream;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.NonNull;
import org.datarocks.banzai.configuration.HandlerConfiguration;
import org.datarocks.banzai.exception.RequiredParameterMissing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DecryptionSingleItemProcessorTest {
  private static final String CORRELATION_ID = UUID.randomUUID().toString();

  static Stream<Arguments> encryptionTests() throws NoSuchAlgorithmException {
    return Stream.of(
        arguments(
            "RSA/ECB/PKCS1Padding",
            EncryptionDecryptionAndMessageDigestHelper.generateKeyPair("RSA", 2048),
            EncryptionDecryptionAndMessageDigestHelper.generateSHA256Hash(
                "Message to be encrypted")),
        arguments(
            "RSA/ECB/PKCS1Padding",
            EncryptionDecryptionAndMessageDigestHelper.generateKeyPair("RSA", 1024),
            EncryptionDecryptionAndMessageDigestHelper.generateSHA256Hash(
                "Message to be encrypted")),
        arguments(
            "RSA",
            EncryptionDecryptionAndMessageDigestHelper.generateKeyPair("RSA", 2048),
            EncryptionDecryptionAndMessageDigestHelper.generateSHA256Hash(
                "Message to be encrypted")),
        arguments(
            "RSA/ECB/PKCS1Padding",
            EncryptionDecryptionAndMessageDigestHelper.generateKeyPair("RSA", 1024),
            EncryptionDecryptionAndMessageDigestHelper.generateSHA256Hash(
                "Message to be encrypted")));
  }

  private static void printKeySpec(KeyPair keyPair) {
    System.out.println(
        "PublicKey: Algorithm["
            + keyPair.getPublic().getAlgorithm()
            + "] Format["
            + keyPair.getPublic().getFormat()
            + "]");

    System.out.println("-----BEGIN PUBLIC KEY-----");
    System.out.println(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
    System.out.println("-----END PUBLIC KEY-----");

    System.out.println(
        "PrivateKey: Algorithm["
            + keyPair.getPrivate().getAlgorithm()
            + "] Format["
            + keyPair.getPrivate().getFormat()
            + "]");

    System.out.println("-----BEGIN PRIVATE KEY-----");
    System.out.println(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
    System.out.println("-----END PRIVATE KEY-----");
  }

  @Test
  void testExceptionOnAllMissingMandatoryParameter() {
    final DecryptionSingleItemProcessor decryptionSingleItemProcessor1 =
        DecryptionSingleItemProcessor.builder()
            .handlerConfiguration(HandlerConfiguration.builder().build())
            .build();
    assertThrows(
        RequiredParameterMissing.class,
        () -> decryptionSingleItemProcessor1.process(CORRELATION_ID, "String to be encrypted"));

    final DecryptionSingleItemProcessor decryptionSingleItemProcessor2 =
        DecryptionSingleItemProcessor.builder()
            .handlerConfiguration(
                HandlerConfiguration.builder()
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER, "RSA")
                    .build())
            .build();

    assertThrows(
        RequiredParameterMissing.class,
        () -> decryptionSingleItemProcessor2.process(CORRELATION_ID, "String to be encrypted"));
  }

  @Test
  void testInvalidPadding() {
    final DecryptionSingleItemProcessor decryptionSingleItemProcessor =
        DecryptionSingleItemProcessor.builder()
            .handlerConfiguration(
                HandlerConfiguration.builder()
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER,
                        "RSA/ECB/PKCS100Padding")
                    .build())
            .build();

    assertThrows(
        CipherException.class,
        () -> decryptionSingleItemProcessor.process(CORRELATION_ID, "String to be encrypted"));
  }

  @Test
  void testInvalidPublicKey() {
    final EncryptionSingleItemProcessor encryptionSingleItemProcessor =
        EncryptionSingleItemProcessor.builder()
            .handlerConfiguration(
                HandlerConfiguration.builder()
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER, "RSA/ECB/PKCS1Padding")
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_PUBLIC_KEY, "invalid")
                    .build())
            .build();

    assertThrows(
        EncryptionException.class,
        () -> encryptionSingleItemProcessor.process(CORRELATION_ID, "String to be encrypted"));
  }

  @Test
  void encryptionTest()
      throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
          NoSuchPaddingException, BadPaddingException {
    KeyPair keyPair = EncryptionDecryptionAndMessageDigestHelper.generateKeyPair("RSA", 2048);

    String message = "Message to be encrypted";

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    byte[] encryptedMessage =
        EncryptionDecryptionAndMessageDigestHelper.encrypt(cipher, keyPair.getPublic(), message);
    String encryptedMessageBase64 =
        EncryptionDecryptionAndMessageDigestHelper.encodeBase64(encryptedMessage);
    byte[] encryptedDecoded =
        EncryptionDecryptionAndMessageDigestHelper.decodeBase64(encryptedMessageBase64);
    String decryptedMessage =
        EncryptionDecryptionAndMessageDigestHelper.decrypt(
            cipher, keyPair.getPrivate(), encryptedDecoded);

    assertEquals(message, decryptedMessage);
  }

  @ParameterizedTest
  @DisplayName("Test encryption / decryption process")
  @MethodSource("encryptionTests")
  void testHappyPath(
      @NonNull final String cipherSpec,
      @NonNull final KeyPair keyPair,
      @NonNull final String message)
      throws NoSuchPaddingException, NoSuchAlgorithmException {
    // printKeySpec(keyPair);

    Cipher cipher = Cipher.getInstance(cipherSpec);

    EncryptionSingleItemProcessor encryptionSingleItemProcessor =
        EncryptionSingleItemProcessor.builder()
            .handlerConfiguration(
                HandlerConfiguration.builder()
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_PUBLIC_KEY,
                        Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()))
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER, cipherSpec)
                    .build())
            .build();
    String encryptedBase64Message = encryptionSingleItemProcessor.process(CORRELATION_ID, message);

    DecryptionSingleItemProcessor decryptionSingleItemProcessor =
        DecryptionSingleItemProcessor.builder()
            .handlerConfiguration(
                HandlerConfiguration.builder()
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_PRIVATE_KEY,
                        Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()))
                    .handlerConfigurationItem(
                        LWGSPersonDataProcessorParameters.PARAM_KEY_CIPHER, cipherSpec)
                    .build())
            .build();
    String decryptedMessage =
        decryptionSingleItemProcessor.process(CORRELATION_ID, encryptedBase64Message);

    assertEquals(message, decryptedMessage);
  }
}
