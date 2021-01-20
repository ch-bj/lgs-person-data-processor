package org.datarocks.lwgs.persondataprocessor.processor.stringprocessor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.datarocks.lwgs.persondataprocessor.processor.stringprocessor.exception.EncryptionException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncryptionDecryptionAndMessageDigestHelper {

  public static PublicKey getPublicKey(byte[] publicKeyByteArray) {
    try {
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByteArray);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new EncryptionException();
    }
  }

  public static PrivateKey getPrivateKey(byte[] privateKeyByteArray) {
    try {
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByteArray);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new EncryptionException();
    }
  }

  public static byte[] decodeBase64(String base64EncodedData) {
    return Base64.getDecoder().decode(base64EncodedData);
  }

  public static String encodeBase64(byte[] data) {
    return Base64.getEncoder().encodeToString(data);
  }

  public static byte[] encrypt(
      final @NonNull Cipher cipher, final @NonNull PublicKey publicKey, final String message)
      throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    return cipher.doFinal(message.getBytes());
  }

  public static String decrypt(
      final @NonNull Cipher cipher,
      final @NonNull PrivateKey privateKey,
      final @NonNull byte[] encryptedMessage)
      throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    cipher.init(Cipher.DECRYPT_MODE, privateKey);
    return new String(cipher.doFinal(encryptedMessage), StandardCharsets.UTF_8);
  }

  public static KeyPair generateKeyPair(String keyType, int keyLength)
      throws NoSuchAlgorithmException {
    KeyPairGenerator keGen = KeyPairGenerator.getInstance(keyType);
    keGen.initialize(keyLength);
    return keGen.generateKeyPair();
  }

  public static String generateSHA256Hash(String message) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static RSAPublicKey readPublicKey(@NonNull final File file)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    KeyFactory factory = KeyFactory.getInstance("RSA");

    try (FileReader keyReader = new FileReader(file);
        PemReader pemReader = new PemReader(keyReader)) {

      PemObject pemObject = pemReader.readPemObject();
      byte[] content = pemObject.getContent();
      X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
      return (RSAPublicKey) factory.generatePublic(pubKeySpec);
    }
  }

  public static RSAPrivateKey readPrivateKey(@NonNull final File file)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    KeyFactory factory = KeyFactory.getInstance("RSA");

    try (FileReader keyReader = new FileReader(file);
        PemReader pemReader = new PemReader(keyReader)) {

      PemObject pemObject = pemReader.readPemObject();
      byte[] content = pemObject.getContent();
      PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
      return (RSAPrivateKey) factory.generatePrivate(privKeySpec);
    }
  }

  public static KeyPair readKeyPair(
      @NonNull final File publicKeyFile, @NonNull final File privateKeyFile)
      throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
    RSAPublicKey publicKey = readPublicKey(publicKeyFile);
    RSAPrivateKey privateKey = readPrivateKey(privateKeyFile);

    return new KeyPair(publicKey, privateKey);
  }

  public static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString().toUpperCase();
  }
}
