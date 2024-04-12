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

    // XmlTags/ Composite Elements
    private val rootTag = XmlTag("rootTag")
    private val childTag = XmlTag("childTag", rootTag)
    private val anotherChildTag = XmlTag("anotherChildTag", rootTag)
    private val anotherChildTagChild = XmlTag("anotherChildTagChild", anotherChildTag)
    private val deepestTag = XmlTag("deepestTag", childTag)
    private val rootTagToBeAdded = XmlTag("rootTagToBeAdded")
    private val randomTag = XmlTag("random")
    private val randomTagChild = XmlTag("randomChild", randomTag)
    private val normalTagToBeAdded = XmlTag("normalTagToBeAdded", anotherChildTagChild)

    // XmlTagContents/ Leaf Elements
    private val someXmlContent = XmlTagContent("aTagContent", childTag)
    private val contentToBeAdded = XmlTagContent("content", normalTagToBeAdded)
    private val deepesteChildTagContent = XmlTagContent("deepestChildTagContent", anotherChildTagChild)

    // Document
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
        assertIterableEquals(arrayListOf(rootTag, childTag, deepestTag, anotherChildTag, anotherChildTagChild, normalTagToBeAdded), xmlDoc.docRoot.listAllTags())
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
        println(xmlDoc.listAllElements)
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
        assertFalse(xmlDoc.listAllElements.toString().contains(elementToRemove.name))
    }

    /**
     * Confirms that user isn't able to remove non-existing element.
     * This doesn't throw an exception, but makes sure the Document's element list remains the same.
     */
    @Test
    fun shouldntBeAbleToRemoveNonExistingElement() {
        assertFalse(xmlDoc.listAllElements.contains(randomTag))
        val elementListBefore = xmlDoc.listAllElements
        xmlDoc.removeElementFromDoc(randomTag.name)
        val elementListAfter = xmlDoc.listAllElements
        assertIterableEquals(elementListBefore, elementListAfter)
    }

    /**
     * Confirms that the user can add any type of XmlElement to a Document,
     * as long as its part of the Document Root's children.
     * Also tests that it's impossible for the user to add an element that's not a part of the Document Root children.
     */
    @Test
    fun shouldBeAbleToAddElementToDoc() {
        assertThrows(IllegalArgumentException::class.java) { xmlDoc.addElementToDoc(rootTagToBeAdded) }
        assertThrows(IllegalArgumentException::class.java) { xmlDoc.addElementToDoc(randomTagChild) }
        xmlDoc.addElementToDoc(normalTagToBeAdded)
        xmlDoc.addElementToDoc(contentToBeAdded)
        assertTrue(xmlDoc.listAllElements.contains(normalTagToBeAdded))
        assertTrue(xmlDoc.listAllElements.contains(contentToBeAdded))
    }

    // ------------------- Tests for Adding and Removing Attributes ------------------- \\

    /**
     * Confirms that a user can always access a tag's attributes,
     * even if those haven't been defined yet.
     */
    @Test
    fun shouldBeAbleToAccessATagsNonExistingAttributes() {
        assertTrue(xmlDoc.docRoot.tagAttributes.isEmpty())
    }

    /**
     * Tests that user can add attributes to an XmlTag.
     */
    @Test
    fun shouldBeAbleToAddAttributesToTag() {
        val newTagWithAttributes = XmlTag("newTag", randomTag, mutableMapOf(Pair("attr", "value")))

        assertTrue(newTagWithAttributes.listAttributes.containsKey("attr"))
        assertTrue(newTagWithAttributes.listAttributes.containsValue("value"))
        assertFalse(newTagWithAttributes.listAttributes.containsKey("newAttr"))
        newTagWithAttributes.addOrEditAttribute("newAttr", "newValue")
        assertTrue(newTagWithAttributes.listAttributes.containsKey("newAttr"))
        assertTrue(newTagWithAttributes.listAttributes.containsValue("newValue"))
    }

    /**
     * Confirms that user can edit attributes, based on a key.
     */
    @Test
    fun shouldBeAbleToEditAttributesOfExistingTag() {
        val newTagWithAttributes = XmlTag("newTag", randomTag, mutableMapOf(Pair("attr", "value")))

        assertTrue(newTagWithAttributes.listAttributes.containsKey("attr") &&
                    newTagWithAttributes.listAttributes["attr"] == "value")
        newTagWithAttributes.addOrEditAttribute("attr", "editedValue")
        assertTrue(newTagWithAttributes.listAttributes["attr"] == "editedValue")

        newTagWithAttributes.addOrEditAttribute("something", "editedValue")
        // confirm that, despite same value, a new key is added
        assertTrue(newTagWithAttributes.listAttributes.containsKey("something") &&
                newTagWithAttributes.listAttributes.containsKey("attr"))
    }

    /**
     * Assesses that user can remove existing attributes from an XmlTag.
     */
    @Test
    fun shouldBeAbleToRemoveAttributes() {
        val newTagWithAttributes = XmlTag("newTag", randomTag, mutableMapOf(Pair("attr", "value")))

        assertTrue(newTagWithAttributes.listAttributes["attr"] == "value")
        newTagWithAttributes.removeAttribute("attr")
        assertFalse(newTagWithAttributes.listAttributes["attr"] == "value")
        assertTrue(newTagWithAttributes.listAttributes.isEmpty())
    }

    /**
     * Confirms that user isn't able to remove non-existing attribute.
     */
    @Test
    fun shouldntBeAbleToRemoveNonExistingAttribute() {
        val newTagWithAttributes = XmlTag("newTag", randomTag, mutableMapOf(Pair("attr", "value")))

        assertThrows(IllegalArgumentException::class.java) { newTagWithAttributes.removeAttribute("")}
    }

}