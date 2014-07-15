package ciir.proteus.users.error;

/**
 *
 * @author michaelz
 */
public class DuplicateUser extends DBError {

    public DuplicateUser() {
        super("Duplicate user.");
    }
}
