package de.msquadrat.blobwizard.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

@Path("/store/{store}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
public class BlobResource {
    
    public BlobResource() {
    }
    
    @PUT
    @Timed
    public void putBlob(@PathParam("store") String store) {
        
    }
    
    @GET
    @Timed
    public Object getBlob(@PathParam("store") String store) {
        return null;
    }
    
    @DELETE
    @Timed
    public void deleteBlob(@PathParam("store") String store) {
        
    }

}
