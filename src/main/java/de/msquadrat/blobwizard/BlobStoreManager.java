package de.msquadrat.blobwizard;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.NotImplementedException;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

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
        private final BlobStoreContext context;

        public Store(ServerConfiguration.Store config) {
            Properties overrides = new Properties();
            for (Map.Entry<String, String> option : config.getOptions().entrySet()) {
                String name = "jclouds." + option.getKey();
                overrides.put(name, option.getValue());
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
        }

        @Override
        public void stop() throws Exception {
            context.close();
        }
        
        @Deprecated
        private Optional<Object> notImplemented(String method, String container, String path) {
            LOGGER.info("{} {}:{}", method, container, path);
            throw new NotImplementedException(method);
        }
        
        public void put(String container, String path, Object data) {
            notImplemented("PUT", container, path);
        }
        
        public Optional<Object> get(String container, String path) {
            return notImplemented("GET", container, path);
        }
        
        public void delete(String container, String path) {
            notImplemented("DELETE", container, path);
        }
    }
}
