package fnui.generator.util

import spock.lang.Specification

class WritableCollectionSpec extends Specification {
    def "WriteTo: Write nothing of no writable was added"() {
        def writer = new StringWriter()
        def writablesCollection = new WritableCollection()

        when:
        writablesCollection.writeTo(writer)

        then:
        writer.toString() == ''
    }

    def "WriteTo: Write all collected writables in adding order"() {
        def writer = new StringWriter()
        def writablesCollection = new WritableCollection()
        def gString1 = "A ${1}"
        def gString2 = "B ${false}"

        when:
        writablesCollection << gString2
        writablesCollection << gString1
        writablesCollection.add(gString2)
        writablesCollection.writeTo(writer)

        then:
        writer.toString() == 'B falseA 1B false'
    }
}