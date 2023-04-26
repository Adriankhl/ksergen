package ksergen.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import ksergen.annotations.GenerateImmutable

/**
 * Generate a file to store the immutable versions of the classes from the original source file
 *
 * @param packageName the package name of the generated file
 * @param fileName the file name of the generated file
 * @param declarationList generate the immutable version of these classes
 * @param logger logger for ksp process
 *
 * @return a kotlin-poet FileSpec
 */
fun generateImmutableFile(
    packageName: String,
    fileName: String,
    declarationList: List<KSClassDeclaration>,
    logger: KSPLogger,
): FileSpec {
    return FileSpec.builder(
        packageName = packageName,
        fileName = fileName,
    ).apply {
        indent("    ")

        declarationList.forEach { declaration ->
            // handle interface and class differently
            // interface does not need to have the @Serializable annotation
            val builder = if (declaration.classKind == ClassKind.INTERFACE) {
                TypeSpec.interfaceBuilder(declaration.simpleName.getShortName().drop(7))
            } else {
                TypeSpec.classBuilder(declaration.simpleName.getShortName().drop(7)).apply {
                    addAnnotation(Serializable::class)

                    // Copy the original SerialName if it exists
                    // otherwise use the full class path as the serialname
                    // so they can be serialized to each other
                    if (declaration.hasAnnotation(SerialName::class)) {
                        val serialNameAnnotation: KSAnnotation =
                            declaration.annotations.first {
                                it.shortName.getShortName() == "SerialName"
                            }
                        addAnnotation(
                            serialNameAnnotation.toAnnotationSpec()
                        )
                    } else {
                        addAnnotation(
                            AnnotationSpec.builder(SerialName::class)
                                .addMember("%S", declaration.qualifiedName!!.asString())
                                .build()
                        )
                    }

                    declaration.superTypes.map {
                        it.resolve()
                    }.forEach {
                        val superDeclaration: KSDeclaration = it.declaration
                        if (superDeclaration is KSClassDeclaration) {
                            if (superDeclaration.classKind != ClassKind.INTERFACE) {
                                superclass(convertFullTypeImmutable(it, logger))
                            }
                        }
                    }

                    // Set primary constructor and property for the same property
                    // so that the generated data class has a proper constructor layout
                    primaryConstructor(
                        FunSpec.constructorBuilder().apply {
                            declaration.primaryConstructor!!.parameters.forEach { parameter ->
                                addParameter(
                                    name = parameter.name!!.getShortName(),
                                    type = convertFullTypeImmutable(
                                        parameter.type.resolve(),
                                        logger
                                    )
                                )
                            }
                        }.build()
                    )

                    declaration.getAllProperties().forEach { property ->
                        property.modifiers
                        addProperty(
                            PropertySpec.builder(
                                property.simpleName.asString(),
                                convertFullTypeImmutable(property.type.resolve(), logger),
                            ).initializer(property.simpleName.asString())
                                .build()
                        )
                    }
                }
            }

            // Common generation logic for both interface and class
            builder.apply {
                addModifiers(
                    declaration.modifiers.mapNotNull { it.toKModifier() }
                )

                declaration.superTypes.map {
                    it.resolve()
                }.forEach {
                    val superDeclaration: KSDeclaration = it.declaration
                    if (superDeclaration is KSClassDeclaration) {
                        if (superDeclaration.classKind == ClassKind.INTERFACE) {
                            addSuperinterface(convertFullTypeImmutable(it, logger))
                        }
                    }
                }
            }

            addType(builder.build())
        }
    }.build()
}

/**
 * Generate ksergen.serializers.module.GeneratedModule to help automatically
 * registering class-subclass relation for polymorphic serialization
 *
 * @param serializableDeclarations classes with Serialization annotation
 * @param immutableDeclarations classes with GenerateImmutable annotation
 * @param logger logger for ksp process
 *
 * @return a kotlin-poet FileSpec
 */
