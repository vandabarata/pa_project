import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

@Target(AnnotationTarget.CLASS)
annotation class Tag(val tagName: String)

@Target(AnnotationTarget.PROPERTY)
annotation class TagAttribute(val showAttribute: Boolean = true)

@Target(AnnotationTarget.CLASS)
annotation class TagWithContent(val name: String, val content: String)

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

fun inference(obj: Any): XmlElement {
    val tagName =   if (obj::class.hasAnnotation<Tag>()) obj::class.findAnnotation<Tag>()?.tagName
                    else obj::class.simpleName

    val objectOrderedFields = obj::class.orderedFields

    val tagAttributesFromObj: MutableList<Pair<String, String>> = mutableListOf()
    val children: MutableList<Any> = mutableListOf()

    objectOrderedFields.forEach {
        if (it.hasAnnotation<TagAttribute>()) tagAttributesFromObj.add(Pair(it.name, getPropertyValue(obj, it.name)))
        if (it is List<*>) it.forEach { child ->
            inference(child!!)
            children.add(child)
        }
    }

    if (tagAttributesFromObj.isNotEmpty() || children.isNotEmpty()) return XmlTag(tagName!!, tagAttributes = tagAttributesFromObj.toMap(mutableMapOf()))
    // else return XmlTagWithContent(tagName!!, "", "")

    // val tagAttributes = obj::class.orderedFields.map { it -> Pair<String, Any>(it.name, readInstanceProperty(obj, it.name)) }
    // println(tagAttributesFromObj)

    return XmlTag(tagName!!, tagAttributes = tagAttributesFromObj.toMap(mutableMapOf()))
}

fun createTags(annotatedRootTag: KClass<*>) {
    if (annotatedRootTag::class.hasAnnotation<Tag>()) {
        var tag: XmlTag

    }
}


