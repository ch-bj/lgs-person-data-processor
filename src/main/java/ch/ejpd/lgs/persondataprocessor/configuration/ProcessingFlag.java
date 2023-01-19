package ch.ejpd.lgs.persondataprocessor.configuration;

public enum ProcessingFlag {
  NONE(1 << 0),
  HASHED(1 << 1),
  ENCRYPTED(1 << 2),
  PHONETICALLY_NORMALIZED(1 << 3),
  EDITING_DISTANCE(1 << 4);

  private final long attributeClassValue;

  ProcessingFlag(long attributeClassValue) {
    this.attributeClassValue = attributeClassValue;
  }

  public long getAttributeClassValue() {
    return attributeClassValue;
  }
}
