package de.msquadrat.blobwizard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.google.common.io.ByteStreams;

import de.msquadrat.blobwizard.BlobStoreManager;
import de.msquadrat.blobwizard.BlobStoreManager.BlobException;
import de.msquadrat.blobwizard.BlobStoreManager.Store;

@Path("/blob/{store}/{container}/{path:.+}")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
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
            @PathParam("path") String path, InputStream in) throws IOException {
        trace("PUT", store, container, path);
        
        try {
            getStore(store).put(container, path, in);
        }
        catch (BlobException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @GET
    @Timed
    public StreamingOutput getBlob(@PathParam("store") String store,
            @PathParam("container") String container,
            @PathParam("path") String path) throws IOException {
        trace("GET", store, container, path);
        
        final InputStream in;
        try {
            in = getStore(store).get(container, path).orNull();
        }
        catch (BlobException e) {
            throw new InternalServerErrorException(e);
        }
        if (in == null) {
            LOGGER.debug("Blob {} not found in container {}", path, container);
            throw new NotFoundException();
        }
        
        return new StreamingBlobOutput(in);
    }

    @DELETE
    @Timed
    public void deleteBlob(@PathParam("store") String store,
            @PathParam("container") String container,
            @PathParam("path") String path) throws IOException {
        trace("DELETE", store, container, path);
        
        try {
            getStore(store).delete(container, path);
        }
        catch (BlobException e) {
            throw new InternalServerErrorException(e);
        }
    }
    
    private void trace(String method, String store, String container, String path) {
        LOGGER.trace("{} on {}:{}:{}", method, store, container, path);
    }

    
    public static class StreamingBlobOutput implements StreamingOutput {
        private final InputStream in;
        
        public StreamingBlobOutput(InputStream in) {
            this.in = in;
        }

        @Override
        public void write(OutputStream out) throws IOException,
                WebApplicationException {
            ByteStreams.copy(in, out);
        }
    }
}
