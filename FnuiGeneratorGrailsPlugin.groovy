class FnuiGeneratorGrailsPlugin {
    def version = "0.1"
    def grailsVersion = "2.4 > *"

    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def loadAfter = ['fnui-core', 'fnui-model']

    def title = "FnUI Generate Plugin" // Headline display name of the plugin
    def author = "Florian Freudenberg"
    def authorEmail = "flo@freudenberg.berlin"
    def description = '''\
This plugin provides the tools for the code generation step of the toolchain.
This plugin does not provided any template implementations.
'''

    def documentation = "https://github.com/fnui/fnui-generator"

    def license = "APACHE"

    def scm = [ url: "https://github.com/fnui/fnui-generator" ]

    def doWithSpring = {
        application.config.fnui.ui.template.provider = []
    }
}
