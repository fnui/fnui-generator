package fnui.generator.templateengine.result

/**
 * Describes a result file for the UiGenerator
 */
interface UiGeneratorResultFile {

    /**
     * @return the name of the generated artefact
     */
    String getName()

    /**
     * @return the relative path for the generated artefact
     */
    String getRelativePath()

    /**
     * @return the fileName for the generated artefact
     */
    String getFileName()

    /**
     * @return the writable content
     */
    Writable getContent()
}
