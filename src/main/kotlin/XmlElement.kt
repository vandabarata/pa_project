/**
 * Xml element
 *
 * @constructor Create empty Xml element
 */
sealed interface XmlElement {
    val parent: XmlTag?
}

/**
 * Xml tag
 *
 * @property parent
 * @constructor Create empty Xml tag
 */
data class XmlTag(
    val tagAttributes: MutableList<String>? = null,
    override val parent: XmlTag? = null
) : XmlElement {
    val children: MutableList<XmlElement> = mutableListOf()

    init {
        parent?.children?.add(this)
    }
}

/**
 * This class represents the XML tag content.
 * This is basically the textual content of each tag, and it's considered the leaf element.
 * This can never have other children.
 *
 * @property parent The tag of which this content belongs to.
 * @constructor Create empty Xml tag content
 */
data class XmlTagContent(
    val tagText: String,
    override val parent: XmlTag?
) : XmlElement {

    init {
        parent?.children?.add(this)
    }

}