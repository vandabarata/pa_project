/**
 * Creates an entity called Document, which is the class that contains all elements that compose an XML Document.
 *
 * @constructor When being initialized, the Document creates a list with all elements under the root XmlTag,
 * which is what's going to be manipulated going forward.
 *
 * @param header An element to represent the XML Prolog with the XML version and encoding being used.
 * @param rootElement The XmlTag under where all other elements are nested.
 */
class Document(
    header: XmlHeader,
    rootElement: XmlTag) {

    private val docHeader: String = header.toString()
    val docRoot: XmlTag = rootElement
    private val allElements = mutableListOf<XmlElement>()

    init {
        updateElementList()
    }

    /**
     * Makes sure to clear the list of elements and update it, by iterating through the children of the root tag again.
     */
    private fun updateElementList() {
        allElements.clear()
        docRoot.accept {
            allElements.add(it)
            true
        }
    }

    val listAllElements: List<XmlElement>
        get() = allElements

    /**
     * Adds an XmlElement to the Document's list of elements if its parentage is a part of the Document.
     *
     * @param element XmlElement to be added to the Document. Can't be a root element.
     * Must be a child of the Document's root element, or its children.
     */
    fun addElementToDoc(element: XmlElement) {
        if (allElements.contains(element.parent?: "")) allElements.add(element)
        else throw IllegalArgumentException (
            "Can't add ${element.name} to Document, since it doesn't belong to ${docRoot.name}'s children or their children")
    }

    /**
     * Given the name of a tag, an attribute name and an attribute value, adds this attribute to the XmlTag,
     * if it's a part of the Document.
     *
     * @param tagName The name of the tag to add the attribute to.
     * @param attrName The name/ key of the attribute.
     * @param attrValue The value of the attribute.
     */
    fun addAttributeToTag(tagName: String, attrName: String, attrValue: String) {
        allElements.forEach {
            if(it.name == tagName && it is XmlTag) it.addOrEditAttribute(attrName, attrValue)
        }
        updateElementList()
    }


    /**
     * Removes the XmlElement from its parent's children list, as well as from the Document's list of elements.
     * If the element is an XmlTag, makes sure to clear its children as well.
     *
     * @param elementName The name of the element to be removed.
     */
    fun removeElementsFromDoc(elementName: String) {
        val elementsToRemove = mutableListOf<XmlElement>()
        docRoot.accept {
            if (it.name == elementName) {
                if (it is XmlTag) {
                    it.children.clear()
                }
                elementsToRemove.add(it)
            }
            true
        }
        elementsToRemove.forEach { element ->
            element.parent?.children?.remove(element)
        }
        updateElementList()
    }

    /**
     * Overrides the toString method to show the Header/ XML Prolog,
     * and the XML elements that compose it.
     *
     * @return a String showing the XML Prolog and all the XML elements, clearly separated.
     */
    override fun toString(): String = """
        Header: $docHeader
        XML Elements: $allElements
    """.trimIndent()

}