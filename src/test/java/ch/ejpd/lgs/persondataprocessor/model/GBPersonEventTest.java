package ch.ejpd.lgs.persondataprocessor.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GBPersonEventTest {

  @Test
  void testGetAttributesNeverReturnsNull() {
    GBPersonEvent gbPersonEvent =
        GBPersonEvent.builder()
            .personType(PersonType.NATUERLICHE_PERSON)
            .eventType(EventType.INSERT)
            .build();

    assertNotNull(gbPersonEvent.getAttributes());
  }
}
