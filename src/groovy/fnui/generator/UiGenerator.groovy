package fnui.generator

import fnui.generator.templateengine.UiGeneratorTemplateProcessor
import fnui.generator.templateengine.extension.ExtensionPointTemplate
import fnui.generator.templateengine.extension.ExtensionWrapperTemplate
import fnui.generator.templateengine.extension.TemplateFunction
import fnui.model.AbstractUserInterfaceModel
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware

/**
 * The UiGenerator handles the generation process of an user interface for the provided
 * AbstractUserInterfaceModel.
 *
 * It provides functionalities to register extension points and wrapper for the generation process.
 */
class UiGenerator implements GrailsApplicationAware {
    GrailsApplication grailsApplication

    UiGeneratorTemplateProcessor processor
    Map<String,TemplateFunction> templateFunctions = [:]
    Map<String,List<ExtensionPointTemplate>> extensionPointTemplatesMap = [:].withDefault {[]}
    Map<String,List<ExtensionWrapperTemplate>> extensionWrapperTemplatesMap = [:].withDefault {[]}

    /**
     * Generates the UI files for the provided AbstractUserInterfaceModel. The path-parameter allows
     * to specific the base-location of the generation results.
     *
     * PRE-REQUIREMENT: grailsApplication must be set
     *
     * @param model defining the to-be-generated UI
     * @param path (optional) generation location defaulting to app-root
     */
    void generateUi(AbstractUserInterfaceModel model, File path = '.' as File) {
        assert grailsApplication && processor
        processor.generate(model.auiGroups, path)
    }

    /**
     * Adds a function to the template engine of the generator.
     *
     * @param templateFunction
     */
    void addTemplateFunction(TemplateFunction templateFunction) {
        templateFunction.processor = processor
        templateFunctions[templateFunction.functionName] = templateFunction
    }

    /**
     * Adds a template for an extension point to the generator.
     *
     * @param extensionName defining the extension point
     * @param template
     */
    void addExtensionPointTemplate(String extensionName, String template) {
        def extensionPointTemplate = new ExtensionPointTemplate(extensionName, template)
        extensionPointTemplate.processor = processor
        extensionPointTemplatesMap[extensionPointTemplate.extensionName] << extensionPointTemplate
    }

    /**
     * Adds a template for an extension wrapper to the generator.
     *
     * @param extensionName defining the extension wrapper
     * @param template
     */
    void addExtensionWrapperTemplate(String extensionName, String template) {
        def extensionWrapperTemplate = new ExtensionWrapperTemplate(extensionName, template)
        extensionWrapperTemplate.processor = processor
        extensionWrapperTemplatesMap[extensionWrapperTemplate.extensionName] << extensionWrapperTemplate
    }

    void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
        processor = new UiGeneratorTemplateProcessor(grailsApplication, extensionPointTemplatesMap, extensionWrapperTemplatesMap, templateFunctions)
    }
}
