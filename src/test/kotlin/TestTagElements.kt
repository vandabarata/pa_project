import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests to assess the correct behaviour of each Tag Element
 */
class TestTagElements {
    private val xmlSampleFile = File("src/test/resources/XmlSampleFromMainProblem")
    private val sampleDefaultHeader: String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"

    /**
     * Tests if the XML Header is correctly generated.
     * Assesses the string format of it and assesses if:
     * - The default header uses version 1.0 and encoding UTF-8
     * - The user can create a header with a different XML version and encoding
     */
    @Test
    fun xmlHeaderShouldBeGeneratedProperly(){
        val defaultHeader = XmlHeader().toString()
        assertEquals(defaultHeader, sampleDefaultHeader)

        val specificHeader = XmlHeader(version="1.1", encoding="UTF-32").toString()
        assertEquals(specificHeader, "<?xml version=\"1.1\" encoding=\"UTF-32\"?>")
    }


}