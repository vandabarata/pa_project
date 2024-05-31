/**
 * The interface to be used with the `@XmlAdapter` annotation.
 * It allows the user to transform an XmlElement, after it's been processed along with its nested elements.
 */
interface XmlElementAdapter {
    /**
     * The method that allows the user to transform its class,
     * after the XmlElement and its nested elements have been processed.
     *
     * @param element The class in which the annotation is used on, passed as an XmlElement, after processing.
     */
    fun freeTransform(element: XmlElement)
}

/**
 * Implements the XmlElementAdapter interface.
 * Orders the XmlElement's nested tags and attributes alphabetically.
 */
class AlphabeticalAdapter: XmlElementAdapter {
    /**
     * Runs through an XmlElement and orders its nested elements alphabetically, if it has any.
     *
     * @param element The XmlElement to start with.
     */
    override fun freeTransform(element: XmlElement) {
        if (element is XmlTag && element.children.isNotEmpty()) {
            element.accept {
                orderChildTagsAlphabetically(it)
                true
            }
        }
    }

    /**
     * Takes an XmlElement and orders its children (if any) by alphabetical order.
     *
     * @param element The XmlElement to start with.
     */
    private fun orderChildTagsAlphabetically(element: XmlElement) {
        if (element is XmlTag && element.children.isNotEmpty()) element.children.sortBy { it.name }
    }
}