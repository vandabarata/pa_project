interface XmlStringTransformer {
    fun transformAttribute(attribute: Any): String
}

class AddPercentage(): XmlStringTransformer {
    override fun transformAttribute(attribute: Any): String {
        if (attribute.toString().toIntOrNull() != null || attribute.toString().toDoubleOrNull() != null) return "$attribute%"
        else throw IllegalArgumentException("Attribute can't be converted to percentage")
    }

}