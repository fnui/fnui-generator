includeTargets << grailsScript("_GrailsBootstrap")

target(generateFnui: "Generate the UI following the (FN)UI model") {
    depends(checkVersion, parseArguments, packageApp, loadApp, configureApp)
    def pipeline = grailsApp.classLoader.loadClass('fnui.generator.FnuiGeneratorPipeline').newInstance()
    pipeline.grailsApplication = grailsApp
    pipeline.generateUi()
}

USAGE = """
    generate-fnui
"""

setDefaultTarget(generateFnui)
