sealed interface XmlElement {
    // val tagText: String
    val parent: XmlTag?
}

data class XmlTag(
    // override val tagText: String,
    override val parent: XmlTag? = null
) : XmlElement {
    val children: MutableList<XmlElement> = mutableListOf()

    init {
        parent?.children?.add(this)
    }
}

data class XmlTagContent(
    // override val tagText: String,
    override val parent: XmlTag?
) : XmlElement {

    init {
        parent?.children?.add(this)
    }

}