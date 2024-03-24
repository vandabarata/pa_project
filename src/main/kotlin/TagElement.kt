sealed interface TagElement {
    val tagText: String
    val parent: RootTag?
}

data class RootTag(
    override val tagText: String,
    override val parent: RootTag? = null
) : TagElement {
    val children: MutableList<TagElement> = mutableListOf()
}

data class NestedTag(
    override val tagText: String,
    override val parent: RootTag?
) : TagElement {

}