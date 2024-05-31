import java.io.File

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
    header: XmlHeader = XmlHeader(),
    rootElement: XmlTag) {

    private val docHeader: String = header.toString()
    val docRoot: XmlTag = rootElement
    private val allElements = mutableListOf<XmlElement>()

    init {
        visitElements()
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
            if(it.name == tagName) it.addOrEditAttribute(attrName, attrValue)
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
            if (it.name == oldName) it.name = newName
            true
        }
        updateElementList()
    }

    /**
     * Renames a certain attribute in the Document. This only renames the attribute if:
     * - The tag to which it belongs exists in the document,
     * - The given old name of the attribute exists in the map of attributes of that tag
     *
     * @param tagName The tag where the attribute to be renamed belongs.
     * @param attrOldName The current attribute's name (map key), before the renaming.
     * @param attrNewName The new attribute's name (map key), after the renaming.
     */
    fun renameAttributesInDoc(tagName: String, attrOldName: String, attrNewName: String) {
        /**
         * This method works by first finding the tag that corresponds to the given tagName,
         * then it retrieves the current value of the given attribute's current name (attrOldName),
         * then it iterates through a list of the keys of the map,
         * adding the new attibute name as key, mapped with the current value, right before the old attribute's name.
         * If this operation is successful, it then proceeds to remove the old attribute's name entry in the map,
         * making sure to only remove it if we were able to add the new attribute's name, with the same value.
         */

        docRoot.accept {
            if (it.name == tagName && it.getTagAttributes.isNotEmpty()) {
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
            if (it.name == tagName) it.removeAttribute(attributeName)
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

    /**
     * Turns this data structure into a valid XML file,
     * by adding the XML Prolog (docHeader) and the XmlElements to it,
     * formatted as XML.
     *
     * @param file The path to the file to be created, in a String format
     */
    fun writeXmlToFile(file: String) {
        File(file).writeText(docHeader + "\n" + docRoot.turnToXml().trimEnd())
    }

    /**
     * Ierates through the elements to find all the ones that match the given xpath.
     * This function finds all elements that correspond to the path,
     * even if not directly related to the elements before.
     * Example: a/b/c will find all b elements that descend from a,
     *          and all c elements that descend from the b elements found before,
     *          even if b isn't a direct child of a, and even if c isn't a direct child of b.
     *
     * @param xpath The xpath-like path to find the elements. Example: root/child/someOtherChild
     * @param startingElement This is the Document Root by default. Shouldn't need to be changed.
     * Only relevant for the recursivity of this function.
     * @param index This is 0 by default. Also shouldn't need to be changed, and is only relevant
     * for the recursivity of this function.
     * @return Returns a list of XmlElements of all found elements that match the xpath.
     */
    private fun getElementsFromMicroXpath(xpath: String, startingElement: XmlElement = docRoot, index: Int = 0): List<XmlElement> {
        val foundElements: MutableList<XmlElement> = mutableListOf()
        val elementPath = xpath.split("/")

        // if the current index is bigger than the path size, it means we can conclude our search
        // because we reached the end of the xpath
        if (index >= elementPath.size) return foundElements

        // if the current element we're iterating through is the one we're looking for
        if (startingElement.name == elementPath[index]) {
            // and if we're at the last part of the xpath, we add it to the list of elements
            if (index == elementPath.size - 1) {
                foundElements.add(startingElement)
            }
            // otherwise, it means we haven't found the final element yet,
            // so we go to the next part of the xpath, starting from the current element onto its children
            else {
                val nextIndex = index + 1
                if (startingElement is XmlTag) {
                    startingElement.children.forEach {
                        foundElements.addAll(getElementsFromMicroXpath(xpath, it, nextIndex))
                    }
                }
            }
        }

        // in case we haven't yet reached the starting element of the path, we keep iterating until we do
        // and then restart this function from there
        if (startingElement is XmlTag) {
            startingElement.children.forEach {
                foundElements.addAll(getElementsFromMicroXpath(xpath, it, index))
            }
        }
        return foundElements
    }

    /**
     * Wrapper function for the real one giving out the elements list.
     * Serves for processing the elements and returning them on their XML form.
     * This also prevents the user from interfering with the other parameters
     * of the function it's wrapping, as they shouldn't be changed.
     *
     * @param xpath The intended path for the elements to retrive.
     * @return A List with the XML Elements formatted as their XML form in Strings.
     */
    fun getElementXmlFromXpath(xpath: String): List<String> {
        val elementList = getElementsFromMicroXpath(xpath)
        val xmlList = mutableListOf<String>()
        elementList.forEach {
            xmlList.add(it.turnToXml().trimEnd())
        }
        return xmlList
    }

    /**
     * Makes sure to clear the list of elements and update it,
     * by iterating through the children of the root tag again.
     */
    private fun updateElementList() {
        allElements.clear()
        visitElements()
    }

    /**
     * Iterates through all the root Element's children and adds them to the element list.
     */
    private fun visitElements() {
        docRoot.accept {
            allElements.add(it)
            true
        }
    }
}