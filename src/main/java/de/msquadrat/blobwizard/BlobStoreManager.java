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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.io.input.ProxyInputStream;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.io.Payload;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

import io.dropwizard.lifecycle.Managed;

public class BlobStoreManager implements Managed {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreManager.class);
    
    private final Map<String, Store> stores = new HashMap<>();
    
    
    public BlobStoreManager(ServerConfiguration config) {
        for (Map.Entry<String, ServerConfiguration.Store> store : config.getStores().entrySet()) {
            String name = store.getKey();
            stores.put(name, new Store(name, store.getValue()));
        }
    }
    
    @Override
    public void start() throws Exception {
        for (Store store : stores.values()) {
            store.start();
        }
    }

    @Override
    public void stop() throws Exception {
        for (Store store : stores.values()) {
            try {
                store.stop();
            }
            catch (Exception e) {
                LOGGER.warn("Exception while stopping store: {}", e.getMessage(), e);
            }
        }
    }
    
    public Store get(String name) {
        return stores.get(name);
    }


    public static class Store implements Managed {
        private final String name;
        private final BlobStoreContext context;

        public Store(String name, ServerConfiguration.Store config) {
            this.name = name;
            
            Properties overrides = new Properties();
            for (Map.Entry<String, String> option : config.getOptions().entrySet()) {
                String key = "jclouds." + option.getKey();
                overrides.put(key, option.getValue());
            }

            Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());
            
            ContextBuilder builder = ContextBuilder.newBuilder(config.getApi())
                    .modules(modules)
                    .overrides(overrides);
            if (!Strings.isNullOrEmpty(config.getIdentity())) {
                builder.credentials(config.getIdentity(), config.getCredential());
            }
            context = builder.buildView(BlobStoreContext.class);
        }
        
        @Override
        public void start() throws Exception {
            LOGGER.info("Started store {}", name);
        }

        @Override
        public void stop() throws Exception {
            try {
                context.close();
            }
            finally {
                LOGGER.info("Stopped store {}", name);
            }
        }

        public void put(String container, String path, InputStream in) throws IOException {
            BlobStore store = context.getBlobStore();
            Blob blob = store.blobBuilder(path)
                    .payload(in)
                    .build();
            try {
                store.putBlob(container, blob);
            }
            catch (Exception e) {
                throw BlobException.forPut(this, container, path, e);
            }
        }

        public Optional<InputStream> get(String container, String path) throws IOException {
            Blob blob;
            try {
                blob = context.getBlobStore().getBlob(container, path);
            }
            catch (Exception e) {
                throw BlobException.forGet(this, container, path, e);
            }
            
            if (blob == null) {
                return Optional.empty();
            }
            InputStream in = new BlobInputStream(blob);
            return Optional.of(in);
        }

        public void delete(String container, String path) throws IOException {
            try {
                context.getBlobStore().removeBlob(container, path);
            }
            catch (Exception e) {
                throw BlobException.forDelete(this, container, path, e);
            }
        }
    }

    public static class BlobInputStream extends ProxyInputStream {
        private final Payload payload;

        public BlobInputStream(Blob blob) throws IOException {
            this(blob.getPayload());
        }
        
        private BlobInputStream(Payload payload) throws IOException {
            super(payload.openStream());
            
            this.payload = payload;
        }
        
        @Override
        public void close() throws IOException {
            try {
                in.close();
            }
            finally {
                payload.close();
            }
        }
    }

    public static class BlobException extends IOException {
        private static final long serialVersionUID = 1L;

        public static BlobException forPut(Store store, String container, String path, Throwable cause) {
            return forRequest("PUT", store, container, path, cause);
        }

        public static BlobException forGet(Store store, String container, String path, Throwable cause) {
            return forRequest("GET", store, container, path, cause);
        }

        public static BlobException forDelete(Store store, String container, String path, Throwable cause) {
            return forRequest("DELETE", store, container, path, cause);
        }

        private static BlobException forRequest(String method, Store store, String container, String path, Throwable cause) {
            StringBuilder msg = new StringBuilder();
            msg.append("Failed to ")
                .append(method)
                .append(" ")
                .append(store.name)
                .append(":")
                .append(container)
                .append(":")
                .append(path);
            if (cause != null) {
                msg.append(": ")
                    .append(cause.getMessage());
            }
            return new BlobException(msg.toString(), cause);
        }

        private BlobException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
