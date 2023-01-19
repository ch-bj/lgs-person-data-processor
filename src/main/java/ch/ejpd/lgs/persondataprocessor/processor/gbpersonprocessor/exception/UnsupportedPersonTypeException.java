package ch.ejpd.lgs.persondataprocessor.processor.gbpersonprocessor.exception;

import ch.ejpd.lgs.persondataprocessor.model.PersonType;

public class UnsupportedPersonTypeException extends RuntimeException {
  public UnsupportedPersonTypeException(PersonType personType) {
    super("PersonType " + personType.toString() + " is not supported.");
  }
}
