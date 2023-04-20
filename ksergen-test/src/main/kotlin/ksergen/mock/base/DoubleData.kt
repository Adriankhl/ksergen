package ksergen.mock.base

import kotlinx.serialization.Serializable
import ksergen.annotations.GenerateImmutable

@GenerateImmutable
data class MutableDoubleData(var d1: Double, var d2: Double)