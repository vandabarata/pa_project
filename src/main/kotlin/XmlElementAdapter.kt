interface XmlElementAdapter {
    fun transformElement(element: XmlElement)
}

class OrderElementAttributes(): XmlElementAdapter {
    override fun transformElement(element: XmlElement) {
        TODO("Not yet implemented")
    }
}

class FUCAdapter(): XmlElementAdapter {
    override fun transformElement(element: XmlElement) {
        element as XmlTag

        element.addOrEditAttribute("id", "cenas")

        TODO("Not yet implemented")
    }
}