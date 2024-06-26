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

    /**
     * Confirms that AddPercentage's transform is adding a % to a numeric string.
     */
    @Test
    fun addPercentageShouldAddPercentageToString() {
        val percent = AddPercentage().transformAttribute("50")
        assertEquals("50%", percent)
    }

    /**
     * Assess that a numerical attribute's value can be converted into percentage,
     * using the approppriate annotation.
     */
    @Test
    fun shouldBeAbleToConvertPercentageAsAnnotationInClass() {
        // same as original class but with added @XmlString annotation to add percentage
        data class ComponenteAvaliacao2 (
            @TagAttribute
            val nome: String,

            @TagAttribute
            @XmlString(AddPercentage::class)
            val peso: Int)

        val testPercentTag = inference(ComponenteAvaliacao2("Quizzes", 20)) as XmlTag
        assertEquals(mapOf(Pair("nome", "Quizzes"), Pair("peso", "20%")), testPercentTag.getTagAttributes)

        // same as class above but the attribute "peso" uses value of type "Any"
        data class TestComponent (
            @TagAttribute
            val nome: String,

            @TagAttribute
            @XmlString(AddPercentage::class)
            val peso: Any)

        assertThrows(IllegalArgumentException::class.java) { inference(TestComponent("", "anything")) }
    }

    /**
     * Confirms that a class with the @XmlAdapter annotation is correctly transformed, as an XmlElement.
     * In this particular exemple, the transformation to assess it's ordering child tags and attributes by their name
     * in alphabetical order instead of as they appear in class.
     */
    @Test
    fun shouldBeAbleToAdaptElement() {
        val testExpectedAlphabeticalInferenceFile = File("src/test/resources/expectedXmlPostMappingAlphabetical")
        val testAlphabeticalInferenceFile = File("src/test/resources/testAlphabetical")

        @XmlAdapter(AlphabeticalAdapter::class)
        data class FUC1 (
            @TagAttribute
            val codigo: String,
            val nome: String,
            val ects: Double,
            @Ignore
            val observacoes: String,
            val avaliacao: List<ComponenteAvaliacao>,
            val cenas: String,
            val anatomia: String
        )

        val fucAlphabetical = FUC1("M4310", "Programação Avançada", 6.0, "la la...",
            listOf (ComponenteAvaliacao("Quizzes", 20), ComponenteAvaliacao("Projeto", 80)),
            "algo", "último algo"
        )

        Document(rootElement = inference(fucAlphabetical) as XmlTag).writeXmlToFile(testAlphabeticalInferenceFile.toString())

        val mismatch = Files.mismatch(testExpectedAlphabeticalInferenceFile.toPath(), testAlphabeticalInferenceFile.toPath())
        // mismatch returns -1 if the files' contents match
        assertEquals(-1, mismatch)
    }
}