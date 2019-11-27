package exceptions;

public class ErrorConnectingToDatabaseException extends Exception {

    public ErrorConnectingToDatabaseException() {
        super("There was an error connecting to the database. Please try again.");
    }

}
