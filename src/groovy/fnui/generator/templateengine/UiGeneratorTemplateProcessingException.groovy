package fnui.generator.templateengine

import fnui.UiGenerationException

/**
 * The UiGeneratorTemplateProcessingException is use for exceptions cause while processing the UI generation.
 */
class UiGeneratorTemplateProcessingException extends UiGenerationException {
    String templateName

    UiGeneratorTemplateContext context

    UiGeneratorTemplateProcessingException(String templateName, UiGeneratorTemplateContext context,  Throwable cause) {
        super("Error in '${templateName}': ${cause.message}", cause)
        this.templateName
        this.context = context
    }
}
