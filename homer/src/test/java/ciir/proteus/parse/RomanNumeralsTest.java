package ciir.proteus.parse;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jfoley
 */
public class RomanNumeralsTest {
  @Test
  public void testNumerals() {
    assertEquals(1900, RomanNumeral.romanToDecimal("MCM"));
    assertEquals(4, RomanNumeral.romanToDecimal("IV"));
    assertEquals(4, RomanNumeral.romanToDecimal("iv"));
    assertEquals(54, RomanNumeral.romanToDecimal("liv"));
    assertEquals(44, RomanNumeral.romanToDecimal("xliv"));
    assertEquals(64, RomanNumeral.romanToDecimal("lxiv"));

    try {
      RomanNumeral.romanToDecimal("this isn't a roman numeral!");
      fail();
    } catch (AssertionError assertion) {
      //pass
    }
  }

  @Test
  public void testIsRoman() {
    assertFalse(RomanNumeral.isRomanNum("IS NOT ROMAN"));
    assertTrue(RomanNumeral.isRomanNum("IX"));

    // Try a whitelist instead of a blacklist. 7 or so legal characters 2^20ish illegal ones...
    assertFalse(RomanNumeral.isRomanNum("~\u263a")); // smiley-face
  }
}
