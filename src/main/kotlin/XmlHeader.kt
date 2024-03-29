data class XmlHeader(val version: String = "1.0", val encoding: String = "UTF-8") {

}

fun main() {
    print(XmlHeader())
}