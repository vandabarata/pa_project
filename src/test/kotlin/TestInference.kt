import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.print.Doc

class TestInference {

    private val c = ComponenteAvaliacao("Quizzes", 20)
    private val f = FUC("M4310", "Programação Avançada", 6.0, "la la...",
        listOf(
            ComponenteAvaliacao("Quizzes", 20),
            ComponenteAvaliacao("Projeto", 80)
        )
    )

    @Test
    fun shouldBeAbleToCreateXmlTagWithExpectedTagName() {
        assertEquals("FUC", inference(f).name)
        assertEquals("componente", inference(c).name)
    }

    @Test
    fun shouldListTagAttributesCorrectly() {
        val fucElement = inference(f) as XmlTag
        assertEquals(mapOf(Pair("codigo", "M4310")), fucElement.getTagAttributes)

        val componenteElement = inference(c) as XmlTag
        assertEquals(mapOf(Pair("nome", "Quizzes"), Pair("peso", "20")), componenteElement.getTagAttributes)
    }

    @Test
    fun shouldSomething() {
        val testDoc = Document(XmlHeader(), inference(f) as XmlTag)
        testDoc.writeXmlToFile("src/test/resources/pleaseWork")
    }
}