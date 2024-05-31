interface XmlElementAdapter {

    fun addOrEditAttribute(element: XmlElement, attributeKey: String, attributeValue: String)
    fun changeAttributes(element: XmlElement, newAttributes: MutableMap<String, String>)
    fun removeAttribute(element: XmlElement, attributeKey: String)

    fun transformElement(element: XmlElement)
}

class FUCAdapter(): XmlElementAdapter {
    override fun addOrEditAttribute(element: XmlElement, attributeKey: String, attributeValue: String) {
        TODO("Not yet implemented")
    }

    override fun changeAttributes(element: XmlElement, newAttributes: MutableMap<String, String>) {
        TODO("Not yet implemented")
    }

    override fun removeAttribute(element: XmlElement, attributeKey: String) {
        TODO("Not yet implemented")
    }

    override fun transformElement(element: XmlElement) {
        element as XmlTag

        element.addOrEditAttribute("id", "cenas")

        TODO("Not yet implemented")
    }
}