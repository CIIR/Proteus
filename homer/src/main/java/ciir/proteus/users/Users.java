package ciir.proteus.users;

import java.security.SecureRandom;

/**
 * @author jfoley.
 */
public class Users {
  private static final SecureRandom random = new SecureRandom();
  public static final int UserEmailMaxLength = 64;
  public static final int SessionIdLength = generateSessionId().length();

  public static String generateSessionId() {
    return String.format("%016x%016x", random.nextLong(), random.nextLong());
  }

}
