package exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class ConflictException extends WebApplicationException {

    public ConflictException(){
        super(Response.Status.CONFLICT);
    }
}
