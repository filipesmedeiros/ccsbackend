package exceptions;

public class ContainerDoesNotExistException extends Exception {

    public ContainerDoesNotExistException() {
        super("The container doesn't exist.");
    }
}
