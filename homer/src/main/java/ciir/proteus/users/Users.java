package ciir.proteus.users;

import java.security.SecureRandom;

/**
 * @author jfoley.
 */
public class Users {
  public static final SecureRandom random = new SecureRandom();
  public static final int UserMaxLength = 64;
  public static final int SessionIdLength = generateSessionId().length();

  public static String generateSessionId() {
    return Long.toHexString(random.nextLong()) +
        Long.toHexString(random.nextLong());
  }

}
