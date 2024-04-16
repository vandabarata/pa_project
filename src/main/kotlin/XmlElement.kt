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

    /**
     * Turns an XmlElement into its official XML format.
     *
     * @param tabulation The amount of tabulation it should use.
     * @return The String representation of the XML Element.
     */
    fun turnToXml(tabulation: Int = 0): String
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
     * Since there isn't a list of parents for each element, this function allows us to
     * count how many parents an XmlTag has, so we can use that amount to indent the XML file.
     *
     * @return How many parents the current XmlElement is under.
     */
    private fun countParents(): Int {
        var count = 0
        var currentParent = parent

        while (currentParent != null) {
            count++
            currentParent = currentParent.parent
        }

        return count
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

    /**
     * Formats an XmlTag and its children into a proper XML format.
     * Starts by adding indentation according to the tabulation value provided,
     * both to the opening and closing tags, so that each element is coherent.
     *
     * If the tag has no attributes, its tags show up in the usual <tag>\n</tag> format,
     * so it sets the opening and closing tags to the format shown above.
     *
     * If there are attributes, however, then they can be shown in two different ways:
     * - If there's only one, it should look like <tag attributeName="attributeValue">\n</tag>
     * - If there's more than one, then there should be no children and the tag should close in the same line,
     *   and look like <tag attributeName="attributeValue" attributeName="attributeValue"/>.
     *
     * After all of this, we then reach the part where we iterate through the XmlTag's children
     * and call this function for each one of them.
     * To ensure the XML elements are formatted correctly, we start by adding the number of tabs to each element
     * according to how many parents it has. Then, each element is formatted like the explanation above.
     * In the end, all of this is returned in a single String, formatted properly, ready to be written into an XML file.
     *
     * @param tabulation The amount of indents to use, when converting to XML.
     * @return String with properly formatted XML for this XmlTag and all of its nested elements.
     */
    override fun turnToXml(tabulation: Int): String {

        var openTag = "\t".repeat(tabulation)
        var closeTag = openTag

        openTag += "<$name"

        if (tagAttributes.isEmpty())  {
            openTag += ">\n"
            closeTag += "</$name>\n"
        }
        else {
            if (tagAttributes.size == 1) {
                val onlyAttribute = tagAttributes.toList()[0]
                openTag += " ${onlyAttribute.first}=\"${onlyAttribute.second}\">\n"
            } else {
                tagAttributes.forEach { openTag += " ${it.key}=\"${it.value}\"" }
            }

            if (children.isEmpty()) closeTag = "/>\n"
            else closeTag += "</$name>\n"
        }

        var childrenXml = ""
        val nrOfTabs = countParents() + 1
        childrenXml =  children.joinToString("") { it.turnToXml(nrOfTabs) }

        return openTag + childrenXml + closeTag
    }
}

/**
 * This class represents an XML Tag with textual content.
 * The name of the tag represents what shows as the <name></name> and the content is what shows inbetween.
 * It's considered the leaf element, as it can never have other children elements.
 *
 * @property name The text to be presented as the tag name.
 * @property parent The tag under which this is nested.
 * @property content The actual content to be shown inbetween the tags.
 * @constructor Adds this tag content to an existing tag or nested tags (parent element(s)).
 */
data class XmlTagWithContent(
    override var name: String,
    override val parent: XmlTag,
    val content: String
) : XmlElement {

    init {
        parent.children.add(this)
    }

    /**
     * Shows this element as its XML equivalent, which is basically <name>content</name>.
     *
     * @param tabulation The amount of indents to use, when converting to XML.
     * @return The String representation of the XML Tag with Content.
     */
    override fun turnToXml(tabulation: Int): String {
        return "\t".repeat(tabulation) + "<$name>$content</$name>\n"
    }
}