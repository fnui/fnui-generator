package fnui.generator.templateengine

import fnui.UiGenerationException
import fnui.generator.templateengine.extension.ExtensionPointTemplate
import fnui.generator.templateengine.extension.ExtensionWrapperTemplate
import fnui.generator.templateengine.extension.TemplateFunction
import fnui.generator.templateengine.result.UiGeneratorController
import fnui.generator.templateengine.result.UiGeneratorResultFile
import fnui.model.AuiNode
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * The UiGeneratorTemplateProcessor handles the generation process.
 */
@Log4j
class UiGeneratorTemplateProcessor {
    File basePath
    UiGeneratorTemplateEngine templateEngine

    Map<String,TemplateFunction> templateFunctions
    Map<String,List<ExtensionPointTemplate>> extensionPointTemplatesMap
    Map<String,List<ExtensionWrapperTemplate>> extensionWrapperTemplatesMap

    UiGeneratorTemplateProcessor(GrailsApplication grailsApplication, Map<String,List<ExtensionPointTemplate>> extensionPointTemplates, Map<String,List<ExtensionWrapperTemplate>> extensionWrapperTemplates, Map<String,TemplateFunction> templateFunctions) {
        templateEngine = new UiGeneratorTemplateEngine()
        templateEngine.grailsApplication = grailsApplication
        this.extensionPointTemplatesMap = extensionPointTemplates
        this.extensionWrapperTemplatesMap = extensionWrapperTemplates
        this.templateFunctions = templateFunctions
    }

    /**
     * Generates the specified artefacts.
     *
     * @param controllers
     *     = auiGroups of the AbstractUserInterfaceModel
     * @param basePath
     *     location to generate the generated artefacts
     */
    void generate(Map<String,AuiNode> controllers, File basePath) {
        assert basePath
        this.basePath = basePath
        basePath.mkdirs()

        controllers.each { name, node ->
            def controller = new UiGeneratorController(this, node)
            controller.translate()
            writeFile(controller)

            controller.views.each { view ->
                view.translate()
                writeFile(view)
            }
        }
    }

    private void writeFile(UiGeneratorResultFile file) {
        def cdDir = new File(basePath, file.relativePath)
        cdDir.mkdirs()
        def cdFile = new File(cdDir, file.fileName)

        def writer = new FileWriter(cdFile)
        try {
            file.content.writeTo(writer)
        } catch (e) {
            def cause = e
            while (cause.cause) {
                if (!(cause.cause instanceof UiGenerationException)) {
                    break
                }

                cause = cause.cause
            }
            throw new UiGenerationException("Generation of '${file.name}' failed with root cause: ${cause.message}", e)
        }
        writer.close()
        log.info "Wrote file: $cdFile"
    }

    /**
     * Retrieves the defined template.
     *
     * @param templateName
     * @param ignoreMissing
     *     if true, no exception is raised if an template could not be found
     * @return the template or null if not found and ignoreMissing
     * @throws UiGenerationException if defined template could not be found
     */
    UiGeneratorTemplate getTemplate(String templateName, Boolean ignoreMissing) {
        def template = templateEngine.getTemplate(templateName)

        if (!template && !ignoreMissing) {
            throw new UiGenerationException("Could not find template with name: $templateName")
        }

        return template
    }

    /**
     * Retrieve all extension point template of the defined extensionPoint name.
     *
     * @param extensionName
     * @return
     */
    List<ExtensionPointTemplate> getExtensionPointTemplates(String extensionName) {
        extensionPointTemplatesMap[extensionName].each { it.processor = this }
    }

    /**
     * Retrieve all extension wrapper templates of the defined extensionWrapper name.
     *
     * @param extensionName
     * @return
     */
    List<ExtensionWrapperTemplate> getExtensionWrapperTemplates(String extensionName) {
        extensionWrapperTemplatesMap[extensionName].each { it.processor = this }
    }

    TemplateFunction getTemplateFunction(String functionName) {
        templateFunctions[functionName]
    }
}
