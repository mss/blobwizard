package de.msquadrat.blobwizard

import de.msquadrat.blobwizard.ServerConfiguration.*
import de.msquadrat.blobwizard.BlobStoreManager.*

import spock.lang.*
import java.nio.file.*

class BasicStoreSpec extends Specification {
    String containerName
    Path container
    BlobStoreManager.Store store
    def setup() {
        Path target = Paths.get(this.class.protectionDomain.codeSource.location.toURI())
        while (target.toFile().name != "target") {
            target = target.parent
        }
        container = Files.createTempDirectory(target, "test-container-")
        container.toFile().deleteOnExit()
        containerName = container.toFile().name

        ServerConfiguration.Store config = Stub()
        config.getApi() >> "filesystem"
        config.getOptions() >> ["filesystem.basedir": target.toString()]
        store = new BlobStoreManager.Store("target", config)
        store.start()
    }

    def "PUT creates file"() {
        when:
        store.put(containerName, "GET", new ByteArrayInputStream([ 0x66, 0x6f, 0x6f ] as byte[]))

        then:
        Files.exists(container.resolve("GET"))
    }

    def "GET retrieves file"() {
        setup:
        Files.write(Files.createFile(container.resolve("GET")), [ 0x66, 0x6f, 0x6f ] as byte[]);

        when:
        def res = store.get(containerName, "GET")

        then:
        res.isPresent()
    }

    def "DELETE purges file"() {
        setup:
        Files.write(Files.createFile(container.resolve("DELETE")), [ 0x66, 0x6f, 0x6f ] as byte[]);

        when:
        def res = store.delete(containerName, "DELETE")

        then:
        !Files.exists(container.resolve("DELETE"))
    }
}
