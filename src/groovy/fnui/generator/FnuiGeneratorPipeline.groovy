package fnui.generator

import fnui.FnuiModelPipeline
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.GrailsPluginManager
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware

@Log4j
class FnuiGeneratorPipeline implements GrailsApplicationAware {
    final static String CONFIGURE_CLOSURE_NAME = 'configureFnuiGenerator'

    GrailsApplication grailsApplication
    UiGenerator uiGenerator

    void generateUi() {
        assert grailsApplication

        def fnuiPipeline = new FnuiModelPipeline()
        fnuiPipeline.grailsApplication = grailsApplication
        fnuiPipeline.initializePipeline()

        def uiModel = fnuiPipeline.generateModel()
        assert uiModel

        uiGenerator = newUiGenerator(grailsApplication)

        configureUiGenerator(uiGenerator)

        uiGenerator.generateUi(uiModel)
    }

    void configureUiGenerator(UiGenerator uiGenerator) {
        assert grailsApplication

        loadConfigurationFromPlugins(uiGenerator)
    }

    private void loadConfigurationFromPlugins(UiGenerator uiGenerator) {
        GrailsPluginManager pluginManager = grailsApplication.mainContext.pluginManager

        pluginManager.allPlugins.each { plugin ->
            if (plugin.supportsCurrentScopeAndEnvironment()) {
                try {
                    def instance = plugin.instance
                    if (!instance.hasProperty(CONFIGURE_CLOSURE_NAME)) {
                        return
                    }

                    Closure c = (Closure) instance.getProperty(CONFIGURE_CLOSURE_NAME)
                    c.setDelegate(this)
                    c.call(uiGenerator)
                } catch (Throwable t) {
                    log.error "Error configuring dynamic methods for plugin ${plugin}: ${t.message}", t
                }
            }
        }
    }

    UiGenerator newUiGenerator(GrailsApplication grailsApplication) {
        assert grailsApplication

        def uiGenerator = new UiGenerator()
        uiGenerator.grailsApplication = grailsApplication

        return uiGenerator
    }
}
