/**
 * TODO
 *
 * @constructor
 */
sealed interface XmlElement {
    val name: String
    val parent: XmlTag?

    /**
     * TODO
     *
     * @param visitor
     * @receiver
     */
    fun accept(visitor: (XmlElement) -> Boolean) {
        visitor(this)
    }


}

/**
 * TODO
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

    /**
     * TODO
     *
     * @param visitor
     * @receiver
     */
    override fun accept(visitor: (XmlElement) -> Boolean) {
        if (visitor(this))
            children.forEach {
                it.accept(visitor)
            }
    }

    /**
     * TODO
     *
     * @return
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