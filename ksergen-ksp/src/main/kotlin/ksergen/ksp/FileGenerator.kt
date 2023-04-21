package ksergen.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlinx.serialization.Serializable

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
        declarationList.forEach { declaration ->
            addType(
                if (declaration.primaryConstructor != null) {
                    TypeSpec.classBuilder(declaration.simpleName.getShortName().drop(7)).apply {
                        addModifiers(
                            declaration.modifiers.mapNotNull { it.toKModifier() }
                        )

                        addAnnotation(Serializable::class)

                        declaration.superTypes.map {
                            it.resolve()
                        }.forEach {
                            val superDeclaration: KSDeclaration = it.declaration
                            if (superDeclaration is KSClassDeclaration) {
                                if (superDeclaration.primaryConstructor == null) {
                                    addSuperinterface(convertFullTypeImmutable(it, logger))
                                } else {
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
                    }.build()
                } else {
                    TypeSpec.interfaceBuilder(
                        declaration.simpleName.getShortName().drop(7)
                    ).apply {
                        addModifiers(
                            declaration.modifiers.mapNotNull { it.toKModifier() }
                        )

                        addAnnotation(Serializable::class)
                    }.build()
                }
            )
        }
    }.build()
}