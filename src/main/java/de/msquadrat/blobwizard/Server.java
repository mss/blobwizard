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

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
