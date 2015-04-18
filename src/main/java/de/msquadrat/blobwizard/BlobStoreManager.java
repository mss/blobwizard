package de.msquadrat.blobwizard;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;

public class BlobStoreManager implements Managed {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreManager.class);
    
    final Map<String, Store> stores = new HashMap<>();
    
    
    public BlobStoreManager(ServerConfiguration config) {
        for (Map.Entry<String, ServerConfiguration.Store> store : config.getStores().entrySet()) {
            stores.put(store.getKey(), new Store(store.getValue()));
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
            store.start();
        }
    }
    
    public Store get(String name) {
        return stores.get(name);
    }

    
    public class Store implements Managed {
        public Store(ServerConfiguration.Store config) {
            // TODO
        }
        
        @Override
        public void start() throws Exception {
        }

        @Override
        public void stop() throws Exception {
        }
        
        @Deprecated
        private Object notImplemented(String method, String path) {
            LOGGER.info("{} <{}>", method, path);
            throw new NotImplementedException(method);
        }
        
        public void put(String path, Object data) {
            notImplemented("PUT", path);
        }
        
        public Object get(String path) {
            return notImplemented("GET", path);
        }
        
        public void delete(String path) {
            notImplemented("DELETE", path);
        }
    }
}
