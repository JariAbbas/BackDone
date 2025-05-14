package org.acme.resource;

import io.agroal.api.AgroalDataSource;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Path("/currency")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CurrencyResource {

    @Inject
    AgroalDataSource dataSource;

    @GET
    @Path("/{currencyCode}")
    public Response getCurrencyInfo(@PathParam("currencyCode") int currencyCode){
        String query = "Select CurrencyName, CurrencyRate from CurrencyTable where CurrencyCode = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, currencyCode);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()){
                Map<String, Object> result = new HashMap<>();
                result.put("CurrencyName", resultSet.getString("CurrencyName"));
                result.put("CurrencyRate", resultSet.getDouble("CurrencyRate"));
                return  Response.ok(result).build();
            }else {
                return Response.status(Response.Status.NOT_FOUND).entity("Currency Not Found").build();
            }

        } catch (Exception e) {
            return Response.serverError().entity("Database error: " + e.getMessage()).build();
        }
    }



}
