package de.msquadrat.blobwizard.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import de.msquadrat.blobwizard.BlobStoreManager;
import de.msquadrat.blobwizard.BlobStoreManager.Store;

@Path("/blob/{store}/{container}/{path:.+}")
public class BlobResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobResource.class);
    
    private final BlobStoreManager stores;

    public BlobResource(BlobStoreManager storeManager) {
        stores = storeManager;
    }

    private Store getStore(String name) {
        Store store = stores.get(name);
        if (store == null) {
            LOGGER.debug("Store {} not found", name);
            throw new NotFoundException("Unknown BLOB store " + name);
        }
        return store;
    }

    @PUT
    @Timed
    public void putBlob(@PathParam("store") String store,
            @PathParam("container") String container,
            @PathParam("path") String path, Object data) {
        getStore(store).put(container, path, data);
    }

    @GET
    @Timed
    public Object getBlob(@PathParam("store") String store,
            @PathParam("container") String container,
            @PathParam("path") String path) {
        Object blob = getStore(store).get(container, path).orNull();
        if (blob == null) {
            throw new NotFoundException();
        }
        return blob;
    }

    @DELETE
    @Timed
    public void deleteBlob(@PathParam("store") String store,
            @PathParam("container") String container,
            @PathParam("path") String path) {
        getStore(store).delete(container, path);
    }

}
