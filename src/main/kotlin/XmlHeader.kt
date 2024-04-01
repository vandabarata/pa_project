import java.nio.charset.Charset


/**
 * Also known as the XML Prolog, this class represents the Header of any valid XML file.
 *
 * As a default, it's created with the 1.0 version, and the UTF-8 encoding charset.
 * This can be modified by the user to use any valid XML version, or supported encoding/ charset.
 *
 * @property version The XML version - can only be a valid XML version like 1.0 or 1.1. Is 1.0 by default.
 * @property encoding This is the XML encoding/ charset used. Is UTF-8 by default.
 * @constructor Creates an XML Header only if the `version` and `encoding`/ charset are valid accepted values.
 * @throws IllegalArgumentException if conditions above aren't met.
 */
data class XmlHeader(private val version: String = "1.0", private val encoding: String = "UTF-8") {
    private val validXmlVersions: List<String> = listOf("1.0", "1.1")

    init {
        require(version in validXmlVersions && Charset.isSupported(encoding)) {
            """$version isn't a valid XML version, such as
                |$validXmlVersions,
                |or $encoding isn't a valid supported charset, such as
                |${Charset.availableCharsets().values}""".trimMargin()
        }
    }

    /**
     * Converts the XmlHeader into a valid XML Prolog.
     *
     * @return The valid XML format of an XML Prolog, as a String.
     */
    override fun toString(): String = "<?xml version=\"$version\" encoding=\"$encoding\"?>"
}
