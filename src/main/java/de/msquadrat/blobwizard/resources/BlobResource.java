package de.msquadrat.blobwizard.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import de.msquadrat.blobwizard.BlobStoreManager;

@Path("/blob/{store}/{path:.+}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
public class BlobResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobResource.class);
    
    public BlobResource(BlobStoreManager storeManager) {
    }
    
    @PUT
    @Timed
    public void putBlob(@PathParam("store") String store, @PathParam("path") String path) {
        
    }
    
    @GET
    @Timed
    public Object getBlob(@PathParam("store") String store, @PathParam("path") String path) {
        return null;
    }
    
    @DELETE
    @Timed
    public void deleteBlob(@PathParam("store") String store, @PathParam("path") String path) {
        
    }

}
