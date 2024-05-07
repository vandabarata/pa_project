@Target(AnnotationTarget.CLASS)
annotation class Tag(val name: String)

@Target(AnnotationTarget.PROPERTY)
annotation class TagAttribute(val showAttribute: Boolean = true)

@Target(AnnotationTarget.CLASS)
annotation class TagWithContent(val name: String, val content: String)


