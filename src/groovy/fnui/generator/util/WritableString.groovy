package fnui.generator.util

/**
 * Simple wrapper for Strings to Writable
 */
class WritableString implements Writable {
    final String string

    WritableString(String string) {
        this.string = string
    }

    @Override
    Writer writeTo(Writer out) throws IOException {
        out.write(string)
        return out
    }

    Boolean asBoolean() {
        string.asBoolean()
    }
}
