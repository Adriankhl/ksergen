package ksergen.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.serialization.Serializable
import ksergen.annotations.GenerateImmutable

/**
 * Generate codes for GenerateImmutable annotation and Serializable annotation
 */
internal class KSerGenSymbolProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    private val logger = environment.logger

    // Whether this is the first process
    // Prevent processing generated files
    private var isFirst = true

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isFirst) {
            isFirst = false

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

            val serializableDeclarations: List<KSClassDeclaration> = resolver
                .getSymbolsWithAnnotation(Serializable::class.qualifiedName.orEmpty())
                .filterIsInstance<KSClassDeclaration>()
                .toList()

            logger.info("${serializableDeclarations.size} Serializable annotation")

            // Group classes by package and then by file name
            // Generate one file for each package + file name
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

            // Generate a file for SerializersModule
            val serializersModuleFileSpec: FileSpec = generateSerializersModuleFile(
                serializableDeclarations, immutableDeclarations, logger
            )

            serializersModuleFileSpec.writeTo(environment.codeGenerator, false)
        }

        return emptyList()
    }
}

