package com.gvtech.client;

import com.gvtech.client.request.Request;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;


@RegisterRestClient(configKey = "content-search-client")
@ClientHeaderParam(name = "Accept", value = "application/json")
public interface ContentSearchClient {


    @POST
    @Path("/run")
    List<Long> search(final Request request, @HeaderParam("Language") String language);


}
