package de.msquadrat.blobwizard;

import java.util.Collections;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class ServerConfiguration extends Configuration {
    
    @JsonProperty
    @NotEmpty
    private Map<String, Store> stores;
    
    public Map<String, Store> getStores() {
        if (stores == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(stores);
    }
    
    public static class Store {
        @JsonProperty
        @NotEmpty
        private String api;
        
        @JsonProperty
        private String identity;
        
        @JsonProperty
        private String credential;
        
        @JsonProperty
        private Map<String, String> options;
        
        
        public String getApi() {
            return api;
        }
        
        public String getIdentity() {
            return identity;
        }
        
        public String getCredential() {
            return credential;
        }
        
        public Map<String, String> getOptions() {
            if (options == null) {
                return Collections.emptyMap();
            }
            return Collections.unmodifiableMap(options);
        }
    }
}
