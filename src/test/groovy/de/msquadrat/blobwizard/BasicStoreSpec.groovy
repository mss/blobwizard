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
        containerName = container.toFile().name

        ServerConfiguration.Store config = Stub()
        config.getApi() >> "filesystem"
        config.getOptions() >> ["filesystem.basedir": target.toString()]
        store = new BlobStoreManager.Store("target", config)
        store.start()
    }

    def cleanup() {
        if (!container.deleteDir()) {
            throw new IOException("Failed to remove test container " + containerName);
        }
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
