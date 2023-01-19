package ch.ejpd.lgs.persondataprocessor.transformer.exception;

public class InvalidJsonStructure extends RuntimeException {
  public InvalidJsonStructure() {
    super();
  }

  public InvalidJsonStructure(String message) {
    super(message);
  }
}