fun generateSerializersModuleFile(
    packageName: String,
    serializableDeclarations: List<KSClassDeclaration>,
    immutableDeclarations: List<KSClassDeclaration>,
    logger: KSPLogger,
): FileSpec {
    // only register the relation if both the child and the parent are serializable,
    // i.e., with Serializable notation or with GenerateImmutable notation
    val originalSerializablePairs: List<Pair<KSClassDeclaration, KSClassDeclaration>> =
        serializableDeclarations.filter {
            // sealed class cannot be a subclass in the polymorphic serializer
            !it.modifiers.contains(Modifier.SEALED)
        }.flatMap { c ->
            c.getAllSuperTypes().map { s ->
                s.declaration
            }.filterIsInstance<KSClassDeclaration>().filter { p ->
                !p.modifiers.contains(Modifier.SEALED)
            }.filter { p ->
                p.hasAnnotation(Serializable::class) ||
                        p.hasAnnotation(GenerateImmutable::class)
            }.map { p ->
                p to c
            }
        }

    logger.info("${originalSerializablePairs.size} original serializable pair")

    // only register the relation if both the child and the parent are serializable,
    // i.e., with Serializable notation or with GenerateImmutable notation
    val mutableSerializablePairs: List<Pair<KSClassDeclaration, KSClassDeclaration>> =
        immutableDeclarations.filter {
            // sealed class cannot be a subclass in the polymorphic serializer
            !it.modifiers.contains(Modifier.SEALED)
        }.flatMap { c ->
            c.getAllSuperTypes().map { s ->
                s.declaration
            }.filterIsInstance<KSClassDeclaration>().filter { p ->
                !p.modifiers.contains(Modifier.SEALED)
            }.filter { p ->
                p.hasAnnotation(Serializable::class) ||
                        p.hasAnnotation(GenerateImmutable::class)
            }.map { p ->
                p to c
            }
        }

    logger.info("${mutableSerializablePairs.size} mutable serializable pair")

    return FileSpec.builder(
        packageName = packageName,
        fileName = "GeneratedModule"
    ).apply {
        val builder = TypeSpec.objectBuilder("GeneratedModule").apply {
            indent("    ")

            val moduleBuilder = PropertySpec.builder(
                "serializersModule",
                SerializersModule::class
            ).apply {
                initializer(buildCodeBlock {
                    val polymorphic = MemberName(
                        "kotlinx.serialization.modules",
                        "polymorphic"
                    )
                    val subClass = MemberName(
                        "kotlinx.serialization.modules",
                        "subclass"
                    )

                    // Group serializable class by parent
                    val originalSerializableMap: Map<KSClassDeclaration, List<KSClassDeclaration>> =
                        originalSerializablePairs.groupBy {
                            it.first
                        }.mapValues { (_, v) ->
                            v.map { it.second }
                        }

                    val mutableSerializableMap: Map<KSClassDeclaration, List<KSClassDeclaration>> =
                        mutableSerializablePairs.groupBy {
                            it.first
                        }.mapValues { (_, v) ->
                            v.map { it.second }
                        }

                    beginControlFlow("SerializersModule")
                    originalSerializableMap.forEach { (parent, childList) ->
                        val parentName = MemberName(
                            parent.packageName.asString(),
                            parent.simpleName.asString()
                        )
                        beginControlFlow("%M(%M::class)", polymorphic, parentName)
                        childList.forEach { child ->
                            val childName = MemberName(
                                child.packageName.asString(),
                                child.simpleName.asString()
                            )
                            addStatement("%M(%M::class)", subClass, childName)
                        }
                        endControlFlow()
                    }

                    // Group serializable class by parent
                    mutableSerializableMap.forEach { (parent, childList) ->
                        val parentName = MemberName(
                            parent.packageName.asString(),
                            parent.simpleName.asString()
                        )
                        beginControlFlow("%M(%M::class)", polymorphic, parentName)
                        childList.forEach { child ->
                            val childName = MemberName(
                                child.packageName.asString(),
                                child.simpleName.asString()
                            )
                            addStatement("%M(%M::class)", subClass, childName)
                        }
                        endControlFlow()

                        // also register the parent-child relation for the immutable counterpart
                        // this function guess that there is a immutable version of the parent
                        // if the class has the GenerateImmutable annotation
                        // or the name start with Mutable
                        val immutableParentName = MemberName(
                            parent.packageName.asString(),
                            if (parent.simpleName.asString().startsWith("Mutable")) {
                                parent.simpleName.asString().drop(7)
                            } else {
                                parent.simpleName.asString()
                            }
                        )
                        beginControlFlow("%M(%M::class)", polymorphic, immutableParentName)
                        childList.forEach { child ->
                            val immutableChildName = MemberName(
                                child.packageName.asString(),
                                child.simpleName.asString().drop(7),
                            )
                            addStatement("%M(%M::class)", subClass, immutableChildName)
                        }
                        endControlFlow()
                    }
                    endControlFlow()
                })
            }

            addProperty(moduleBuilder.build())
        }

        addType(builder.build())

    }.build()
}