package ksergen.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration
import kotlinx.serialization.Serializable
import ksergen.annotations.GenerateImmutable
import kotlin.reflect.KClass

internal class KSerGenSymbolProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        environment.logger.info("Starting KSerGen Processor.")

        val immutableDeclarations: Sequence<KSDeclaration> = resolver.getSymbolsWithAnnotation(
            GenerateImmutable::class.qualifiedName.orEmpty()
        ).filterIsInstance<KSDeclaration>()

        val serializableDeclarations : Sequence<KSDeclaration> = resolver.getSymbolsWithAnnotation(
            Serializable::class.qualifiedName.orEmpty()
        ).filterIsInstance<KSDeclaration>()

        return emptyList()
    }
}

private fun <T : Annotation> KSDeclaration.hasAnnotation(annotationClass: KClass<T>): Boolean {
    return this.annotations.any {
        it.shortName.getShortName() == annotationClass.simpleName
    }
}
