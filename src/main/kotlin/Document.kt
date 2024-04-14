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
     * Finds XmlTags with the oldName and renames them with the given newName.
     *
     * @param oldName The XmlTag's current name in the Document.
     * @param newName The new XmlTag's name in the Document.
     */
    fun renameTagInDoc(oldName: String, newName: String) {
        docRoot.accept {
            if (it is XmlTag && it.name == oldName) it.name = newName
            true
        }
        updateElementList()
    }

    /**
     * Renames a certain attribute in the Document. This only renames the attribute if:
     * - The tag to which it belongs exists in the document,
     * - The given old name of the attribute exists in the map of attributes of that tag
     *
     * This method works by first finding the tag that corresponds to the given tagName,
     * then it retrieves the current value of the given attribute's current name (attrOldName),
     * then it iterates through a list of the keys of the map,
     * adding the new attibute name as key, mapped with the current value, right before the old attribute's name.
     * If this operation is successful, it then proceeds to remove the old attribute's name entry in the map,
     * making sure to only remove it if we were able to add the new attribute's name, with the same value.
     *
     * @param tagName The tag where the attribute to be renamed belongs.
     * @param attrOldName The current attribute's name (map key), before the renaming.
     * @param attrNewName The new attribute's name (map key), after the renaming.
     */
    fun renameAttributesInDoc(tagName: String, attrOldName: String, attrNewName: String) {
        docRoot.accept {
            if (it is XmlTag && it.name == tagName) {
                val oldAttributes = it.getTagAttributes
                val currentValue = oldAttributes.getValue(attrOldName)

                val attributesList = oldAttributes.toList().toMutableList()
                attributesList[attributesList.indexOf(Pair(attrOldName, currentValue))] = Pair(attrNewName, currentValue)
                val newAttributesMap = attributesList.toMap() as MutableMap<String, String>
                it.changeAttributesMap(newAttributesMap)
            }
            true
        }
        updateElementList()
    }

    /**
     * Removes an attribute from a certain tag in the Document.
     *
     * @param tagName The name of the tag where the attribute belongs.
     * @param attributeName The name of the attribute to be removed.
     */
    fun removeAttributesFromTagInDoc(tagName: String, attributeName: String) {
        docRoot.accept {
            if (it is XmlTag && it.name == tagName) it.removeAttribute(attributeName)
            true
        }
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

    fun pretty(): String {
        updateElementList()
        var toPrint = ""
        docRoot.accept {
            toPrint += it.toString()
            true
        }
        return toPrint
    }

}