package org.datarocks.lwgs.persondataprocessor.processor.gbpersonprocessor.exception;

import org.datarocks.lwgs.persondataprocessor.model.PersonType;

public class UnsupportedPersonTypeException extends RuntimeException {
  public UnsupportedPersonTypeException(PersonType personType) {
    super("PersonType " + personType.toString() + " is not supported.");
  }
}
