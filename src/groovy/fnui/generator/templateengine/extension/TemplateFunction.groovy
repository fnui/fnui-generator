package fnui.generator.templateengine.extension

import fnui.UiGenerationException
import fnui.generator.templateengine.UiGeneratorTemplateContext
import fnui.generator.templateengine.UiGeneratorTemplateProcessor
import fnui.generator.util.EmptyWritable
import fnui.model.AuiLink
import groovy.util.logging.Log4j

/**
 * A TemplateFunctions is a function which can be used in the templates if registered
 * for the specific generator.
 */
@Log4j
abstract class TemplateFunction {
    UiGeneratorTemplateProcessor processor

    final String functionName

    TemplateFunction(String functionName) {
        this.functionName = functionName
    }

    /**
     * This method will be called by the template engine if the function is invoked
     * in a template.
     *
     * @param context of the callee
     * @param arguments list of method arguments
     * @return
     */
    abstract Writable executeFunction(UiGeneratorTemplateContext context, List arguments)

    /**
     * Uses a template file to create the Writable.
     *
     * @param templateName
     * @param context
     * @param variables
     * @return
     */
    protected Writable translateTemplate(String templateName, UiGeneratorTemplateContext context, Map variables) {
        def template = processor.getTemplate(templateName, true)

        if (!template) {
            log.error "TemplateFunction $functionName needs the template $templateName but it is missing"
            return EmptyWritable.INSTANCE
        }

        return template.translate(context.generateSubContext(variables))
    }

    protected validateParameters(List arguments, List<Class> expectedParameters) {
        if (arguments.size() != expectedParameters.size()) {
            throw new UiGenerationException("TemplateFunction '${functionName}' expects exactly ${expectedParameters.size()} parameters: ${expectedParameters.collect { it.simpleName }}")
        }

        for (int i = 0; i < arguments.size(); i++) {
            if (!expectedParameters[i].isAssignableFrom(arguments[i].getClass())) {
                throw new UiGenerationException("TemplateFunction '${functionName}' expects: ${expectedParameters.collect { it.simpleName }} but got ${arguments.collect { it.getClass().simpleName }}")
            }
        }
    }
}
