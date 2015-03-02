package fnui.generator.templateengine.result

import fnui.generator.templateengine.UiGeneratorTemplateParser
import fnui.generator.templateengine.UiGeneratorTemplateProcessor
import fnui.model.AuiNode

/**
 * Describes a view controller.
 */
class UiGeneratorController implements UiGeneratorResultFile {
    /**
     * the root node of the controller
     */
    AuiNode node

    UiGeneratorControllerContext context
    Writable content
    List<UiGeneratorView> views = []

    UiGeneratorTemplateProcessor processor

    UiGeneratorController(UiGeneratorTemplateProcessor templateProcessor, AuiNode controllerNode) {
        assert controllerNode.type == 'controller'
        processor = templateProcessor
        node = controllerNode
    }

    /**
     * Prepare the content for generation process of the controller artefact.
     */
    void translate() {
        context = new UiGeneratorControllerContext(this,
                [node       : node,
                 name       : node.name,
                 simpleName : node.data['simpleName'],
                 packageName: node.data['servicePackage'] ? "${node.data['servicePackage']}.generated" : 'generated',
                 (UiGeneratorTemplateParser.INDENTION_VAR):''])

        String controllerKind = node.data['kind'] ?: 'Controller'
        def template = processor.getTemplate("controllers/${controllerKind}.groovy", false)
        content = template.translate(context)
    }

    /**
     * Adds an new view for this controller.
     *
     * @param viewNode is the view describing AuiNode
     */
    void addView(AuiNode viewNode) {
        views << new UiGeneratorView(processor, simpleName, viewNode)
    }

    @Override
    String getName() {
        assert context
        context['name']
    }

    String getSimpleName() {
        assert context
        context['simpleName']
    }

    String getPackageName() {
        assert context
        context['packageName']
    }

    @Override
    String getRelativePath() {
        assert packageName
        "grails-app/controllers/${packageName.replaceAll(/\./, '/')}"
    }

    @Override
    String getFileName() {
        assert name
        "${name}.groovy"
    }
}
