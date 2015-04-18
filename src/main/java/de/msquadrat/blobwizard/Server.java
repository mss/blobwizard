package de.msquadrat.blobwizard;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.msquadrat.blobwizard.resources.BlobResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class Server extends Application<ServerConfiguration> {
    private static final String SERVICE_NAME = "blobwizard";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    
    public static void main(String[] args) throws Exception {
        try {
            new Server().run(args);
        }
        catch (FileNotFoundException e) {
            die("Can't load config file: " + e.getMessage());
        }
        catch (Throwable e) {
            die("Unhandled exception in main thread", e);
        }
        finally {
            LOGGER.debug("Finished main thread");
        }
    }
    
    private static void die(String msg) throws Exception {
        if (msg != null) {
            System.err.println(msg);
        }
        
        System.exit(1);
    }
    
    private static void die(String msg, Throwable cause) throws Exception {
        LOGGER.error(msg, cause);

        // Avoid mix up of the logger and print output
        Thread.sleep(10);

        System.err.print(msg + ": ");
        cause.printStackTrace(System.err);

        die(null);
    }
    
    private Server() {
        super();
        
        Thread.setDefaultUncaughtExceptionHandler(
            new Thread.UncaughtExceptionHandler() {
                @Override public void uncaughtException(Thread t, Throwable e) {
                    Logger logger = LoggerFactory.getLogger(t.getClass());
                    logger.error("Unhandled exception in thread {}", t.getName(), e);
                }
            }
        );
    }
    
    @Override
    public void run(ServerConfiguration config, Environment env) throws Exception {
        final BlobStoreManager storeManager = new BlobStoreManager(config);
        env.lifecycle().manage(storeManager);
        
        env.jersey().register(new BlobResource(storeManager));
    }
    
    @Override
    public String getName() {
        return SERVICE_NAME;
    }

}
