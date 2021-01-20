package org.datarocks.lwgs.persondataprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;

public class TestHelper {
  public static String readSupportedAttributes() throws IOException {
    Path path = Paths.get("src", "main", "resources", "SupportedAttributes.json");
    return new String(Files.readAllBytes(path));
  }

  public static String readSupportedAttributesScheme() throws IOException {
    Path path = Paths.get("src", "main", "resources", "schemas", "SupportedAttributesSchema.json");
    return new String(Files.readAllBytes(path));
  }

  public static List<String> generateRandomArguments(
      int minNumArguments, int maxNumArguments, int stringSize) {
    List<String> argumentList = new ArrayList<>();
    final int numberOfArguments =
        (int) (Math.random() * (maxNumArguments - minNumArguments + 1) + minNumArguments);
    for (int i = 0; i < numberOfArguments; i++) {
      argumentList.add(RandomStringUtils.random(4096, true, true));
    }
    return argumentList;
  }
}
