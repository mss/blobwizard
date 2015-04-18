package de.msquadrat.blobwizard.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.lang3.NotImplementedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import de.msquadrat.blobwizard.BlobStoreManager;

@Path("/blob/{store}/{path:.+}")
public class BlobResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobResource.class);
    
    public BlobResource(BlobStoreManager storeManager) {
    }
    
    @Deprecated
    private Object notImplemented(String method, String store, String path) {
        LOGGER.info("{} on store <{}> via path <{}>", method, store, path);
        throw new NotImplementedException(method);
    }
    
    @PUT
    @Timed
    public void putBlob(@PathParam("store") String store, @PathParam("path") String path) {
        notImplemented("PUT", store, path);
    }
    
    @GET
    @Timed
    public Object getBlob(@PathParam("store") String store, @PathParam("path") String path) {
        return notImplemented("GET", store, path);
    }
    
    @DELETE
    @Timed
    public void deleteBlob(@PathParam("store") String store, @PathParam("path") String path) {
        notImplemented("DELETE", store, path);
    }

}
