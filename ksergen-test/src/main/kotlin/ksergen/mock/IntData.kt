package ksergen.mock

import kotlinx.serialization.Serializable
import ksergen.annotations.GenerateImmutable

@GenerateImmutable
data class MutableIntData(var a: Int)

@Serializable
data class SerIntData(val a: Int)