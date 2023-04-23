package ksergen.annotations

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MetaSerializable

/**
 * Generate a serializable immutable copy of this class.
 * This class is also automatically serializable.
 * Can't set Retention to source since it breaks serialization
 */
@OptIn(ExperimentalSerializationApi::class)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@MetaSerializable
annotation class GenerateImmutable
