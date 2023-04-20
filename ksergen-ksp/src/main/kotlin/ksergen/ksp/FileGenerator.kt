package ksergen.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
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
                TypeSpec.classBuilder(declaration.simpleName.getShortName().drop(7))
                    .addModifiers(KModifier.DATA)
                    .addAnnotation(Serializable::class)
                    .primaryConstructor(
                        FunSpec.constructorBuilder().apply {
                            declaration.primaryConstructor!!.parameters.forEach { parameter ->
                               addParameter(
                                   name = parameter.name!!.getShortName(),
                                   type = convertTypeImmutable(parameter.type.resolve(), logger)
                               )
                            }
                        }.build()
                    ).apply {
                        declaration.primaryConstructor!!.parameters.forEach { parameter ->
                            addProperty(
                                PropertySpec.builder(
                                    parameter.name!!.getShortName(),
                                    convertTypeImmutable(parameter.type.resolve(), logger),
                                ).initializer(parameter.name!!.getShortName()).build()
                            )
                        }
                    }
                    .build()
            )
        }
    }.build()
}