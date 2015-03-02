package fnui.generator.util

/**
 * The EmptyWritable is an Writable which writes never anything.
 *
 * GroovyTruth: always false.
 */
class EmptyWritable implements Writable {
    /**
     * A static instance of EmptyWritable to reuse.
     */
    final static Writable INSTANCE = new EmptyWritable()

    @Override
    Writer writeTo(Writer out) throws IOException {
        return out
    }

    Boolean asBoolean() {
        false // GroovyTruth
    }
}
