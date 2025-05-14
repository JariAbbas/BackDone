// org.acme.resource.GLAccountResource.java
package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.GLAccountCustomerNumbers;
import org.acme.service.GLAccountService;

@Path("/glaccounts")
@Produces(MediaType.APPLICATION_JSON)
public class GLAccountResource {

    @Inject
    GLAccountService glAccountService;

    @GET
    @Path("/customernumbers")
    public Response getCustomerNumbers() {
        GLAccountCustomerNumbers customerNumbers = glAccountService.getCustomerNumbers();

        if (customerNumbers == null || (customerNumbers.getCustomerNumber1() == null && customerNumbers.getCustomerNumber2() == null)) {
            return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"No customer numbers found.\"}").build();
        }

        return Response.ok(customerNumbers).build();
    }
}