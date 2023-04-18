package ksergen.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MetaSerializable

/**
 * Generate a serializable immutable copy of this class.
 * This class is also automatically serializable.
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@MetaSerializable
annotation class GenerateImmutable

/**
 * This is a pure function belongs in a class annotated with [ksergen.annotations.GenerateImmutable],
 * so this function will be copied to the generated immutable data class.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class GenerateFunction