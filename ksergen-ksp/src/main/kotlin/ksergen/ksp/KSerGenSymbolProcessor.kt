package ksergen.ksp

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.Serializable
import ksergen.annotations.GenerateImmutable

internal class KSerGenSymbolProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    private val logger = environment.logger

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // Change the message to force ksp processor to be triggered
        logger.info("2")

        logger.info("Starting KSerGen Processor")

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

        val immutablePackageMap: Map<String, List<KSClassDeclaration>> =
            immutableDeclarations.groupBy {
                it.packageName.asString()
            }

        immutablePackageMap.forEach { (packageName, dList) ->
            val immutableFileMap: Map<String, List<KSClassDeclaration>> = dList.groupBy {
                // Drop .Kt from the file name
                it.containingFile!!.fileName.dropLast(3)
            }

            immutableFileMap.forEach { (fileName, declarationList) ->
                val fileSpec: FileSpec = generateImmutableFile(
                    packageName,
                    fileName,
                    declarationList,
                    logger,
                )

                fileSpec.writeTo(environment.codeGenerator, false)
            }
        }

        val serializableDeclarations: List<KSClassDeclaration> = resolver
            .getSymbolsWithAnnotation(Serializable::class.qualifiedName.orEmpty())
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        logger.info("${serializableDeclarations.size} Serializable annotation")

        val serializablePairs: List<Pair<KSClassDeclaration, KSClassDeclaration>> =
            serializableDeclarations.flatMap { c ->
                c.getAllSuperTypes().map { s ->
                    s.declaration
                }.filterIsInstance<KSClassDeclaration>().filter { p ->
                    p.getSealedSubclasses().none()
                }.filter { p ->
                    p.hasAnnotation(Serializable::class)
                }.map { p ->
                    p to c
                }
            }

        logger.info("${serializablePairs.size} serializable pair")

        return emptyList()
    }
}

