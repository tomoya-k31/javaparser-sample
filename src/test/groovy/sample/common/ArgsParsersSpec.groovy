package sample.common

import spock.lang.Specification

class ArgsParsersSpec extends Specification {

    def "test parse"() {
        when:
        def result = ArgsParsers.parseJarText(new File("src/test/resources/resolved-jars.txt"))

        then:
        result.size() == 4
        result[0].each {
            assert it.library == 'Mikasa.Server:webapp-core:'
            assert it.path.path.endsWith('mikasa.jar')
            assert it.otherProject
        }

        result[1].each {
            assert it.library == ':springmobile_mikasa-0.0.1:'
            assert it.path.path.endsWith('springmobile_mikasa-0.0.1.jar')
            assert !it.otherProject
        }

        result[2].each {
            assert it.library == 'org.springframework.boot:spring-boot-starter-web-services:2.1.4.RELEASE'
            assert it.path.path.endsWith('spring-boot-starter-web-services-2.1.4.RELEASE.jar')
            assert !it.otherProject
        }

        result[3].each {
            assert it.library == 'dummy'
            assert it.path.path.endsWith('dummy.jar')
            assert !it.otherProject
        }
    }
}
