package fnui.generator.templateengine

import fnui.UiGenerationException
import fnui.generator.templateengine.extension.ExtensionWrapperTemplate
import fnui.generator.util.EmptyWritable
import fnui.generator.util.WritableCollection
import fnui.model.AuiNode
import groovy.util.logging.Log4j

/**
 * The UiGeneratorTemplateContext is used as model for the template processing. It provides also the
 * methods to embed other template and defining extension and wrapper points in templates.
 *
 * The contexts for embedded templates creates an hierarchy describing the generation process.
 */
@Log4j
abstract class UiGeneratorTemplateContext {
    UiGeneratorTemplateProcessor templateProcessor
    private UiGeneratorTemplateContext parentContext
    private Map<String, Object> variables

    private UiGeneratorTemplate wrappedTemplate
    private List<ExtensionWrapperTemplate> wrapperTemplates
    private UiGeneratorTemplateContext wrappedContext

    UiGeneratorTemplateContext(UiGeneratorTemplateProcessor templateProcessor, Map variables = null) {
        this.templateProcessor = templateProcessor
        this.variables = variables
    }

    UiGeneratorTemplateContext(UiGeneratorTemplateContext parentContext, Map variables = null) {
        assert parentContext && parentContext.templateProcessor
        this.parentContext = parentContext
        this.templateProcessor = parentContext.templateProcessor
        this.variables = variables
    }

    UiGeneratorTemplateContext(UiGeneratorTemplateContext wrappedContext, UiGeneratorTemplate wrappedTemplate, List<ExtensionWrapperTemplate> wrapperTemplates) {
        assert wrappedContext && wrappedContext.templateProcessor
        this.parentContext = this.wrappedContext = wrappedContext
        this.templateProcessor = wrappedContext.templateProcessor
        this.wrappedTemplate = wrappedTemplate
        this.wrapperTemplates = wrapperTemplates
    }

    /**
     * Retrieve a variable from the context hierarchy. First checks for the variable in the current
     * context. If it could not be found tries the parent context. If the variable is not defined
     * it throws a MissingPropertyException.
     *
     * @param name
     * @return
     * @throws MissingPropertyException
     */
    Object getVariable(String name) {
        Object result = variables?.get(name)

        if (result == null && !variables?.containsKey(name)) {
            if (!parentContext) {
                throw new MissingPropertyException(name, UiGeneratorTemplateContext)
            }

            result = parentContext.getVariable(name)
            if (result == null && !parentContext.hasVariable(name)) {
                throw new MissingPropertyException(name, UiGeneratorTemplateContext)
            }
        }

        return result
    }

    /**
     * Sets a variable for the local context.
     *
     * @param name
     * @param value
     */
    void setVariable(String name, Object value) {
        if (variables == null) {
            variables = [:]
        }

        variables.put(name, value)
    }

    /**
     * Checks the ONLY the local context if the variable is defined.
     *
     * @param name
     * @return
     */
    boolean hasVariable(String name) {
        variables != null && variables.containsKey(name)
    }

    /**
     * Retrieve the variable from the local context only.
     *
     * @param name
     * @return the associated value value
     * @throws MissingPropertyException
     */
    Object getLocalVariable(String name) {
        def value = variables.get(name)
        if (!value && !variables.containsKey(name)) {
            throw new MissingPropertyException(name, UiGeneratorTemplateContext)
        }

        return value
    }

    /**
     * Retrieve the variable from the parentContexts if any.
     *
     * @param name
     * @return found value or null if no parentContext
     * @throws MissingPropertyException
     */
    Object getParentVariable(String name) {
        parentContext ? parentContext.getVariable(name) : null
    }

    /**
     * Checks if the parentContexts contains the variable.
     *
     * @param name
     * @return
     */
    boolean hasParentVariable(String name) {
        parentContext ? parentContext.hasVariable(name) : false
    }

    /**
     * Get the variable from the root context.
     *
     * @param name
     * @return
     */
    Object getRootVariable(String name) {
        rootContext.getVariable(name)
    }

    /**
     * Sets the variable in the root context.
     *
     * @param name
     * @param value
     */
    void setRootVariable(String name, Object value) {
        rootContext.setVariable(name, value)
    }

    /**
     * Checks for the variable in the root context.
     *
     * @param name
     * @return
     */
    boolean hasRootVariable(String name) {
        rootContext.hasVariable(name)
    }

    /**
     * @return the root context of the current hierarchy.
     */
    UiGeneratorTemplateContext getRootContext() {
        def context = this

        while (context.parentContext) { context = this }

        return context
    }

    /**
     * @return the local variables map
     */
    Map getVariables() {
        if (variables == null) {
            variables = []
        }

        return variables
    }

