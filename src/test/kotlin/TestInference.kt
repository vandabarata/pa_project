import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
        assertEquals("FUC", inference(f).name)
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

    @Test
    fun inferenceShouldCreateRootTagCorrectly() {
        val rootTag = inference(f) as XmlTag

    }

    @Test
    fun shouldSomething() {
        val testDoc = Document(rootElement = inference(f) as XmlTag)
        testDoc.writeXmlToFile("src/test/resources/pleaseWork")
    }
}