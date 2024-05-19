package com.gvtech;

import com.gvtech.core.ContentDependant;
import com.gvtech.core.ContentDependency;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.service.ContentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;


@Path("")
public class ExampleResource {

    @Inject
    ContentService contentService;

    @GET
    @Path("/run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Object hello(final Request request) {
        return contentService.get(new ContentType(request.getType()), new ContentId(request.getId()));
    }





}
