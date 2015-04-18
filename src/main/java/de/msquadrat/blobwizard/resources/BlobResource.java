package de.msquadrat.blobwizard.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.codahale.metrics.annotation.Timed;

import de.msquadrat.blobwizard.BlobStoreManager;
import de.msquadrat.blobwizard.BlobStoreManager.Store;

@Path("/blob/{store}/{path:.+}")
public class BlobResource {
    private final BlobStoreManager stores;
    
    public BlobResource(BlobStoreManager storeManager) {
        stores = storeManager;
    }
    
    
    private Store getStore(String name) {
        Store store = stores.get(name);
        if (store == null) {
            throw new NotFoundException("Unknown BLOB store " + name);
        }
        return store;
    }
    
    
    @PUT
    @Timed
    public void putBlob(@PathParam("store") String store, @PathParam("path") String path, Object data) {
        getStore(store).put(path, data);
    }
    
    @GET
    @Timed
    public Object getBlob(@PathParam("store") String store, @PathParam("path") String path) {
        return getStore(store).get(path);
    }
    
    @DELETE
    @Timed
    public void deleteBlob(@PathParam("store") String store, @PathParam("path") String path) {
        getStore(store).delete(path);
    }

}
