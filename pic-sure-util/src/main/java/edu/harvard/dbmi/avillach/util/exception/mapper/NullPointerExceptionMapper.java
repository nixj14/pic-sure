package edu.harvard.dbmi.avillach.util.exception.mapper;

import edu.harvard.dbmi.avillach.util.PicsureNaming;
import edu.harvard.dbmi.avillach.util.response.PICSUREResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static edu.harvard.dbmi.avillach.util.PicsureNaming.ExceptionMessages.INTERNAL_SYSTEM_ERROR;

@Provider
public class NullPointerExceptionMapper implements ExceptionMapper<NullPointerException>{

    @Override
    public Response toResponse(NullPointerException exception) {
        exception.printStackTrace();
        return PICSUREResponse.applicationError(PicsureNaming.ExceptionMessages.INTERNAL_SYSTEM_ERROR);
    }
}
