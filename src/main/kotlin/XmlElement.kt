/**
 * The interface used to represent the XML elements used in a document (such as tags and their content).
 * This implements 2 design patterns simultaneously: the Composite design pattern (where the XmlTag is the Composite,
 * and the XmlTagContent is a Leaf), and the Visitor design pattern.
 *
 * @constructor Any XmlElement must be given a name, and may or may not be given a parent
 * (Composite object that it descends from).
 */
sealed interface XmlElement {
    var name: String
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
 * Considered empty by default, as this isn't mandatory.
 * @constructor An XmlTag must be given a name, and can be given another XmlTag as a parent.
 * It may also include attributes, which are values mapped to certain keys that might show up as
 * <XmlTag key1 = "value1" key2 = "value2"/> on an XML Document.
 */
data class XmlTag(
    override var name: String,
    override val parent: XmlTag? = null,
    private var tagAttributes: MutableMap<String, String> = mutableMapOf()
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
     * User provides an attribute key and an attribute value, which is then added to the attribute map,
     * in case it doesn't exist, or edited, in case the key already exists.
     *
     * @param attributeKey This key is a String that represents the attribute's name to add or edit.
     * @param attributeValue The value that's going to be added or updated onto an existing attribute.
     */
    fun addOrEditAttribute(attributeKey: String, attributeValue: String) {
        this.tagAttributes[attributeKey] = attributeValue
    }

    /**
     * Replaces the old attributes map with a new one. This is especially useful when the user needs to rename
     * attribute names, since these serve as map keys and can't be renamed otherwise.
     *
     * @param newAttributes The new attributes of this XmlTag, mapped by name and value.
     */
    fun changeAttributesMap(newAttributes: MutableMap<String, String>) {
        this.tagAttributes = newAttributes
    }

    /**
     * Remove attribute from this XmlTag's attributes, if it already exists.
     * @throws IllegalArgumentException if the given attribute key isn't a part of this tag's attributes.
     *
     * @param attributeKey The name of the attribute to be removed
     */
    fun removeAttribute(attributeKey: String) {
        if (tagAttributes.containsKey(attributeKey)) this.tagAttributes.remove(attributeKey)
        else throw IllegalArgumentException("Such attribute isn't a part of this XML Tag.")
    }

    val getTagAttributes: MutableMap<String, String>
            get() = tagAttributes

    override fun toString(): String {
/*        return if(children.isEmpty()) "something"
        else "<$name></$name>\n"*/
        var toPrint = ""
        var openTag = "<$name"
        var closeTag = ""

        if (tagAttributes.isEmpty())  {
            openTag += ">"
            closeTag = "</$name>"
        }
        else {
            tagAttributes.forEach {
                openTag += " ${it.key}=\"${it.value}\""
            }
            closeTag = if (children.isEmpty()) "/>"
            else "</$name>"
        }

        val tagContent = children.filterIsInstance<XmlTagContent>()

        if (children.isNotEmpty()) {
            children.forEach {
                if (it is XmlTagContent) toPrint += it
            }
        }

        closeTag += "\n"

        return openTag + toPrint + closeTag
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
    override var name: String,
    override val parent: XmlTag
) : XmlElement {

    init {
        if (parent.children.isEmpty()) parent.children.add(this)
        else throw IllegalArgumentException("Can't add tag content to a tag that has other nested elements.")
    }

    override fun toString(): String {
        return "<${parent.name}>$name</${parent.name}>"
    }
}