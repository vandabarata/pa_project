/**
 * Returns only the main nested tags of the Document root.
 */
infix fun Document.childTags(
    action: (XmlElement) -> Unit
) {
    docRoot.children.forEach { action(it) }
}

/**
 * Returns this XmlTag's nested XmlElement whose name matches the one given.
 *
 * @param name The name of the XMLElement to be returned.
 */
operator fun XmlTag.get(name: String) =
    children.find { it.name == name } as XmlElement

/**
 * Adds an empty XmlTag to an existing one, as its child.
 *
 * @param newTagName The name of the new XmlTag to be added.
 */
operator fun XmlTag.plus(newTagName: String): XmlTag {
    return XmlTag(newTagName, this)
}
