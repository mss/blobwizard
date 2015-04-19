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
            store.stop();
        }
    }
    
    public Store get(String name) {
        return stores.get(name);
    }

    
    public class Store implements Managed {
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
            context.close();
            LOGGER.info("Stopped store {}", name);
        }
        
        @Deprecated
        private Optional<Object> notImplemented(String method, String container, String path) {
            LOGGER.info("{}.{} {}:{}", name, method, container, path);
            throw new NotImplementedException(method);
        }
        
        public void put(String container, String path, Object data) {
            notImplemented("PUT", container, path);
        }
        
        public Optional<Object> get(String container, String path) {
            return notImplemented("PUT", container, path);
        }
        
        public void delete(String container, String path) {
            notImplemented("DELETE", container, path);
        }
    }
}
