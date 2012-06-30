package edu.umass.ciir.proteus.triton.core

// Trait for generating pseudo random strings and keys.
trait RandomDataGenerator {
  // Allow a-z, A-Z, and 0-9
  val keyChars: String = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).mkString("")

  // Generates a random alpha-numeric string of fixed length (8).
  // Granted, this is a BAD way to do it, because it doesn't guarantee true randomness.
  // This is only used for testing purposes, so as long as we don't generate a collision
  // no harm done.
  def genKey(length: Int = 8): String = 
    (1 to length).map(x => keyChars.charAt(util.Random.nextInt(keyChars.length))).mkString
}
