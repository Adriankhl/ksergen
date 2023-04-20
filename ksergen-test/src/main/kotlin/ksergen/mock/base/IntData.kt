package ksergen.mock.base

import kotlinx.serialization.Serializable
import ksergen.annotations.GenerateImmutable

@GenerateImmutable
data class MutableIntData(var i1: Int, var i2: Int)

@Serializable
data class SerIntData(val a: Int)