    def propertyMissing(String name) {
        getVariable(name)
    }

    def propertyMissing(String name, def arg) {
        setVariable(name, arg)
    }

    def methodMissing(String name, args) {
        executeFunction(name, args as List)
    }

    Writable executeFunction(String name, List args) {
        def function = templateProcessor.getTemplateFunction(name)

        if (!function) {
            throw new UiGenerationException("A function with name ${name} was called but is not available in the template engine.")
        }

        return function.executeFunction(this, args)
    }

    /**
     * Embeds the defined template into the current template for the defined node.
     * If ignorable and template is not available an empty string replaces the template.
     * The wrapperName defines an extension wrapper point which is used to wrap the template
     * into the registered extension wrapper.
     *
     * @param templateName
     * @param node
     * @param ignorable
     *      if true, no exception is raised of the template is not available
     * @param wrapperName
     * @return the Writable result
     */
    Writable translate(String templateName, AuiNode node, Boolean ignorable = false, String wrapperName = null) {
        if (!node) {
            return EmptyWritable.INSTANCE
        }

        def template = templateProcessor.getTemplate(getContextRelatedTemplateName(templateName), ignorable)

        if (!template) {
            return EmptyWritable.INSTANCE
        }

        def subContext = generateSubContext([node:node])
        assert subContext != null

        if (wrapperName) {
            log.trace "Looking for extension wrapper for extension point $wrapperName."
            def wrappers = templateProcessor.getExtensionWrapperTemplates(wrapperName)
            if (wrappers) {
                log.trace "For extension point $wrapperName the following wrappers are registered: $wrappers"
                def firstWrapper = wrappers.first()
                def wrappingContext = generateWrapperContext(subContext, template, wrappers.tail())
                return firstWrapper.translateExtension(wrappingContext)
            }
        }

        template.translate(subContext)
    }

    /**
     * Defines the defined extension point and retrieve the content of registered
     *
     * @param extensionPoint name for the extensionPoint
     * @param node as context for the extension point template
     * @return the Writable result
     */
    Writable extend(String extensionPoint, AuiNode node) {
        if (!node) {
            return EmptyWritable.INSTANCE
        }

        def templates = templateProcessor.getExtensionPointTemplates(extensionPoint)

        if (!templates) {
            return EmptyWritable.INSTANCE
        }

        def writables = new WritableCollection()

        for (def t:templates) {
            def context = generateSubContext([node:node])
            writables << t.translateExtension(context)
        }

        return writables
    }

    /**
     * @return true if this is a wrapper context
     */
    boolean isWrapperContext() {
        wrappedContext != null
    }

    /**
     * @return the writable of the wrapped template
     */
    Writable translateWrapped() {
        if (!isWrapperContext()) {
            throw new UiGenerationException('Call to translateWrapped() outside of a wrapper context.')
        }

        if (wrapperTemplates) {
            def wrapper = wrapperTemplates.first()
            def wrapperContext = generateWrapperContext(wrappedContext, wrappedTemplate, wrapperTemplates.tail())
            return wrapper.translateExtension(wrapperContext)
        }

        return wrappedTemplate.translate(wrappedContext)
    }

    /**
     * Pushs the indention of the wrappedContext right by four spaces
     */
    void increaseIndentionForWrapped() {
        if (!isWrapperContext()) {
            throw new UiGenerationException('Call to increaseIndentionForWrapped() outside of a wrapper context.')
        }
        setVariable(UiGeneratorTemplateParser.INDENTION_VAR, wrappedContext[UiGeneratorTemplateParser.INDENTION_VAR])
        wrappedContext[UiGeneratorTemplateParser.INDENTION_VAR] = wrappedContext[UiGeneratorTemplateParser.INDENTION_VAR] + '    '
    }

    /**
     * Creates a new context of the same type which is set upped for executing wrapper templates.
     *
     * @param wrappedContext
     * @param wrappedTemplate
     * @param wrapperTemplates
     * @return
     */
    abstract UiGeneratorTemplateContext generateWrapperContext(UiGeneratorTemplateContext wrappedContext, UiGeneratorTemplate wrappedTemplate, List<ExtensionWrapperTemplate> wrapperTemplates)

    /**
     * Creates a new context of the same type which has the current context as parent
     * and the defined local variables.
     *
     * @param variables
     * @return
     */
    abstract UiGeneratorTemplateContext generateSubContext(Map variables)

    /**
     * Returns an template name matching appropriated for the current context type. This allows
     * to use short templates names in the templates and map eg. for controllers the template
     * name to a certain directory:
     *  baseTemplateName = 'list'
     *  returns 'controller/list.groovy'
     *
     * @param baseTemplateName
     * @return
     */
    abstract String getContextRelatedTemplateName(String baseTemplateName)
}
