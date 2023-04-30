# KSerGen

Kotlin read-only `val`, `data class`, and
[kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)
provide us a nice way to structure our data in concurrent programs.
However, we experienced the following inconveniences:

1. Need to manually register subclasses for [open-polymorphism](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#open-polymorphism).
2. Sometimes we need both a mutable version and an immutable version of the same data.

This library is an attempt to improve the development experience.

## Generate serializers module

### Dependency 1

Add [Maven Central](https://central.sonatype.com/) to the `repositories`:

```kotlin
repositories {
    mavenCentral()
}
```

Add the Google ksp plugin:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "$kspVersion"
}
```

Add the `ksergen-ksp` dependency:

```kotlin
dependencies {
    ksp("com.github.adriankhl.ksergen:ksergen-ksp:$ksergenVersion")
}
```

You may also want to disable code generation for `test` such that tests are forced to refer to
the generated codes in `main`:

```kotlin
afterEvaluate {
    tasks.named("kspTestKotlin") {
        enabled = false
    }
}
```

### GeneratedModule

Whenever you build your source code (e.g., `gradle build`),
the `ksergen-ksp` will scan the parents of your `@Serializable` classes
to generate a `GeneratedModule` object at the `ksergen` package:

```kotlin
public object GeneratedModule {
    public val serializersModule: SerializersModule = SerializersModule {
        polymorphic(SerializableParentData::class) {
            subclass(SimpleSerializableData::class)
        }
        polymorphic(MutableSerializableParentData::class) {
            subclass(MutableExternalMasterData::class)
        }
        polymorphic(SerializableParentData::class) {
            subclass(ExternalMasterData::class)
        }
    }

}
```

You can then use the generated `serializersModule` for your serializers:

```kotlin
val format = Json {
    encodeDefaults = true
    serializersModule = GeneratedModule.serializersModule
}

val a = MutableExternalPolymorphicData()
val b: String = format.encodeToString(a)
```

### Package name option

You can add following block in `build.gradle.kts` to change the package of `GeneratedModule`:

```kotlin
ksp {
    arg("generatedModulePackage", "my.package")
}
```

## Generate immutable data class

### Dependency 2

In addition to the [ksp dependency](#dependency-1),
you need to add the `ksp-annotations` dependency:

```kotlin
dependencies {
    ksp("com.github.adriankhl.ksergen:ksergen-annotations:$ksergenVersion")
}
```

### Mutable data class to immutable data class

The name of a mutable data class has to start with `Mutable`,
you can apply the `@GenerateImmutable` to automatically generate an
immutable version of the class within the same package.

Original code:

```kotlin
@GenerateImmutable
data class MutableIntData(var i1: Int = 1, var i2: Int = 2)

@GenerateImmutable
@SerialName("Demo")
data class MutableDemoData(
    var id: MutableIntData = MutableIntData(),
    var il: MutableList<Int> = mutableListOf(1, 2),
    var idl: MutableList<MutableIntData> = mutableListOf(MutableIntData()),
)
```

Generated code:

```kotlin

@Serializable
@SerialName("ksergen.mock.base.MutableIntData")
public data class IntData(
    public val i1: Int,
    public val i2: Int,
)

@Serializable
@SerialName(`value` = "Demo")
public data class DemoData(
    public val id: IntData,
    public val il: List<Int>,
    public val idl: List<IntData>,
)
```

### Serialization of mutable data and immutable data

The `@GenerateImmutable` annotation itself is annotated with
[MetaSerializable](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-core/kotlinx.serialization/-meta-serializable/),
so the annotated data class is serializable.
The generated immutable data class has a `serialName` that is the same
with the `serialName` of the original mutable class.
The [GeneratedModule](#generatedmodule) also takes into account of these classes.

This is a sample test code to show how the serialization works:
```kotlin
fun serializationTest() {
    val format = Json {
        encodeDefaults = true
        serializersModule = GeneratedModule.serializersModule
    }

    val a = MutableDemoData()
    val b: String = format.encodeToString(a)
    val c: DemoData = format.decodeFromString(b)
    val d: String = format.encodeToString(c)
    val e: MutableDemoData = format.decodeFromString(d)

    assertEquals(a, e)
}
```

### What about default values and member functions?

In our use case, it would be handy if we can also copy default values and member functions
to the generated class.
Unfortunately, because
[ksp does not support expression-level information](https://github.com/google/ksp/issues/191),
this is not possible.

Instead, you can use serialization to default-initialize immutable data classes and use
extension functions to emulate member functions of immutable data classes:

```kotlin
fun IntData.sum(): Int = i1 + i2
fun MutableIntData.sum(): Int = i1 + i2

fun sumTest() {
    val format = Json {
        encodeDefaults = true
        serializersModule = GeneratedModule.serializersModule
    }

    val mid = MutableIntData()
    val id: IntData = format.decodeFromString(format.encodeToString(mid))


    val s1 = mid.sum()
    val s2 = id.sum()

    assertEquals(s1, s2)
}
```

## Related libraries

[kopykat](https://github.com/kopykat-kt/kopykat) implements a `copy` method to modify a
nynested immutable data class. Actually, this library is inspired by `kopykat`,
but we created this library since the solution
provided by `kopykat` doesn't suit our need.
