import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

/**
 * Annotation to be used when considering a different
 * XmlElement name than the class' being inferred.
 *
 * @property name The intended XmlElement name to be used.
 */
@Target(AnnotationTarget.CLASS)
annotation class Tag(val name: String)

/**
 * Annotation to identify any class property as a tag's attribute.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class TagAttribute

/**
 * Annotation used to ignore a class' property when inferring an XmlElement.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Ignore

/**
 * Annotation used to adapt string elements from an XmlElement
 * into something else, according to the passed class
 *
 * @property adapterClass The class inside of which there's an adapter function to transform the string.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XmlString(val adapterClass: KClass<out XmlStringTransformer>)

@Target(AnnotationTarget.CLASS)
annotation class XmlAdapter(val adapterClass: KClass<out XmlElementAdapter>)

// _______________________________ Objects Inference _______________________________ \\
/**
 * Infers any class into an XmlElement, through annotations and reflection.
 *
 * @param obj The class/ object to analyse and convert to XmlElement (XmlTag or XmlTagWithContent).
 * @param parent This is null by default but when infering recursively, a parent XmlTag may be passed.
 * @return Will return a leaf XmlElement (XmlTagWithContent) if one is passed, or a processed XmlTag otherwise.
 */
fun inference(obj: Any, parent: XmlTag? = null): XmlElement {
    // If the object passed is already a leaf tag (happens when these are processed recursively),
    // simply return it, as these don't need further processing
    if (obj is XmlTagWithContent) return obj

    // process this object as a XmlTag with possible attributes
    val tagName = getTagName(obj)
    val objectFields = obj::class.orderedFields
    val tagAttributes: MutableList<Pair<String, String>> = mutableListOf()

    // assume there are no children unless proven otherwise
    var hasChildren = false

    // map of this tag's child tags (XmlTag) with lists of their own child tags (XmlTag or XmlTagWithContent)
    val childCompositeTags: MutableMap<String, List<Any>> = mutableMapOf()
    // child leaf tags (XmlTagWithContent)
    val leafTags: MutableList<XmlTagWithContent> = mutableListOf()

    // process attributes (and skip ignored fields)
    objectFields.forEach {
        // Skip if ignored field
        if (it.hasAnnotation<Ignore>()) return@forEach

        val propertyName = it.name
        val propertyValue: Any = getPropertyValue(obj, propertyName)
        var value = propertyValue.toString()

        // Process attributes (fields with @TagAttribute annotation)
        if (it.hasAnnotation<TagAttribute>()) {

            // Transform any attribute with the XmlString annotation
            if (it.hasAnnotation<XmlString>()) value = it.findAnnotation<XmlString>()!!.adapterClass.createInstance().transformAttribute(value)

            tagAttributes.add(Pair(propertyName, value))
            return@forEach
        }
    }

    // this XmlTag is needed to further process its children, passing itself as an XmlTag parent to them
    val finalTag = XmlTag(tagName, parent, tagAttributes = tagAttributes.toMap(mutableMapOf()))

    // process other fields that are children XmlElements
    objectFields.forEach {
        // Skip already processed fields
        if (it.hasAnnotation<Ignore>() || it.hasAnnotation<TagAttribute>()) return@forEach

        val propertyName = it.name
        val propertyValue: Any = getPropertyValue(obj, propertyName)

        // if the code has gotten here, it means there's children tags
        hasChildren = true

        // Add child composite tags (XmlTag) that may have their own children to a map of tags linking them a list of their own children
        if (propertyValue is List<*>) {
            val children: MutableList<Any> = mutableListOf()
            propertyValue.forEach { child -> children.add(child!!) }
            childCompositeTags.putIfAbsent(propertyName, children)
        }
        // Add child leaf tags (XmlTagWithContent) to a list
        else leafTags.add(XmlTagWithContent(propertyName, finalTag, propertyValue.toString()))
    }

    // recursively process this tag's children, if there are any
    if (hasChildren) {
        leafTags.forEach { inference(it) }

        childCompositeTags.forEach { tag ->
            val compositeTagsParent = XmlTag(tag.key, finalTag)
            tag.value.forEach { inference(it, compositeTagsParent) }
        }
    }

    // Run adapters

    return finalTag
}

/**
 * Util function to infer XmlElement name from an object's class or annotation.
 *
 * @param obj Any class/ object.
 * @return The tag name inferred from that class.
 */
private fun getTagName(obj: Any): String {
    return  if (obj::class.hasAnnotation<Tag>()) obj::class.findAnnotation<Tag>()!!.name
            else obj::class.simpleName!!.lowercase()
}

/**
 * Returns a KClass' list of properties by the order of their initialization.
 *
 * Taken from https://github.com/andre-santos-pt during Advanced Programming class.
 */
private val KClass<*>.orderedFields: List<KProperty<*>>
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
private fun <R> getPropertyValue(obj: Any, propertyName: String): R {
    val property = obj::class.members.first { it.name == propertyName } as KProperty1<Any, *>

    // force invalid cast exception if incorrect type here
    return property.get(obj) as R
}
