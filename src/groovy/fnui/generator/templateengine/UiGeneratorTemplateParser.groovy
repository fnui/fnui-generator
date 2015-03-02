package fnui.generator.templateengine

import fnui.UiGenerationException

/**
 * The UiGeneratorTemplateParser parse a template into a Groovy class which provides the template as a Writable.
 * It provides indention support for sub-templates.
 *
 * For the usable syntax see the templates provided in fnui-generator.
 *
 * The UiGeneratorTemplateParser is based on the implementation of the  {@link groovy.text.GStringTemplateEngine}.
 */
class UiGeneratorTemplateParser {
    final static String INDENTION_VAR = '_indention'
    final static String INDENTION_BEFORE_VAR = '_indention_before'
    final static String CLASS_TEMPLATE_1 = '''\
package fnui.generator.tmp.templates

String TEMPLATE_NAME = "'''
    final static String CLASS_TEMPLATE_2 = '''"

def getTemplate() {
    return { out ->'''

    final static char LESS_THAN = '<' as char
    final static char GREATER_THAN = '>' as char
    final static char PERCENTAGE = '%' as char
    final static char DOLLAR = '$' as char
    final static char QUOTE = '"' as char
    final static char OPENING_BRACE = '{' as char
    final static char CLOSING_BRACE = '}' as char
    final static char SLASH = '\\' as char
    final static char ASSIGN = '=' as char
    final static char HASH = '#' as char
    final static char CARRIAGE_RETURN = '\r' as char
    final static char NEW_LINE = '\n' as char
    final static char WHITESPACE = ' ' as char
    final static char TAB = '\t' as char
    final static char PIPE = '|' as char

    private final Reader reader
    private boolean parsed = false

    private StringBuilder templateExpressions = new StringBuilder()
    private StringBuilder newLineChars = new StringBuilder()
    private StringBuilder whitespaceChars = new StringBuilder("\${${INDENTION_VAR}}")
    private boolean foundNewline = true

    /**
     * Create a template parse for the content of the provided reader.
     *
     * @param reader which provides the the template
     */
    UiGeneratorTemplateParser(Reader reader) {
        this.reader = reader
    }

    /**
     * Parse the template into a valid Groovy class definition.
     *
     * @param templateName
     * @return the class definition
     */
    String parse(String templateName) {
        if (parsed) {
            return templateExpressions
        }

        templateExpressions << CLASS_TEMPLATE_1
        templateExpressions << templateName
        templateExpressions << CLASS_TEMPLATE_2

        try {
            constructTemplate()
        } catch (e) {
            throw new UiGeneratorTemplateParserException(templateName, templateExpressions.toString(), e)
        }
        parsed = true

        return templateExpressions.toString()
    }

    private void constructTemplate() {
        openString()
        while (true) {
            int c = next()
            if (c == -1) {
                break
            }

            if (foundNewline && (c == WHITESPACE || c == TAB)) {
                whitespaceChars << (c as char)
                continue
            } else if (c == NEW_LINE) {
                writeWhitespaces()
                newLineChars << NEW_LINE
                whitespaceChars << "\${${INDENTION_VAR}}"
                foundNewline = true
                continue
            } else if (c == CARRIAGE_RETURN) {
                writeWhitespaces()
                c = next()
                if (c == -1) {
                    appendTemplate(CARRIAGE_RETURN)
                    break
                }

                if (c == NEW_LINE) {
                    newLineChars << CARRIAGE_RETURN
                    newLineChars << NEW_LINE
                    whitespaceChars << "\${${INDENTION_VAR}}"
                    foundNewline = true
                    continue
                }
            }

            if (c == LESS_THAN) {
                c = next()
                if (c == -1) {
                    appendTemplate(LESS_THAN)
                    break
                }

                if (c == PERCENTAGE) {
                    c = next()
                    if (c == -1) {
                        appendTemplate(PERCENTAGE)
                        break
                    }

                    if (c == ASSIGN) {
                        writeWhitespaces()
                        parseExpression()
                        continue
                    } else if (c == HASH) {
                        parseIndentedExpression()
                        continue
                    } else {
                        dropWhitespaces()
                        parseSection(c)
                        continue
                    }
                } else {
                    writeWhitespaces()
                    appendTemplate(LESS_THAN)
                }
            } else if (c == QUOTE) {
                writeWhitespaces()
                appendTemplate(SLASH)
            } else if (c == DOLLAR) {
                writeWhitespaces()
                appendTemplate(DOLLAR)
                c = next()
                if (c == -1) {
                    break
                }

                if (c == OPENING_BRACE) {
                    appendTemplate(OPENING_BRACE)
                    parseGString()
                    continue
                }
            }

            if (foundNewline) {
                writeWhitespaces()
            }

            appendTemplate(c)
        }
        closeString()

        // close the method and class
        templateExpressions << '}}'
    }

