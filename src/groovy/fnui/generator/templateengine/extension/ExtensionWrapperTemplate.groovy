package fnui.generator.templateengine.extension

import fnui.generator.templateengine.UiGeneratorTemplateContext
import fnui.generator.templateengine.UiGeneratorTemplateProcessor
import groovy.util.logging.Log4j

/**
 * An ExtensionWrapperTemplate allows to wrap to a template at an extension point.
 */
@Log4j
class ExtensionWrapperTemplate {
    UiGeneratorTemplateProcessor processor

    String extensionName
    String templateName

    ExtensionWrapperTemplate(String extensionName, String templateName) {
        this.extensionName = extensionName
        this.templateName = templateName
    }

    /**
     * Retrieves the defined template and get writable result of the template for the provided context.
     *
     * Returns the wrapped template's writable if template can not be found.
     *
     * @param context the extension point is called in
     * @return a writable containing the extension result (or an EmptyWriter if non)
     */
    Writable translateExtension(UiGeneratorTemplateContext context) {
        def template = processor.getTemplate(templateName, true)

        if (!template) {
            log.trace "Extension wrapper $extensionName has a registered template with name $templateName which does not exists."
            return context.translateWrapped()
        }

        return template.translate(context)
    }

    String toString() {
        templateName
    }
}
