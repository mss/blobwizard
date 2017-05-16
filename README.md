# Blobwizard

This is a proof of concept for using jclouds BlobStore with Dropwizard.
Its actual purpose is to check out the jclouds API and have a simple
daemon to play around with.


## Starting

To build and start try this:

    mvn clean package
    java -jar target/blobwizard.jar server src/test/config/target.yml
    curl -v http://127.0.0.1:8080/blob/target/classes/banner.txt

