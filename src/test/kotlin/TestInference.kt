import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class TestInference {
    // examples from the Project's phase 2 instructions
    private val c = ComponenteAvaliacao("Quizzes", 20)
    private val inferredCTag = inference(c) as XmlTag
    private val f = FUC("M4310", "Programação Avançada", 6.0, "la la...",
        listOf(
            ComponenteAvaliacao("Quizzes", 20),
            ComponenteAvaliacao("Projeto", 80)
        )
    )
    private val inferredFTag = inference(f) as XmlTag
    private val firstInferenceDoc = Document(rootElement = inference(f) as XmlTag)

    /**
     * Confirms that tags are created with correct name,
     * whether it's the class name or the one passed through annotation.
     */
    @Test
    fun shouldBeAbleToCreateXmlTagWithExpectedTagName() {
        assertEquals("fuc", inferredFTag.name)
        assertEquals("componente", inferredCTag.name)
    }

    /**
     * Confirms that the XmlTag's attributes processed through inference are correctly added to the tag.
     */
    @Test
    fun shouldListTagAttributesCorrectly() {
        assertEquals(mapOf(Pair("codigo", "M4310")), inferredFTag.getTagAttributes)
        assertEquals(mapOf(Pair("nome", "Quizzes"), Pair("peso", "20")), inferredCTag.getTagAttributes)
    }

    /**
     * Confirms that the inferred XmlTag's children are identified correctly.
     */
    @Test
    fun shouldListInferredChildrenCorrectly() {
        val fChildrenNames: MutableList<String> = mutableListOf()
        val expectedChildren = listOf("nome", "ects", "avaliacao")
        inferredFTag.children.forEach { fChildrenNames.add(it.name) }
        assertEquals(expectedChildren, fChildrenNames)

        val cChildrenNames: MutableList<String> = mutableListOf()
        inferredCTag.children.forEach { cChildrenNames.add(it.name) }
        assertEquals(emptyList<String>(), cChildrenNames)
    }

    /**
     * Confirms that the inference is working as expected, by creating a document using the inferred tag
     * as the root element, and comparing it to a document of the final xml that's expected.
     */
    @Test
    fun inferenceShouldCreateDocumentCorrectly() {
        val inferenceFirstFile = File("src/test/resources/FirstInferenceExampleFromMainProblem")
        val testFirstInferenceFile = File("src/test/resources/testFirstInference")

        firstInferenceDoc.writeXmlToFile(testFirstInferenceFile.toString())

        val mismatch = Files.mismatch(inferenceFirstFile.toPath(), testFirstInferenceFile.toPath())
        // mismatch returns -1 if the files' contents match
        assertEquals(-1, mismatch)
    }
}