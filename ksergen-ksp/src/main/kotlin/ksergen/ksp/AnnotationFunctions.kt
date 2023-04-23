package ksergen.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import ksergen.annotations.GenerateImmutable
import kotlin.reflect.KClass

/**
 * Whether a class has a specific annotation
 */
internal fun <T : Annotation> KSDeclaration.hasAnnotation(annotationClass: KClass<T>): Boolean {
    return this.annotations.any {
        it.shortName.getShortName() == annotationClass.simpleName
    }
}

/**
 * Whether a class is a mutable collection in the standard kotlin library
 */
internal fun isMutableCollection(declaration: KSDeclaration): Boolean {
    val mutableCollectionName: Set<String> = setOf(
        "MutableList",
        "MutableMap",
        "MutableSet",
    )

    return mutableCollectionName.contains(declaration.simpleName.getShortName())
}

/**
 * Convert a mutable type to immutable, use recursion to deal with generic type.
 */
internal fun convertFullTypeImmutable(
    type: KSType,
    logger: KSPLogger,
): TypeName {
    logger.info("Type name: ${type.toTypeName()}")

    logger.info("Type arguments: ${type.arguments}")

    return if (type.arguments.isNotEmpty()) {
        convertGenericTypeImmutable(type, logger)
    } else {
        convertNonGenericImmutable(type, logger)
    }
}

/**
 * Convert a mutable type with generic argument to immutable
 */
internal fun convertGenericTypeImmutable(
    type: KSType,
    logger: KSPLogger,
): TypeName {
    logger.info("Start converting generic type")

    val shouldConvertType: Boolean = type.declaration.hasAnnotation(GenerateImmutable::class)
            || isMutableCollection(type.declaration)

    return if (shouldConvertType) {
        val className: ClassName = type.toClassName()

        val newParametrizedTypeName: ParameterizedTypeName = className.peerClass(
            className.simpleName.drop(7)
        ).copy(
            className.isNullable,
            className.annotations,
            className.tags,
        ).parameterizedBy(
            type.arguments.map {
                convertFullTypeImmutable(it.type!!.resolve(), logger)
            }
        )

        logger.info("Convert generic $className to $newParametrizedTypeName")

        newParametrizedTypeName
    } else {
        type.toTypeName()
    }
}

/**
 * Convert a simple non-generic type to immutable
 */
internal fun convertNonGenericImmutable(
    type: KSType,
    logger: KSPLogger,
): TypeName {
    logger.info("Start converting non-generic type")

    val shouldConvertType: Boolean = type.declaration.hasAnnotation(GenerateImmutable::class) ||
            type.declaration.simpleName.asString().startsWith("Mutable")

    return if (shouldConvertType) {
        val className: ClassName = type.toClassName()

        val newClassName: ClassName = className.peerClass(
            className.simpleName.drop(7)
        ).copy(
            className.isNullable,
            className.annotations,
            className.tags,
        )

        logger.info("Convert $className to $newClassName")

        newClassName
    } else {
        type.toTypeName()
    }
}