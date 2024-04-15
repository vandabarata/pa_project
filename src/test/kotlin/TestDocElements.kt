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

    // ------------------- Tests for Nesting Tags/ Elements ------------------- \\

    // XmlTags/ Composite Elements
    private val rootTag = XmlTag("rootTag")
    private val rootChildTag = XmlTag("childTag", rootTag)
    private val anotherRootChildTag = XmlTag("anotherChildTag", rootTag)
    private val childOfAnotherRootChildTag = XmlTag("anotherChildTagChild", anotherRootChildTag)
    private val childOfRootChildTag = XmlTag("deepestTag", rootChildTag)
    private val childlessTag = XmlTag("childlessTag", anotherRootChildTag)
    private val someTagWithAttributes = XmlTag("someTagWithAttributes", rootTag,
        mutableMapOf(Pair("An Attribute", "With a Value"), Pair("Yet Another Attribute", "With Another Value")))
    private val randomTag = XmlTag("random")

    // XmlTagContent / Leaf Elements
    private val aTagContent = XmlTagContent("aTagContent", childOfRootChildTag)

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
        assertEquals("childTag", rootChildTag.name)
        assertEquals(rootTag.name, rootChildTag.parent?.name)

        // assert that the parent tag now has the right child tag associated
        assertEquals(rootChildTag.name, rootTag.children[0].name)

        // confirm that the children list grows correctly when adding another child tag
        assertEquals(anotherRootChildTag.name, rootTag.children[1].name)
        // also confirm that the new tag's parent isn't its sibling tag
        assertNotEquals(rootChildTag.name, anotherRootChildTag.parent?.name)

        // assert correct nesting of tags
        assertEquals("rootTag", childOfRootChildTag.parent?.parent?.name)
    }

    /**
     * Confirms that XmlTag elements are being correctly added to a Document,
     * and remain properly nested.
     */
    @Test
    fun tagsShouldBeCorrectlyAddedToDoc() {
        assertEquals(rootTag.name, xmlDoc.docRoot.name)
        assertEquals(rootChildTag.name, xmlDoc.docRoot.children[0].name)
    }

    /**
     * Confirms user is able to add an XmlTagContent to an XmtTag (without children), and a Document,
     * and also that user can't add an XmlTagContent to an XmlTag that already has children.
     *
     */
    @Test
    fun shouldBeAbleToAddXmlTagContent() {
        assertTrue(xmlDoc.listAllElements.contains(aTagContent))
        assertThrows(IllegalArgumentException::class.java) { XmlTagContent("something", rootTag) }
    }



    // ------------------- Tests for Adding and Removing XmlElements in Document ------------------- \\

    /**
     * Assesses that user is able to remove the XmlTagContent from a Document.
     */
    @Test
    fun shouldBeAbleToRemoveXmlTagContent() {
        val deepesteChildTagContent = XmlTagContent("deepestChildTagContent", childOfAnotherRootChildTag)
        xmlDoc.addElementToDoc(deepesteChildTagContent)

        assertTrue(xmlDoc.listAllElements.contains(deepesteChildTagContent))
        xmlDoc.removeElementsFromDoc(deepesteChildTagContent.name)
        assertFalse(xmlDoc.listAllElements.contains(deepesteChildTagContent))
    }

    /**
     * Confirms that user is able to remove an XmlTag from a Document, and all children elements associated with it.
     */
    @Test
    fun shouldBeAbleToRemoveElementAndChildren() {
        assertTrue(xmlDoc.listAllElements.contains(anotherRootChildTag))
        xmlDoc.removeElementsFromDoc(anotherRootChildTag.name)
        assertFalse(xmlDoc.listAllElements.contains(anotherRootChildTag))
        assertFalse(xmlDoc.listAllElements.toString().contains(anotherRootChildTag.name))
    }

    /**
     * Confirms that a user can remove several elements, if they share the same name.
     */
    @Test
    fun shouldBeAbleToRemoveSeveralElementsWithTheSameName() {
        val oneTag = XmlTag("aTagName", rootTag)
        val someOtherTag = XmlTag("aTagName", anotherRootChildTag)
        xmlDoc.addElementToDoc(oneTag)
        xmlDoc.addElementToDoc(someOtherTag)

        assertTrue(xmlDoc.listAllElements.contains(oneTag))
        assertTrue(xmlDoc.listAllElements.contains(someOtherTag))
        xmlDoc.removeElementsFromDoc(oneTag.name)
        assertFalse(xmlDoc.listAllElements.contains(oneTag))
        assertFalse(xmlDoc.listAllElements.contains(someOtherTag))
        assertFalse(xmlDoc.listAllElements.toString().contains(someOtherTag.name))
    }

    /**
     * Confirms that user isn't able to remove non-existing element.
     * This doesn't throw an exception, but makes sure the Document's element list remains the same.
     */
    @Test
    fun shouldntBeAbleToRemoveNonExistingElement() {
        assertFalse(xmlDoc.listAllElements.contains(randomTag))
        val elementListBefore = xmlDoc.listAllElements
        xmlDoc.removeElementsFromDoc(randomTag.name)
        val elementListAfter = xmlDoc.listAllElements
        assertIterableEquals(elementListBefore, elementListAfter)
    }

    /**
     * Confirms that the user can add any type of XmlElement to a Document,
     * as long as it's part of the Document Root's children.
     * Also tests that it's impossible for the user to add an element that's not a part of the Document Root children.
     */
    @Test
    fun shouldBeAbleToAddElementToDoc() {
        val rootTagToBeAdded = XmlTag("rootTagToBeAdded")
        val randomTagChild = XmlTag("randomChild", randomTag)
        val normalTagToBeAdded = XmlTag("normalTagToBeAdded", childOfAnotherRootChildTag)
        val contentToBeAdded = XmlTagContent("content", normalTagToBeAdded)

        assertThrows(IllegalArgumentException::class.java) { xmlDoc.addElementToDoc(rootTagToBeAdded) }
        assertThrows(IllegalArgumentException::class.java) { xmlDoc.addElementToDoc(randomTagChild) }
        xmlDoc.addElementToDoc(normalTagToBeAdded)
        xmlDoc.addElementToDoc(contentToBeAdded)
        assertTrue(xmlDoc.listAllElements.contains(normalTagToBeAdded))
        assertTrue(xmlDoc.listAllElements.contains(contentToBeAdded))
    }

    // ------------------- Tests for Adding and Removing Attributes in XmlTags ------------------- \\

    /**
     * Confirms that a user can always access a tag's attributes,
     * even if those haven't been defined yet.
     */
    @Test
    fun shouldBeAbleToAccessATagsNonExistingAttributes() {
        assertTrue(xmlDoc.docRoot.getTagAttributes.isEmpty())
    }

    /**
     * Tests that user can add attributes to an XmlTag.
     */
    @Test
    fun shouldBeAbleToAddAttributesToTag() {
        val newTagWithAttributes = XmlTag("newTag", randomTag, mutableMapOf(Pair("attr", "value")))

        assertTrue(newTagWithAttributes.getTagAttributes.containsKey("attr"))
        assertTrue(newTagWithAttributes.getTagAttributes.containsValue("value"))
        assertFalse(newTagWithAttributes.getTagAttributes.containsKey("newAttr"))
        newTagWithAttributes.addOrEditAttribute("newAttr", "newValue")
        assertTrue(newTagWithAttributes.getTagAttributes.containsKey("newAttr"))
        assertTrue(newTagWithAttributes.getTagAttributes.containsValue("newValue"))
    }

    /**
     * Confirms that user can edit attributes, based on a key.
     */
    @Test
    fun shouldBeAbleToEditAttributesOfExistingTag() {
        val newTagWithAttributes = XmlTag("newTag", randomTag, mutableMapOf(Pair("attr", "value")))

        assertTrue(newTagWithAttributes.getTagAttributes.containsKey("attr") &&
                    newTagWithAttributes.getTagAttributes["attr"] == "value")
        newTagWithAttributes.addOrEditAttribute("attr", "editedValue")
        assertTrue(newTagWithAttributes.getTagAttributes["attr"] == "editedValue")

        newTagWithAttributes.addOrEditAttribute("something", "editedValue")
        // confirm that, despite same value, a new key is added
        assertTrue(newTagWithAttributes.getTagAttributes.containsKey("something") &&
                newTagWithAttributes.getTagAttributes.containsKey("attr"))
    }

    /**
     * Assesses that user can remove existing attributes from an XmlTag.
     */
    @Test
    fun shouldBeAbleToRemoveAttributes() {
        val newTagWithAttributes = XmlTag("newTag", randomTag, mutableMapOf(Pair("attr", "value")))

        assertTrue(newTagWithAttributes.getTagAttributes["attr"] == "value")
        newTagWithAttributes.removeAttribute("attr")
        assertFalse(newTagWithAttributes.getTagAttributes["attr"] == "value")
        assertTrue(newTagWithAttributes.getTagAttributes.isEmpty())
    }

    /**
     * Confirms that user isn't able to remove non-existing attribute.
     */
    @Test
    fun shouldntBeAbleToRemoveNonExistingAttribute() {
        val newTagWithAttributes = XmlTag("newTag", randomTag, mutableMapOf(Pair("attr", "value")))

        assertThrows(IllegalArgumentException::class.java) { newTagWithAttributes.removeAttribute("")}
    }

    /**
     * Confirms that user can add attributes to a Document's XmlTag, based on the XmlTag's name.
     */
    @Test
    fun shouldBeAbleToAddAttributesToDocument() {
        val tag = childlessTag
        xmlDoc.addAttributeToTag(tag.name, "testAttributeName", "testAttributeValue")
        assertTrue(xmlDoc.listAllElements.toString().contains("testAttributeName"))
        xmlDoc.listAllElements.forEach {
            if(it.name == tag.name)
                assertTrue(tag.getTagAttributes.containsKey("testAttributeName")
                            && tag.getTagAttributes.containsValue("testAttributeValue"))
        }
    }

    /**
     * Assesses that user can't add attributes to non-existing tags in document.
     */
    @Test
    fun shouldntBeAbleToAddAttributesToNonExistingTagsInDocument() {
        xmlDoc.addAttributeToTag(randomTag.name, "testRandomAttributeName", "testRandomAttributeValue")
        xmlDoc.listAllElements.forEach {
            assertFalse(it.name == randomTag.name)
            if(it.name == randomTag.name)
                assertFalse(randomTag.getTagAttributes.containsKey("testAttributeName")
                        && randomTag.getTagAttributes.containsValue("testAttributeValue"))
        }
    }

    // ------------------- Tests for manipulating Document Elements ------------------- \\

    /**
     * Confirms that the user can rename an XmlTag, by checking that the element's old name has changed
     * to the new given name, and that the element itself still exists in the Document.
     */
    @Test
    fun shouldBeAbleToRenameATag() {
        val newChildTag = XmlTag("ancientTag", rootTag)
        xmlDoc.addElementToDoc(newChildTag)
        xmlDoc.renameTagInDoc("ancientTag", "renamedTag")
        assertFalse(xmlDoc.listAllElements.toString().contains("ancientTag"))
        assertTrue(xmlDoc.listAllElements.toString().contains("renamedTag"))
        assertTrue(xmlDoc.listAllElements.contains(newChildTag))
    }

    /**
     * Assesses that the user is able to rename attributes of a tag in a certain document.
     * This test checks that:
     * - A new tag with attributes has been added to the document
     * - The tag the user added contains the right attribute value mapped to the right attribute name
     * - The attributes after the renaming are different from the ones before
     * - After the renaming, the tag contains the right attribute value mapped to the new attribute name
     * - The attribute names before and after the renaming are different
     * - The attribute values before and after the renaming are the exact same, meaning their order remains unchanged
     */
    @Test
    fun shouldBeAbleToRenameAttribute() {
        val newTagName = "newTagWithAttributes"
        val newTagWithAttributes = XmlTag(newTagName, rootTag, mutableMapOf(Pair("oldName", "valueToRemain")))
        xmlDoc.addElementToDoc(newTagWithAttributes)
        xmlDoc.addAttributeToTag(newTagName, "anotherAttr", "anotherValue")
        xmlDoc.addAttributeToTag(newTagName, "someOtherAttr", "yetAnotherValue")
        xmlDoc.addAttributeToTag(newTagName, "lastAttr", "lastAnotherValue")

        assertTrue(xmlDoc.listAllElements.contains(newTagWithAttributes))

        xmlDoc.listAllElements.forEach {
            if (it is XmlTag && it.name == newTagName) assertEquals("valueToRemain", it.getTagAttributes["oldName"])
        }

        val oldAttributes = newTagWithAttributes.getTagAttributes

        xmlDoc.renameAttributesInDoc(newTagName, "oldName", "newName")

        val newAttributes = newTagWithAttributes.getTagAttributes
        assertNotEquals(newAttributes, oldAttributes)

        xmlDoc.listAllElements.forEach {
            if (it is XmlTag && it.name == newTagName) assertEquals("valueToRemain", it.getTagAttributes["newName"])
        }
        assertNotEquals(oldAttributes.keys, newAttributes.keys)
        assertIterableEquals(oldAttributes.values, newAttributes.values)
    }

    /**
     * Confirms that user can remove an attribute from a tag belonging to a Document by
     * giving a tag name and the attribute's name.
     */
    @Test
    fun shouldBeAbleToRemoveAttributeFromDocTag() {
        xmlDoc.addElementToDoc(XmlTag("tagToTest", rootTag,
            tagAttributes = mutableMapOf(Pair("attributeToRemove", "some value"))))

        xmlDoc.listAllElements.forEach {
            if (it is XmlTag && it.name == "tagToTest") assertTrue(it.getTagAttributes.containsKey("attributeToRemove"))
        }

        xmlDoc.removeAttributesFromTagInDoc("tagToTest", "attributeToRemove")

        xmlDoc.listAllElements.forEach {
            if (it is XmlTag && it.name == "tagToTest") assertFalse(it.getTagAttributes.containsKey("attributeToRemove"))
        }
    }

    @Test
    fun letsTestStrings() {
        // println(xmlDoc.listAllElements)
        println(xmlDoc.pretty())
    }

}