import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class TestInference {
    // examples from the Project's phase 2 instructions
    private val c = ComponenteAvaliacao("Quizzes", 20)
    private val f = FUC("M4310", "Programação Avançada", 6.0, "la la...",
        listOf(
            ComponenteAvaliacao("Quizzes", 20),
            ComponenteAvaliacao("Projeto", 80)
        )
    )

    /**
     * Confirms that tags are created with correct name,
     * whether it's the class name or the one passed through annotation.
     */
    @Test
    fun shouldBeAbleToCreateXmlTagWithExpectedTagName() {
        assertEquals("fuc", inference(f).name)
        assertEquals("componente", inference(c).name)
    }

    /**
     * Confirms that the XmlTag's attributes processed through inference are correctly added to the tag.
     */
    @Test
    fun shouldListTagAttributesCorrectly() {
        val fucElement = inference(f) as XmlTag
        assertEquals(mapOf(Pair("codigo", "M4310")), fucElement.getTagAttributes)

        val componenteElement = inference(c) as XmlTag
        assertEquals(mapOf(Pair("nome", "Quizzes"), Pair("peso", "20")), componenteElement.getTagAttributes)
    }

    /**
     * Confirms that the inference is working as expected, by creating a document using the inferred tag
     * as the root element, and comparing it to a document of the nesting and tagging that's expected.
     */
    @Test
    fun inferenceShouldCreateDocumentCorrectly() {
        val inferenceFirstFile = File("src/test/resources/FirstInferenceExampleFromMainProblem")
        val testFirstInferenceFile = File("src/test/resources/testFirstInference")

        Document(rootElement = inference(f) as XmlTag).writeXmlToFile(testFirstInferenceFile.toString())

        val mismatch = Files.mismatch(inferenceFirstFile.toPath(), testFirstInferenceFile.toPath())
        // mismatch returns -1 if the files' contents match
        assertEquals(-1, mismatch)
    }
}