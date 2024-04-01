/**
 * Xml element
 *
 * @constructor Create empty Xml element
 */
sealed interface XmlElement {
    // val tagText: String
    val parent: XmlTag?
}

/**
 * Xml tag
 *
 * @property parent
 * @constructor Create empty Xml tag
 */
data class XmlTag(
    // override val tagText: String,
    override val parent: XmlTag? = null
) : XmlElement {
    val children: MutableList<XmlElement> = mutableListOf()

    init {
        parent?.children?.add(this)
    }
}

/**
 * Xml tag content
 *
 * @property parent
 * @constructor Create empty Xml tag content
 */
data class XmlTagContent(
    // override val tagText: String,
    override val parent: XmlTag?
) : XmlElement {

    init {
        parent?.children?.add(this)
    }

}