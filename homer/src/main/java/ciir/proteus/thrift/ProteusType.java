/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package ciir.proteus.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum ProteusType implements org.apache.thrift.TEnum {
  COLLECTION(0),
  PAGE(1),
  PICTURE(2),
  VIDEO(3),
  AUDIO(4),
  PERSON(5),
  LOCATION(6),
  ORGANIZATION(7),
  MISCELLANEOUS(8),
  TOPIC(9);

  private final int value;

  private ProteusType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static ProteusType findByValue(int value) { 
    switch (value) {
      case 0:
        return COLLECTION;
      case 1:
        return PAGE;
      case 2:
        return PICTURE;
      case 3:
        return VIDEO;
      case 4:
        return AUDIO;
      case 5:
        return PERSON;
      case 6:
        return LOCATION;
      case 7:
        return ORGANIZATION;
      case 8:
        return MISCELLANEOUS;
      case 9:
        return TOPIC;
      default:
        return null;
    }
  }
}
