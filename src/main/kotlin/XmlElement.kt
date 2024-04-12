/**
 * The interface used to represent the XML elements used in a document (such as tags and their content).
 * This implements 2 design patterns simultaneously: the Composite design pattern (where the XmlTag is the Composite,
 * and the XmlTagContent is a Leaf), and the Visitor design pattern.
 *
 * @constructor Any XmlElement must be given a name, and may or may not be given a parent
 * (Composite object that it descends from).
 */
sealed interface XmlElement {
    val name: String
    val parent: XmlTag?

    /**
     * Operation to accept visitor elements, representing a visitor interface through lambdas.
     *
     * @param visitor The XmlElement that is being visited - considered the leaf element by default
     * (an XmlTagContent in this case).
     * @receiver Boolean that represents whether the visitor should keep iterating or not.
     */
    fun accept(visitor: (XmlElement) -> Boolean) {
        visitor(this)
    }


}

/**
 *  Oject that implements the XmlElement interface, the XmlTag is considered the Composite object,
 *  being considered the element that nests others and can have them as children. This is where the visits start.
 *
 * @property name Just like the interface it implements, the XmlTag must be given a name as a String.
 * @property parent An XmlTag may or may not have a parent (another XmlTag).
 *  This property is null by default, if one isn't specified (if the XmlTag is the root element, for example).
 * @property tagAttributes XmlTags can have attributes. This is a map of keys with associated values, all as Strings.
 * Considered null by default, as this isn't mandatory.
 * @constructor An XmlTag must be given a name, and can be given another XmlTag as a parent.
 * It may also include attributes, which are values mapped to certain keys that might show up as
 * <XmlTag key1 = "value1" key2 = "value2"/> on an XML Document.
 */
data class XmlTag(
    override val name: String,
    override val parent: XmlTag? = null,
    val tagAttributes: MutableMap<String, String>? = null
) : XmlElement {
    val children: MutableList<XmlElement> = mutableListOf()

    init {
        parent?.children?.add(this)
    }

    /**
     * Operation to accept visitor elements, representing a visitor interface through lambdas.
     *
     * @param visitor The XmlElement that is being visited - since this is a Composite object,
     * its children (Leaf elements) must also be iterated through and visited.
     * @receiver Boolean that represents whether the visitor should keep iterating or not.
     */
    override fun accept(visitor: (XmlElement) -> Boolean) {
        if (visitor(this))
            children.forEach {
                it.accept(visitor)
            }
    }

    /**
     * Simple method that iterates through all the XmlTags of a Document.
     *
     * @return a list containing all XmlTag elements.
     */
    fun listAllTags(): MutableList<XmlTag> {
        val tagList = mutableListOf<XmlTag>()
        accept {
            if(it is XmlTag) tagList.add(it)
            else true
        }
        return tagList
    }

}

/**
 * This class represents the XML tag content.
 * This is basically the textual content of each tag, and it's considered the leaf element.
 * This can never have other children elements.
 *
 * @property name The text to be presented as the tag content.
 * @property parent The tag which this content belongs to.
 * @constructor Adds this tag content to an existing tag or nested tags (parent element(s)).
 */
data class XmlTagContent(
    override val name: String,
    override val parent: XmlTag?
) : XmlElement {

    init {
        parent?.children?.add(this)
    }

}