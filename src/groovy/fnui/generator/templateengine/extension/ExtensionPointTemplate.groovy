package fnui.generator.templateengine.extension

import fnui.generator.templateengine.UiGeneratorTemplateContext
import fnui.generator.templateengine.UiGeneratorTemplateProcessor
import fnui.generator.util.EmptyWritable
import groovy.util.logging.Log4j

/**
 * An ExtensionPointTemplate allows to add code to a template at an extension point.
 */
@Log4j
class ExtensionPointTemplate {
    UiGeneratorTemplateProcessor processor

    String extensionName
    String templateName

    ExtensionPointTemplate(String extensionName, String templateName) {
        this.extensionName = extensionName
        this.templateName = templateName
    }

    /**
     * Retrieves the defined template and get writable result of the template for the provided context.
     *
     * If the template is unavailable an {@link EmptyWritable} is generated.
     *
     * @param context the extension point is called in
     * @return a writable containing the extension result (or an EmptyWriter if non)
     */
    Writable translateExtension(UiGeneratorTemplateContext context) {
        def template = processor.getTemplate(templateName, true)

        if (!template) {
            log.debug "Extension point $extensionName has a registered template with name $templateName which does not exists."
            return EmptyWritable.INSTANCE
        }

        return template.translate(context)
    }

    String toString() {
        templateName
    }
}
