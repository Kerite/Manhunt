package top.kerite.manhunt.manhuntexception;

public class InvalidRoleException extends ManHuntException {
    public InvalidRoleException(String role) {
        super("Invalid role " + role);
    }
}
