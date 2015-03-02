package fnui.generator.templateengine.result

import fnui.generator.templateengine.UiGeneratorTemplateParser
import fnui.generator.templateengine.UiGeneratorTemplateProcessor
import fnui.model.AuiNode

/**
 * Describes a view file.
 */
class UiGeneratorView implements UiGeneratorResultFile {
    /**
     * the root node of the view
     */
    AuiNode node

    String name
    String simpleControllerName

    UiGeneratorViewContext context
    Writable content

    UiGeneratorTemplateProcessor processor

    UiGeneratorView(UiGeneratorTemplateProcessor templateProcessor, String simpleControllerName, AuiNode viewNode) {
        assert viewNode.type == 'view'
        this.processor = templateProcessor
        this.simpleControllerName = simpleControllerName
        this.node = viewNode
    }

    /**
     * Prepare the content for generation process of the view artefact.
     */
    void translate() {
        name = node.name
        def viewKind = node.data['kind'] ?: 'default'
        def template = processor.getTemplate("views/layout/${viewKind}.gsp", false)

        context = new UiGeneratorViewContext(this, [
                node:node,
                name: name,
                simpleControllerName: simpleControllerName,
                (UiGeneratorTemplateParser.INDENTION_VAR):''])
        content = template.translate(context)
    }

    @Override
    String getRelativePath() {
        assert simpleControllerName
        "grails-app/views/generated/${simpleControllerName}"
    }

    @Override
    String getFileName() {
        assert name
        "${name}.gsp"
    }
}
