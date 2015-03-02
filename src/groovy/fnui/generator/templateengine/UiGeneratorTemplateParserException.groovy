package fnui.generator.templateengine

import fnui.UiGenerationException

/**
 * The UiGeneratorTemplateParserException is used for errors while parsing a template file.
 */
class UiGeneratorTemplateParserException extends UiGenerationException {
    String templateName
    String template

    UiGeneratorTemplateParserException(String templateName, String template, Throwable cause) {
        super("Template '${templateName}': ${cause.message} \n${template}", cause)
        this.templateName
    }
}