    private void dropWhitespaces() {
        foundNewline = false
        newLineChars = new StringBuilder()
        whitespaceChars = new StringBuilder()
    }

    private void writeWhitespaces() {
        templateExpressions << newLineChars
        templateExpressions << whitespaceChars
        dropWhitespaces()
    }

    private void parseGString() {
        while (true) {
            int c = next()
            if (c == -1) break

            appendTemplate(c)
            if (c == CLOSING_BRACE) break
            // TODO: this only works for a subset of GString like ${d.d} but not for ${a?"A:${b}":b}. Parsing GStrings correctly is not worth the effort currently.
        }
    }

    private void parseExpression() {
        templateExpressions << '${'
        parseUntilEndSection()
        templateExpressions << '}'
    }

    private void parseSection(int pendingC) {
        closeString()
        appendTemplate(pendingC)
        parseUntilEndSection()
        openString()
    }

    private void parseIndentedExpression() {
        templateExpressions << newLineChars
        closeString()
        templateExpressions << "${INDENTION_BEFORE_VAR} = ${INDENTION_VAR}; ${INDENTION_VAR} = \"${whitespaceChars}\""
        dropWhitespaces()

        openString()
        templateExpressions << '${'
        boolean openSection = parseUntilEndSection()

        if (openSection) {
            templateExpressions << '"""'
            parseUntilPipeSectionEnd()
            templateExpressions << '"""'
            if (parseUntilEndSection()) {
                throw new UiGenerationException("There was an openSectionEnd ('|%>') after another openSection which is not supported.")
            }
            templateExpressions << '}'
            closeString()
            templateExpressions << "${INDENTION_VAR} = ${INDENTION_BEFORE_VAR}"
            openString()
        } else {
            templateExpressions << '}'
            closeString()
            templateExpressions << "${INDENTION_VAR} = ${INDENTION_BEFORE_VAR}"
            openString()
        }
    }

    private void parseUntilPipeSectionEnd() {
        while (true) {
            int c = next()
            if (c == -1) break

            if (foundNewline && (c == WHITESPACE || c == TAB)) {
                whitespaceChars << (c as char)
                continue
            } else if (c == NEW_LINE) {
                writeWhitespaces()
                newLineChars << NEW_LINE
                whitespaceChars << "\${${INDENTION_BEFORE_VAR}}"
                foundNewline = true
                continue
            } else if (c == CARRIAGE_RETURN) {
                writeWhitespaces()
                c = next()
                if (c == -1) {
                    appendTemplate(CARRIAGE_RETURN)
                    break
                }

                if (c == NEW_LINE) {
                    newLineChars << CARRIAGE_RETURN
                    newLineChars << NEW_LINE
                    whitespaceChars << "\${${INDENTION_BEFORE_VAR}}"
                    foundNewline = true
                    continue
                }
            }

            if (c == LESS_THAN) {
                c = next()
                if (c == -1) break
                if (c == PERCENTAGE) {
                    c = next()
                    if (c == PIPE) break
                    writeWhitespaces()
                    appendTemplate(LESS_THAN)
                    appendTemplate(PERCENTAGE)
                } else {
                    writeWhitespaces()
                    appendTemplate(LESS_THAN)
                }
            }
            writeWhitespaces()
            appendTemplate(c)
        }
        dropWhitespaces()
    }

    /**
     * @return true if section end was open section end
     */
    private boolean parseUntilEndSection() {
        while (true) {
            int c = next()
            if (c == -1) return false

            if (c == PERCENTAGE) {
                c = next()
                if (c == GREATER_THAN) return false
                appendTemplate(PERCENTAGE)
            } else if (c == PIPE) {
                c = next()
                if (c == -1) return false
                if (c == PERCENTAGE) {
                    c = next()
                    if (c == GREATER_THAN) return true
                    appendTemplate(PIPE)
                    appendTemplate(PERCENTAGE)
                } else {
                    appendTemplate(PIPE)
                }
            }
            appendTemplate(c)
        }
    }

    private int next() {
        reader.read()
    }

    private void appendTemplate(int c) {
        templateExpressions << (c as char)
    }

    private void appendTemplate(char c) {
        templateExpressions << c
    }

    private void openString() {
        templateExpressions << '\nout << """'
    }

    private void closeString() {
        templateExpressions << '"""\n'
    }
}
