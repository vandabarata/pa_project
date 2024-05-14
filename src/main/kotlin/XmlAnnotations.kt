import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@Target(AnnotationTarget.CLASS)
annotation class Tag(val name: String)

@Target(AnnotationTarget.PROPERTY)
annotation class TagAttribute(val showAttribute: Boolean = true)


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
    val tagName =   if (obj::class.hasAnnotation<Tag>()) obj::class.findAnnotation<Tag>()?.name
                    else obj::class.simpleName

    val objectOrderedFields = obj::class.orderedFields
    val tagAttributesFromObj: MutableList<Pair<String, String>> = mutableListOf()
    var hasChildren = false
    val childrenLists: MutableMap<String, List<Any>> = mutableMapOf()
    val tagsWithContent: MutableList<Pair<String, String>> = mutableListOf()
    var tagContent: String = ""
    var parentTagName: String = ""

    objectOrderedFields.forEach {
        val propertyName = it.name
        val propertyValue: Any = getPropertyValue(obj, it.name)

        if (it.hasAnnotation<TagAttribute>() && it.findAnnotation<TagAttribute>()!!.showAttribute) tagAttributesFromObj.add(Pair(propertyName, propertyValue.toString()))
        else if (it.hasAnnotation<TagAttribute>() && !it.findAnnotation<TagAttribute>()!!.showAttribute) return@forEach

        println(it.returnType.classifier)
        if (it is List<*>) {
            print("I'm a list!")
            hasChildren = true
            parentTagName = propertyName
            val children: MutableList<Any> = mutableListOf()
            it.forEach { child ->
                children.add(child!!)
            }
            childrenLists.putIfAbsent(parentTagName, children)
        }

        // TODO Create tags with content
        if (!it.hasAnnotation<TagAttribute>() && it.returnType.classifier !is List<*>) tagsWithContent.add(Pair(propertyName, propertyValue.toString()))
    }

    val xmlElementToReturn =
        if (tagAttributesFromObj.isNotEmpty() || hasChildren) {
            XmlTag(tagName!!, parent, tagAttributes = tagAttributesFromObj.toMap(mutableMapOf()))
        }
        else XmlTagWithContent(tagName!!, parent as XmlTag, tagContent)

    if (XmlTag::class == xmlElementToReturn::class && hasChildren) {
        childrenLists.forEach { tag ->
            val childParentTag = XmlTag(tag.key, xmlElementToReturn as XmlTag)
            tag.value.forEach {
                inference(it, XmlTag(tag.key, childParentTag))
            }
        }
    }

    return xmlElementToReturn
}



