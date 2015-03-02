package fnui.generator.templateengine

import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.GrailsStringUtils

import java.security.AccessController
import java.security.PrivilegedAction
import java.util.concurrent.atomic.AtomicInteger

/**
 * Compiles a parsed template and generated the UiGeneratorTemplate instance.
 */
@Log4j
class UiGeneratorTemplateCompiler {
    private final static AtomicInteger counter = new AtomicInteger()

    private final GroovyClassLoader loader

    /**
     * Prepare an UiGeneratorTemplateCompiler
     *
     * @param parentLoader
     *    the ClassLoader which is used for the compilation process, which should be able to load
     *    all classes used in the templates.
     */
    UiGeneratorTemplateCompiler(ClassLoader parentLoader) {
        if (parentLoader instanceof GroovyClassLoader) {
            loader = parentLoader
        } else {
            def action = [run: { new GroovyClassLoader(parentLoader) }] as PrivilegedAction
            loader = (GroovyClassLoader) AccessController.doPrivileged(action)

        }
    }

    /**
     * Compile the provided template with the given name.
     *
     * @param name of the template
     * @param template parsed content of the template
     * @return the generated class used for the template
     */
    Class compileTemplateClass(String name, String template) {
        final Class templateClass
        try {
            log.debug template
            templateClass = loader.parseClass(new GroovyCodeSource(template, "UiGeneratorTemplateScript${counter.incrementAndGet()}_${simplifyName(name)}.groovy", "x"));
        } catch (e) {
            throw new UiGeneratorTemplateParserException(name, template, e)
        }

        return templateClass
    }

    private String simplifyName(String name) {
        def simpleName = GrailsStringUtils.substringBeforeLast(name, '.')
        simpleName = simpleName.replaceAll('/', '_')

        if (simpleName.length() > 20) {
            return simpleName.substring(simpleName.length()-20, simpleName.length())
        }

        return simpleName
    }
}
