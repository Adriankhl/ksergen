package ksergen.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import ksergen.annotations.GenerateImmutable
import kotlin.reflect.KClass

internal fun <T : Annotation> KSDeclaration.hasAnnotation(annotationClass: KClass<T>): Boolean {
    return this.annotations.any {
        it.shortName.getShortName() == annotationClass.simpleName
    }
}

internal fun convertTypeImmutable(
    type: KSType,
    logger: KSPLogger,
): TypeName {
    return if (type.declaration.hasAnnotation(GenerateImmutable::class)) {
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
