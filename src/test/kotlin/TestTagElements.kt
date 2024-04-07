import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests to assess the correct behaviour of each Tag Element
 */
class TestTagElements {
    private val xmlSampleFile = File("src/test/resources/XmlSampleFromMainProblem")

    // ------------------- Tests for XML Header ------------------- \\

    private val sampleDefaultHeader: String = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"

    /**
     * Tests if the XML Header is correctly generated.
     * Assesses the string format of it and checks if:
     * - The default header uses version 1.0 and encoding UTF-8
     * - The user can create a header with a different XML version and encoding
     */
    @Test
    fun xmlHeaderShouldBeGeneratedProperly(){
        val defaultHeader = XmlHeader().toString()
        assertEquals(defaultHeader, sampleDefaultHeader)

        val specificHeader = XmlHeader("1.1", "UTF-32").toString()
        assertEquals(specificHeader, "<?xml version=\"1.1\" encoding=\"UTF-32\"?>")
    }

    /**
     * Confirms that a user can't create an XML Header/ Prolog with
     * an invalid XML version, or an unsupported encoding/ charset.
     */
    @Test
    fun invalidXmlHeaderShouldThrowException(){
        assertThrows(IllegalArgumentException::class.java) { XmlHeader(version = "2.0") }
        assertThrows(IllegalArgumentException::class.java) { XmlHeader(encoding = "something") }
    }

    // ------------------- Tests for Adding and Removing Tags ------------------- \\

    @Test
    fun tagsShouldCreatedCorrectly() {
        // assert that an individual tag is created with the correct name
        val parentTag = XmlTag("parentTag")
        assertEquals("parentTag", parentTag.name)

        // assert that another tag can be created and be the first tag's child tag
        val childTag = XmlTag("childTag", parentTag)
        assertEquals("childTag", childTag.name)
        assertEquals(parentTag.name, childTag.parent?.name)

        // assert that the parent tag now has the right child tag associated
        assertEquals(childTag.name, parentTag.children[0].name)

        // confirm that the children list grows correctly when adding another child tag
        val anotherChildTag = XmlTag("anotherChildTag", parentTag)
        assertEquals(anotherChildTag.name, parentTag.children[1].name)
        // also confirm that the new tag's parent isn't its sibling tag
        assertNotEquals(childTag.name, anotherChildTag.parent?.name)
    }

}