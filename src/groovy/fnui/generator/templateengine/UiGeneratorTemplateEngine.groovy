package fnui.generator.templateengine

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginInfo
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.springframework.core.io.AbstractResource
import org.springframework.core.io.FileSystemResource

/**
 * The UiGeneratorTemplateEngine manages the available templates for the generation process.
 */
class UiGeneratorTemplateEngine implements GrailsApplicationAware {
    GrailsApplication grailsApplication
    String templateBaseDir = "."

    private final UiGeneratorTemplateCompiler compiler
    private final Map<String,UiGeneratorTemplate> templates = [:]

    UiGeneratorTemplateEngine() {
        this(UiGeneratorTemplateEngine.classLoader)
    }

    UiGeneratorTemplateEngine(ClassLoader parentLoader) {
        compiler = new UiGeneratorTemplateCompiler(parentLoader)
    }

    /**
     * Retrieves the names template (and caches it).
     *
     * The template discovery performs the following steps:
     *  1. Checks applications '/src/templates' for the named template.
     *  2. If not found check the path for all plugins defined in the 'fnui.ui.template.provider'-config list.
     *  3. If not found check the explicitly defined fromPlugin
     *  4. If not found return null reference.
     *
     * @param name
     * @param fromPlugin
     * @return
     */
    UiGeneratorTemplate getTemplate(String name, String fromPlugin = 'fnui-generator') {
        def template = templates[name]

        if (!template) {
            template = createTemplate(name, getTemplateText(name, fromPlugin))
            templates[name] = template
        }

        return template
    }

    private UiGeneratorTemplate createTemplate(String name, String templateText) throws CompilationFailedException, ClassNotFoundException, IOException {
        if (!templateText) {
            return null
        }

        def classCode = new UiGeneratorTemplateParser(new StringReader(templateText)).parse(name)
        def templateClass = compiler.compileTemplateClass(name, classCode)
        new UiGeneratorTemplate(name, templateClass)
    }

    private String getTemplateText(String templateName, String fromPlugin) {
        String name = "src/templates/${templateName}"

        // Check the application for this template
        AbstractResource templateFile = new FileSystemResource(new File(templateBaseDir, name).absoluteFile)

        // Check the UI provider plugin for this template
        if (!templateFile.exists()) {
            List<String> uiTemplateProviders = grailsApplication.config.fnui.ui.template.provider

            for (String pluginName: uiTemplateProviders.reverse()) {
                templateFile = new FileSystemResource(new File(getPluginDir(pluginName), name).absoluteFile)

                if (templateFile.exists()) {
                    break
                }
            }
        }

        // Check the provided plugin for the template
        if (!templateFile.exists() && fromPlugin) {
            templateFile = new FileSystemResource(new File(getPluginDir(fromPlugin), name).absoluteFile)
        }

        // No template with this name is available
        if (!templateFile.exists()) {
            return null
        }

        return IOGroovyMethods.getText(templateFile.inputStream)
    }

    private File getPluginDir(String plugin) {
        GrailsPluginInfo info = GrailsPluginUtils.pluginBuildSettings.getPluginInfoForName(plugin)
        return info.descriptor.file.parentFile
    }
}
