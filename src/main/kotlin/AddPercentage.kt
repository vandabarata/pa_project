class AddPercentage {
    fun transform(attribute: String): String {
        if (attribute.toIntOrNull() != null || attribute.toDoubleOrNull() != null) return "$attribute%"
        else throw IllegalArgumentException("Attribute can't be converted to percentage")
    }
}