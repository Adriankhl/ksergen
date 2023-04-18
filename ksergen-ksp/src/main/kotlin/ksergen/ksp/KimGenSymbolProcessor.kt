package ksergen.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSDeclaration
import ksergen.annotations.GenerateImmutable
import kotlin.reflect.KClass

internal class KimGenSymbolProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val generateDeclaration: Sequence<KSDeclaration> = resolver.getAllFiles().flatMap {
            it.declarations
        }.filter { declaration ->
            declaration.hasAnnotation(GenerateImmutable::class)
        }
        return emptyList()
    }
}

private fun <T : Annotation> KSDeclaration.hasAnnotation(annotationClass: KClass<T>): Boolean {
    return this.annotations.any {
        it.shortName.getShortName() == annotationClass.simpleName
    }
}
