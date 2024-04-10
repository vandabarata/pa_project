/**
 * Document
 *
 * @constructor
 *
 * @param header
 * @param rootElement
 */
class Document(
    header: XmlHeader,
    rootElement: XmlTag) {

    val docHeader: String = header.toString()
    val docRoot: XmlTag = rootElement

    override fun toString(): String = """
        Header: $docHeader
        Tags: $docRoot
    """.trimIndent()

}