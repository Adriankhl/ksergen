package ksergen.mock

import ksergen.annotations.GenerateImmutable

@GenerateImmutable
data class MutableIntData(var a: Int)