package fnui.generator.templateengine

import groovy.util.logging.Log4j

/**
 * The UiGeneratorTemplateWritable wraps all template writables to provide error handling.
 */
@Log4j
class UiGeneratorTemplateWritable implements Writable {
    String templateName
    UiGeneratorTemplateContext context
    Writable templateClosure

    UiGeneratorTemplateWritable(String templateName, UiGeneratorTemplateContext context, Writable templateClosure) {
        this.templateName = templateName
        this.context = context
        this.templateClosure = templateClosure
    }

    @Override
    Writer writeTo(Writer out) throws IOException {
        try {
            log.trace "Templating $templateName..."
            templateClosure.writeTo(out)
        } catch (e) {
            throw new UiGeneratorTemplateProcessingException(templateName, context, e)
        }

        return out
    }
}
