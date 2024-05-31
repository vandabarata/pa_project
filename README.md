# Object Parser to XML
_Library made for the Advanced Programming subject, in the context of the Master's in Computer Engineering at ISCTE-IUL._
___
![](https://img.shields.io/badge/language-kotlin-purple?style=for-the-badge&logo=kotlin&logoColor=ffffff)

![](https://img.shields.io/github/v/release/vandabarata/pa_project)
![](https://img.shields.io/github/last-commit/vandabarata/pa_project/main?logo=github)

## How it works
This library is able to create XML Documents from a root XML Tag, along with its nested elements.

<details>
    <summary>Brief XML reminder</summary>
    Using this framework assumes basic XML knowledge. <a href="//www.w3schools.com/xml/xml_syntax.asp">This page</a> offers a reminder of basic notions, in case you need that.
</details>

All you need to do is create a class that you intend to use as your root tag.
Then, as properties of the class, you can have other nested tags or tag attributes.

**Example** \
Let's say you want this as a final XML Document (excluding the prolog for now)
```xml
<fuc codigo="M4310">
    <nome>Programação Avançada</nome>
    <ects>6.0</ects>
    <avaliacao>
        <componente nome="Quizzes" peso="20"/>
        <componente nome="Projeto" peso="80"/>
    </avaliacao>
</fuc>
```
One way of achieving that is to have a `FUC` data class and everything else inside it. 
For `componente`, since it's its own tag with attributes and such, you may also create a data class for it and then call it as a list of that class inside `FUC`.

**Tag Attributes** \
In order to be able to tell what is a nested tag or a tag attribute, you need to identify the tag attributes with the `@TagAttribute` annotation.

Here's how that would go:
```kotlin
data class Componente (
    @TagAttribute
    val nome: String, 
    @TagAttribute
    val peso: Int
)

data class FUC (
    @TagAttribute
    val codigo: String,
    val nome: String,
    val ects: Double,
    val avaliacao: List<Componente>
)
```

These classes could be translated into the following instances of objects (still in line with the XML example above):
```kotlin
val example = FUC("M4310", "Programação Avançada", 6.0,
    listOf(
        Componente("Quizzes", "20"),
        Componente("Projeto", "80")
    )
)
```

This is the base "vanilla" behaviour you can get. Let's get onto other customizations you can do.

## The fun stuff
Click the indexes for a quick TL;DR of each section 
- [Different tag name](#tag-name)
- [Ignore fields](#ignore)
- [Transform attributes](#transform-attr)
- [Transform elements](#transform-elem)

### <u>I want my tag's name to differ from its class name</u>
There's an annotation for that! Imagine you still want the tag to be named `componente` but you want the class name to be more descriptive.
Not a problem. Just add the annotation `@Tag` passing the intended name as a parameter, like `@Tag("intended name")`.
```kotlin
@Tag("componente")
data class ComponenteAvaliacao (
    @TagAttribute
    val nome: String,
    @TagAttribute
    val peso: Int)
```
By default, the tag name will be a lowercase version of the class name, you should use this annotation if you want it to be different. \
**Note**: This annotation can only be applied to classes, so if you want a different tag name, you need to create a class for it.
<div id='tag-name'></div> 

___
#### _TL;DR_: Add `@Tag("name")` to the tag's class
___

### <u>I want to ignore certain fields of my class</u>
Let's imagine you want to convert your class into an XML Element but you don't want to convert everything into a nested tag or an attribute.
Have you guessed it yet? _There's an annotation for that_: `@Ignore`

Remember our old class? Let's add an extra field called `observacoes`
```kotlin
data class FUC (
    @TagAttribute
    val codigo: String,
    val nome: String,
    val ects: Double,
    @Ignore
    val observacoes: String,
    val avaliacao: List<Componente>
)
```
Now, when you create an object from this, it can be like
```kotlin
val example = FUC("M4310", "Programação Avançada", 6.0, "your field to be ignored",
    listOf(
        Componente("Quizzes", "20"),
        Componente("Projeto", "80")
    )
)
```
and it will be translated into this anyway
```xml
<fuc codigo="M4310">
    <nome>Programação Avançada</nome>
    <ects>6.0</ects>
    <avaliacao>
        <componente nome="Quizzes" peso="20"/>
        <componente nome="Projeto" peso="80"/>
    </avaliacao>
</fuc>
```
<div id='ignore'></div> 


___
#### _TL;DR_: Add `@Ignore` to the property to be ignored
___
### <u>I want to transform an attribute but not change it inside my class</u>
What if I told you _there's an annotation for that too_? 
This framework comes with an interface you can use, called `XmlStringTransformer`.
```kotlin
interface XmlStringTransformer {
    fun transformAttribute(attribute: Any): String
}
```
You can pass a class implementing this framework into any XmlAttribute, and it will transform that attribute in any way you want.
There's a transformation "out of the box" you can use, called `AddPercentage`.
Here's the gist of what it looks like, so you can make your own

```kotlin
class AddPercentage: XmlStringTransformer {
    override fun transformAttribute(attribute: Any): String {
        if (attribute.toString().toIntOrNull() != null || attribute.toString().toDoubleOrNull() != null) return "$attribute%"
        else throw IllegalArgumentException("Attribute can't be converted to percentage")
    }
}
```
In a nutshell, it accepts any value that can be parsed into an Int or Double, and adds a percentage to it.

The way this can be used, based on our former examples, is
```kotlin
data class Componente (
    @TagAttribute
    val nome: String,

    @TagAttribute
    @XmlString(AddPercentage::class)
    val peso: Int)
```
which would then result in "converting" our Int values into a percentage, like so
```xml
<fuc codigo="M4310">
    <ects>6.0</ects>
    <nome>Programação Avançada</nome>
        <componente nome="Quizzes" peso="20%"/>
        <componente nome="Projeto" peso="80%"/>
</fuc>
```
All this without changing the original class itself, only with an annotation. Feel free to be creative as long as you call your method `transformAttribute` and return the attribute as a String.
<div id='transform-attr'></div>

___
#### _TL;DR_: Add `@XmlString(YourXmlStringTransformer::class)` to the attribute
___
### <u>I want to transform my XmlElement after mapping everything</u>
Even if it seems like a weird request, I've also added an annotation for that!
The interface to be used for this is `XmlElementAdapter`.
```kotlin
interface XmlElementAdapter {
    fun freeTransform(element: XmlElement)
}
```
You can pass a class implementing this interface into the annotation of a class you wish to transform.
Out of the box, I've decided to add an `AlphabeticalAdapter` as an example.
The method your adapter class should implement is `freeTransform`. Here's the gist of my own implementation:
```kotlin
class AlphabeticalAdapter: XmlElementAdapter {
    override fun freeTransform(element: XmlElement) {
        if (element is XmlTag && element.children.isNotEmpty()) {
            element.accept {
                orderChildTagsAlphabetically(it)
                true
            }
        }
    }

    private fun orderChildTagsAlphabetically(element: XmlElement) {
        if (element is XmlTag && element.children.isNotEmpty()) element.children.sortBy { it.name }
    }
}
```
You can copy the `freeTransform` method above, create your adapter class and then invoke any method you create. 
Or do something completely different, I'm not the boss of you. This is just an example.

In the end, the XML would be converted from
```xml
<fuc codigo="M4310">
    <nome>Programação Avançada</nome>
    <ects>6.0</ects>
    <avaliacao>
        <componente nome="Quizzes" peso="20"/>
        <componente nome="Projeto" peso="80"/>
    </avaliacao>
</fuc>
```
to
```xml
<fuc codigo="M4310">
    <avaliacao>
        <componente nome="Quizzes" peso="20"/>
        <componente nome="Projeto" peso="80"/>
    </avaliacao>
    <ects>6.0</ects>
    <nome>Programação Avançada</nome>
</fuc>
```
<div id='transform-elem'></div>

___
#### _TL;DR_: Add `@XmlAdapter(YourXmlElementAdapter::class)` to the class you want to transform post mapping
___
