/**
 * TODO
 *
 * @constructor
 *
 * @param header
 * @param rootElement
 */
class Document(
    header: XmlHeader,
    rootElement: XmlTag) {

    private val docHeader: String = header.toString()
    val docRoot: XmlTag = rootElement
    private val allElements = mutableListOf<XmlElement>()

    init {
        docRoot.accept {
            allElements.add(it)
            true
        }
    }

    val listAllElements: List<XmlElement>
        get() = allElements

    /**
     * TODO
     *
     * @param elementName
     */
    fun removeElementFromDoc(elementName: String) {
        docRoot.accept {
            allElements.removeAll {
                it.name == elementName || it.parent?.name == elementName }
            true
        }
    }
        // allElements.removeAll { it.name == elementName || it.parent?.name == elementName }

    /**
     * TODO
     *
     * @return
     */
    override fun toString(): String = """
        Header: $docHeader
        Tags: $docRoot
    """.trimIndent()

}