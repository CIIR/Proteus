package ciir.proteus.parse;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jfoley
 */
public class RomanNumeralsTest {
  @Test
  public void testNumerals() {
    assertEquals(1900, XMLTrimmer.romanToDecimal("MCM"));
    assertEquals(4, XMLTrimmer.romanToDecimal("IV"));
    assertEquals(4, XMLTrimmer.romanToDecimal("iv"));
    assertEquals(54, XMLTrimmer.romanToDecimal("liv"));
    assertEquals(44, XMLTrimmer.romanToDecimal("xliv"));
    assertEquals(64, XMLTrimmer.romanToDecimal("lxiv"));

    try {
      XMLTrimmer.romanToDecimal("this isn't a roman numeral!");
      fail();
    } catch (AssertionError assertion) {
      //pass
    }
  }

  @Test
  public void testIsRoman() {
    assertFalse(XMLTrimmer.isRomanNum("IS NOT ROMAN"));
    assertTrue(XMLTrimmer.isRomanNum("IX"));

    // Try a whitelist instead of a blacklist. 7 or so legal characters 2^20ish illegal ones...
    assertFalse(XMLTrimmer.isRomanNum("~\u263a")); // smiley-face
  }
}
