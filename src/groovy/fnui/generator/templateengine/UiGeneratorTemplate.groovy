package fnui.generator.templateengine

/**
 * The UiGeneratorTemplate allows to reuse a parse template. For each usage of the template
 * an UiGeneratorTemplateContext has to be provided.
 */
class UiGeneratorTemplate {
    final String templateName
    final Closure template

    UiGeneratorTemplate(final String templateName, final Class templateClass) {
        this.templateName = templateName
        try {
            final GroovyObject script = (GroovyObject) templateClass.newInstance();

            this.template = (Closure) script.invokeMethod("getTemplate", null);
            // GROOVY-6521: must set strategy to DELEGATE_FIRST, otherwise writing
            // books = 'foo' in a template would store 'books' in the binding of the template script itself ("script")
            // instead of storing it in the delegate, which is a Binding too
            this.template.setResolveStrategy(Closure.DELEGATE_FIRST);
        } catch (InstantiationException e) {
            throw new ClassNotFoundException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ClassNotFoundException(e.getMessage());
        }
    }

    /**
     * Get a Writable containing the result of this template in the provided context.
     *
     * @param context
     * @return
     */
    Writable translate(UiGeneratorTemplateContext context) {
        def templateInstance = ((Closure) this.template.clone()).asWritable()
        templateInstance.setDelegate(context)
        return new UiGeneratorTemplateWritable(templateName, context, (Writable)templateInstance)
    }
}
