/**
 * Xml element
 *
 * @constructor Create empty Xml element
 */
sealed interface XmlElement {
    val name: String
    val parent: XmlTag?
}

/**
 * Xml tag
 *
 * @property parent
 * @constructor
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

//    override fun toString(): String = """Tag Name: $name
//        |${if (parent != null) "Parent: $parent" else ""}
//        |${if (children.isNotEmpty()) "Children: $children" else ""}
//        |${if (tagAttributes.isNullOrEmpty()) "" else "Attributes: $tagAttributes"}
//        |""".trimMargin()
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