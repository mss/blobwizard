/**
 * Copyright (c) 2015 Malte S. Stretz <http://msquadrat.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
            in = getStore(store).get(container, path).orElseThrow(NotFoundException::new);
        }
        catch (BlobException e) {
            throw new InternalServerErrorException(e);
        }
        catch (NotFoundException e) {
            LOGGER.debug("Blob {} not found in container {}", path, container);
            throw e;
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
