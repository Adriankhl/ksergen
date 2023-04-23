package ksergen.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import ksergen.annotations.GenerateImmutable

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
            val builder = if (declaration.classKind == ClassKind.INTERFACE) {
                TypeSpec.interfaceBuilder(declaration.simpleName.getShortName().drop(7))
            } else {
                TypeSpec.classBuilder(declaration.simpleName.getShortName().drop(7)).apply {
                    addAnnotation(Serializable::class)

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
                    ).apply {
                        declaration.primaryConstructor!!.parameters.forEach { parameter ->
                            addProperty(
                                PropertySpec.builder(
                                    parameter.name!!.getShortName(),
                                    convertFullTypeImmutable(parameter.type.resolve(), logger),
                                ).initializer(parameter.name!!.getShortName()).build()
                            )
                        }
                    }
                }
            }

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

fun generateSerializersModuleFile(
    serializableDeclarations: List<KSClassDeclaration>,
    immutableDeclarations: List<KSClassDeclaration>,
    logger: KSPLogger,
): FileSpec {
    val originalSerializablePairs: List<Pair<KSClassDeclaration, KSClassDeclaration>> =
        serializableDeclarations.flatMap { c ->
            c.getAllSuperTypes().map { s ->
                s.declaration
            }.filterIsInstance<KSClassDeclaration>().filter { p ->
                p.getSealedSubclasses().none()
            }.filter { p ->
                p.hasAnnotation(Serializable::class) ||
                        p.hasAnnotation(GenerateImmutable::class)
            }.map { p ->
                p to c
            }
        }

    logger.info("${originalSerializablePairs.size} original serializable pair")

    val mutableSerializablePairs: List<Pair<KSClassDeclaration, KSClassDeclaration>> =
        immutableDeclarations.flatMap { c ->
            c.getAllSuperTypes().map { s ->
                s.declaration
            }.filterIsInstance<KSClassDeclaration>().filter { p ->
                p.getSealedSubclasses().none()
            }.filter { p ->
                p.hasAnnotation(Serializable::class) ||
                        p.hasAnnotation(GenerateImmutable::class)
            }.map { p ->
                p to c
            }
        }

    logger.info("${mutableSerializablePairs.size} mutable serializable pair")

    return FileSpec.builder(
        packageName = "ksergen.serializers.module",
        fileName = "GeneratedModule"
    ).apply {
        val builder = TypeSpec.objectBuilder("GeneratedModule").apply {
            indent("    ")
            val moduleBuilder = PropertySpec.builder(
                "module",
                SerializersModule::class
            ).apply {
                initializer(buildCodeBlock {
                    beginControlFlow("SerializersModule")
                    addStatement("1")
                    endControlFlow()
                })
            }

            addProperty(moduleBuilder.build())
        }

        addType(builder.build())

    }.build()
}