package fnui.generator.util

import spock.lang.Specification

class EmptyWritableSpec extends Specification {
    def "WriteTo: EmptyWritable writes nothing"() {
        def writer = new StringWriter()

        when:
        EmptyWritable.INSTANCE.writeTo(writer)

        then:
        writer.toString() == ''
    }

    def "AsBoolean: the GroovyTruth of empty writable is false"() {
        expect:
        EmptyWritable.INSTANCE ? false : true
    }
}