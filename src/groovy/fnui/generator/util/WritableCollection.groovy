package fnui.generator.util

/**
 * The WritableCollection collects some Writables and writes the collected writables in order
 * of adding.
 */
class WritableCollection implements Writable {
    List<Writable> writables = []

    @Override
    Writer writeTo(Writer out) throws IOException {
        writables.each { w -> w.writeTo(out) }
        return out
    }

    void add(Writable writable) {
        writables << writable
    }

    WritableCollection leftShift(Writable writable) {
        add(writable)
        return this
    }
}
