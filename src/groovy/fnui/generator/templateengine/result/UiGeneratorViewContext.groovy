package fnui.generator.templateengine.result

import fnui.generator.templateengine.UiGeneratorTemplate
import fnui.generator.templateengine.UiGeneratorTemplateContext
import fnui.generator.templateengine.extension.ExtensionWrapperTemplate
import groovy.util.logging.Log4j

/**
 * The UiGeneratorViewContext describes the specific context used for the processing of a view artefact template.
 */
@Log4j
class UiGeneratorViewContext extends UiGeneratorTemplateContext {
    private UiGeneratorView uiGeneratorView


    UiGeneratorViewContext(UiGeneratorView uiGeneratorView, Map variables) {
        super(uiGeneratorView.processor, variables)
        this.uiGeneratorView = uiGeneratorView
    }

    UiGeneratorViewContext(UiGeneratorViewContext parentContext, Map variables) {
        super(parentContext, variables)
        this.uiGeneratorView = parentContext.uiGeneratorView
    }

    UiGeneratorViewContext(UiGeneratorViewContext wrappedContext, UiGeneratorTemplate wrappedTemplate, List<ExtensionWrapperTemplate> wrapperTemplates) {
        super(wrappedContext, wrappedTemplate, wrapperTemplates)
        this.uiGeneratorView = wrappedContext.uiGeneratorView
    }

    @Override
    UiGeneratorTemplateContext generateWrapperContext(UiGeneratorTemplateContext wrappedContext, UiGeneratorTemplate wrappedTemplate, List<ExtensionWrapperTemplate> wrapperTemplates) {
        new UiGeneratorViewContext((UiGeneratorViewContext)wrappedContext, wrappedTemplate, wrapperTemplates)
    }

    @Override
    UiGeneratorTemplateContext generateSubContext(Map variables) {
        new UiGeneratorViewContext(this, variables)
    }

    @Override
    String getContextRelatedTemplateName(String baseTemplateName) {
        "views/${baseTemplateName}.gsp"
    }
}
