import java.nio.charset.StandardCharsets

/**
 * Xml header
 *
 * @property version
 * @property encoding
 * @constructor Create empty Xml header
 */
data class XmlHeader(private val version: String = "1.0", private val encoding: String = "UTF-8") {
    private val validXmlVersions: List<String> = listOf("1.0", "1.1")
    override fun toString(): String = "<?xml version=\"$version\" encoding=\"$encoding\"?>"

    fun isValidXmlHeader(): Boolean = true

    companion object {
        public val whatever = listOf(Charsets)
        public val validEncodings: List<String> = listOf(Charsets.toString())
    }

}


fun main(){
    for (charset in listOf(Charsets)) {
        println(charset.toString())
    }
}

