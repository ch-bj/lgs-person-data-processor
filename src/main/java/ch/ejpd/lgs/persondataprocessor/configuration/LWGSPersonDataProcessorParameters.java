package ch.ejpd.lgs.persondataprocessor.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LWGSPersonDataProcessorParameters {
  public static final String PARAM_KEY_MERGE_ATTRIBUTES = "MERGE_ATTRIBUTES";
  public static final String PARAM_KEY_SUPPORTED_ATTRIBUTES = "SUPPORTED_ATTRIBUTES";
  public static final String PARAM_KEY_PUBLIC_KEY = "PUBLIC_KEY";
  public static final String PARAM_KEY_PRIVATE_KEY = "PRIVATE_KEY";
  public static final String PARAM_KEY_CIPHER = "CIPHER";
  public static final String PARAM_KEY_MESSAGE_DIGEST = "MESSAGE_DIGEST";
}
