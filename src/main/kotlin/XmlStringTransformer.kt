interface XmlStringTransformer {
    /**
     * Interface to be used as basis for classes that allow
     * the client to transform attributes with the `@XmlString` annotation.
     *
     * @param attribute Any kind of parameter type.
     * @return Returns the attribute as a String after transforming it.
     */
    fun transformAttribute(attribute: Any): String
}

/**
 * Implements the XmlStringTransformer interface.
 * Adds a percentage symbol to an attribute, and returns it as a String.
 */
class AddPercentage: XmlStringTransformer {
    /**
     * Only method needed to perform this class' function.
     *
     * @param attribute The tag attribute, which can be of Any type.
     * @return That attribute, after transformation, as a String.
     */
    override fun transformAttribute(attribute: Any): String {
        if (attribute.toString().toIntOrNull() != null || attribute.toString().toDoubleOrNull() != null) return "$attribute%"
        else throw IllegalArgumentException("Attribute can't be converted to percentage")
    }
}