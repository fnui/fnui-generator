package fnui.generator.templateengine.result

import fnui.generator.templateengine.UiGeneratorTemplate
import fnui.generator.templateengine.UiGeneratorTemplateContext
import fnui.generator.templateengine.extension.ExtensionWrapperTemplate
import fnui.model.AuiNode
import groovy.util.logging.Log4j

/**
 * The UiGeneratorControllerContext describes the specific context used for the processing of a controller artefact template.
 */
@Log4j
class UiGeneratorControllerContext extends UiGeneratorTemplateContext {
    UiGeneratorController uiGeneratorController

    UiGeneratorControllerContext(UiGeneratorController uiGeneratorController, Map variables) {
        super(uiGeneratorController.processor, variables)
        this.uiGeneratorController = uiGeneratorController
    }

    UiGeneratorControllerContext(UiGeneratorControllerContext parentContext, Map variables) {
        super(parentContext, variables)
        this.uiGeneratorController = parentContext.uiGeneratorController
    }

    UiGeneratorControllerContext(UiGeneratorControllerContext wrappedContext, UiGeneratorTemplate wrappedTemplate, List<ExtensionWrapperTemplate> wrapperTemplates) {
        super(wrappedContext, wrappedTemplate, wrapperTemplates)
        this.uiGeneratorController = wrappedContext.uiGeneratorController
    }

    /**
     * Adds a view to the generation process
     *
     * @param viewNode the view describing AuiNode
     */
    void addView(AuiNode viewNode) {
        uiGeneratorController.addView(viewNode)
    }

    @Override
    UiGeneratorTemplateContext generateWrapperContext(UiGeneratorTemplateContext wrappedContext, UiGeneratorTemplate wrappedTemplate, List<ExtensionWrapperTemplate> wrapperTemplates) {
        new UiGeneratorControllerContext((UiGeneratorControllerContext)wrappedContext, wrappedTemplate, wrapperTemplates)
    }

    @Override
    UiGeneratorTemplateContext generateSubContext(Map variables) {
        new UiGeneratorControllerContext(this, variables)
    }

    @Override
    String getContextRelatedTemplateName(String baseTemplateName) {
        "controllers/${baseTemplateName}.groovy"
    }
}
