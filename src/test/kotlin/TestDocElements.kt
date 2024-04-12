import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests to assess the correct behaviour of each Xml Element
 */
class TestDocElements {
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

    // ------------------- Tests for Nesting Tags ------------------- \\

    private val rootTag = XmlTag("rootTag")
    private val childTag = XmlTag("childTag", rootTag)
    private val anotherChildTag = XmlTag("anotherChildTag", rootTag)
    private val anotherChildTagChild = XmlTag("anotherChildTagChild", anotherChildTag)
    private val deepesteChildTagContent = XmlTagContent("deepestChildTagContent", anotherChildTagChild)
    private val deepestTag = XmlTag("deepestTag", childTag)
    private val someXmlContent = XmlTagContent("aTagContent", childTag)
    private val xmlDoc = Document(XmlHeader(), rootTag)

    /**
     * Confirms the correct creation and nesting of XmlTag elements.
     */
    @Test
    fun tagsShouldBeCreatedAndNestedCorrectly() {
        // assert that an individual tag is created with the correct name
        assertEquals("rootTag", rootTag.name)

        // assert that another tag can be created and be the first tag's child tag
        assertEquals("childTag", childTag.name)
        assertEquals(rootTag.name, childTag.parent?.name)

        // assert that the parent tag now has the right child tag associated
        assertEquals(childTag.name, rootTag.children[0].name)

        // confirm that the children list grows correctly when adding another child tag
        assertEquals(anotherChildTag.name, rootTag.children[1].name)
        // also confirm that the new tag's parent isn't its sibling tag
        assertNotEquals(childTag.name, anotherChildTag.parent?.name)

        // assert correct nesting of tags
        assertEquals("rootTag", deepestTag.parent?.parent?.name)
    }

    /**
     * Confirms that XmlTag elements are being correctly added to a Document,
     * and remain properly nested.
     */
    @Test
    fun tagsShouldBeCorrectlyAddedToDoc() {
        assertEquals(rootTag.name, xmlDoc.docRoot.name)
        assertEquals(childTag.name, xmlDoc.docRoot.children[0].name)
    }

    /**
     * Confirms that the method to list all tags displays them correctly,
     * and doesn't show any tag content.
     */
    @Test
    fun testShowAllTagsInDoc() {
        // confirm the XmlTagContent isn't included in the tags list
        assertFalse(xmlDoc.docRoot.listAllTags().toString().contains(someXmlContent.name))

        // confirm that the lists contains all expected elements
        assertIterableEquals(arrayListOf(rootTag, childTag, deepestTag, anotherChildTag, anotherChildTagChild), xmlDoc.docRoot.listAllTags())
    }

    // ------------------- Tests for Adding and Removing XmlElements in Document ------------------- \\

    /**
     * Assesses that user is able to remove the XmlTagContent from a Document.
     */
    @Test
    fun shouldBeAbleToRemoveXmlTagContent() {
        val elementToRemove = deepesteChildTagContent
        assertTrue(xmlDoc.listAllElements.contains(elementToRemove))
        xmlDoc.removeElementFromDoc(elementToRemove.name)
        assertFalse(xmlDoc.listAllElements.contains(elementToRemove))
    }

    /**
     * Confirms that user is able to remove an XmlTag from a Document, and all children elements associated with it.
     */
    @Test
    fun shouldBeAbleToRemoveElementAndChildren() {
        val elementToRemove = anotherChildTag
        assertTrue(xmlDoc.listAllElements.contains(elementToRemove))
        xmlDoc.removeElementFromDoc(elementToRemove.name)
        assertFalse(xmlDoc.listAllElements.contains(elementToRemove))
        // TODO: Uncomment when https://github.com/vandabarata/pa_project/issues/13 is fixed
        // assertFalse(xmlDoc.listAllElements.toString().contains(elementToRemove.name))
    }

}