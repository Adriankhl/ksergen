package ksergen.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName

fun generateImmutableFile(
    packageName: String,
    fileName: String,
    declarationList: List<KSClassDeclaration>,
): FileSpec {
    return FileSpec.builder(
        packageName = packageName,
        fileName = fileName,
    ).apply {
        declarationList.forEach { declaration ->
            addType(
                TypeSpec.classBuilder(declaration.simpleName.getShortName().drop(7))
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(
                        FunSpec.constructorBuilder().apply {
                            declaration.primaryConstructor!!.parameters.forEach { parameter ->
                               addParameter(
                                   name = parameter.name!!.getShortName(),
                                   type = parameter.type.toTypeName()
                               )
                            }
                        }.build()
                    ).apply {
                        declaration.primaryConstructor!!.parameters.forEach { parameter ->
                            addProperty(
                                PropertySpec.builder(
                                    parameter.name!!.getShortName(),
                                    parameter.type.toTypeName()
                                ).initializer(parameter.name!!.getShortName()).build()
                            )
                        }
                    }
                    .build()
            )
        }
    }.build()
}