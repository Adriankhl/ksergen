package ksergen.mock.base

import ksergen.annotations.GenerateImmutable

@GenerateImmutable
data class MutableDoubleData(var d1: Double, var d2: Double)