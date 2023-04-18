package ksergen.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import kotlinx.serialization.Serializable
import ksergen.annotations.GenerateImmutable
import kotlin.reflect.KClass

internal class KSerGenSymbolProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Starting KSerGen Processor.".repeat(2))

        val immutableDeclarations: List<KSClassDeclaration> = resolver
            .getSymbolsWithAnnotation(GenerateImmutable::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>().toList()
            .filter {
                val className: String = it.simpleName.getShortName()
                val isNameCorrect = className.startsWith("Mutable")
                if (!isNameCorrect) {
                    logger.error(
                        "$className class name does not start with Mutable"
                    )
                }

                isNameCorrect
            }.toList()

        logger.info("${immutableDeclarations.size} GenerateImmutable annotation")

        val serializableDeclarations : List<KSClassDeclaration> = resolver
            .getSymbolsWithAnnotation(Serializable::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        logger.info("${serializableDeclarations.size} Serializable annotation")

        return emptyList()
    }
}

private fun <T : Annotation> KSDeclaration.hasAnnotation(annotationClass: KClass<T>): Boolean {
    return this.annotations.any {
        it.shortName.getShortName() == annotationClass.simpleName
    }
}
