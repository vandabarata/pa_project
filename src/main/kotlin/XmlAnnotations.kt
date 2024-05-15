import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

@Target(AnnotationTarget.CLASS)
annotation class Tag(val name: String)

@Target(AnnotationTarget.PROPERTY)
annotation class TagAttribute

@Target(AnnotationTarget.PROPERTY)
annotation class Ignore


/**
 * Returns a KClass' list of properties by the order of their initialization.
 *
 * Taken from https://github.com/andre-santos-pt during Advanced Programming class.
 */
val KClass<*>.orderedFields: List<KProperty<*>>
    get() {
        require(isData) { "instance must be data class" }
        return primaryConstructor!!.parameters.map { p ->
            declaredMemberProperties.find { it.name == p.name }!!
        }
    }

/**
 * Reads the value of a KProperty.
 *
 * Taken from https://stackoverflow.com/a/35539628
 *
 * @param obj The object/ class we're retrieving the property value from.
 * @param propertyName The name of the property we want to get the value of.
 * @return The value of the property.
 */
@Suppress("UNCHECKED_CAST")
fun <R> getPropertyValue(obj: Any, propertyName: String): R {
    val property = obj::class.members.first { it.name == propertyName } as KProperty1<Any, *>

    // force invalid cast exception if incorrect type here
    return property.get(obj) as R
}

fun inference(obj: Any, parent: XmlTag? = null): XmlElement {

    if (XmlTagWithContent::class == obj::class) {
        return obj as XmlTagWithContent
    }

    val tagName =   if (obj::class.hasAnnotation<Tag>()) obj::class.findAnnotation<Tag>()?.name
                    else obj::class.simpleName

    val orderedParameters = obj::class.orderedFields
    val tagAttributesFromObj: MutableList<Pair<String, String>> = mutableListOf()
    var hasChildren = false
    val mappedListsOfChildren: MutableMap<String, List<Any>> = mutableMapOf()
    val tagsWithContent: MutableList<Pair<String, String>> = mutableListOf()
    var tagContent: String = ""

    orderedParameters.forEach {
        // Skip if ignored field
        if (it.hasAnnotation<Ignore>()) return@forEach

        val propertyName = it.name
        val propertyValue: Any = getPropertyValue(obj, propertyName)

        // Process attributes (fields with @TagAttribute annotation)
        if (it.hasAnnotation<TagAttribute>()) tagAttributesFromObj.add(Pair(propertyName, propertyValue.toString()))

        // Process child leaf tags (any field that doesn't have @TagAtrribute and isn't a List
        if (!it.hasAnnotation<TagAttribute>() && propertyValue !is List<*>) {
            tagContent = propertyValue.toString()
            tagsWithContent.add(Pair(propertyName, tagContent))
        }

        // Process child composite tags (any field that is a List)
        if (propertyValue is List<*>) {
            hasChildren = true
            val children: MutableList<Any> = mutableListOf()
            propertyValue.forEach { child ->
                children.add(child!!)
            }
            mappedListsOfChildren.putIfAbsent(propertyName, children)
        }
    }

    val currentParentTag = XmlTag(tagName!!, parent, tagAttributes = tagAttributesFromObj.toMap(mutableMapOf()))

    tagsWithContent.forEach {
        inference(XmlTagWithContent(it.first, parent?: currentParentTag , it.second))
    }

    val xmlElementToReturn =
        if (tagAttributesFromObj.isNotEmpty() || hasChildren) {
            currentParentTag
        }
        else XmlTagWithContent(tagName, parent?: currentParentTag, tagContent)

    if (XmlTag::class == xmlElementToReturn::class && hasChildren) {
        mappedListsOfChildren.forEach { tag ->
            val childParentTag = XmlTag(tag.key, xmlElementToReturn as XmlTag)
            tag.value.forEach {
                inference(it, XmlTag(tag.key, childParentTag))
            }
        }
    }

    return xmlElementToReturn
}



