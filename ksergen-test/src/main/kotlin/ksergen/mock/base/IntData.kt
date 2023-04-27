package ksergen.mock.base

import kotlinx.serialization.SerialName
import ksergen.annotations.GenerateImmutable

@GenerateImmutable
data class MutableIntData(var i1: Int = 1, var i2: Int = 2)

@GenerateImmutable
@SerialName("Demo")
data class MutableDemoData(
    var id: MutableIntData = MutableIntData(),
    var il: MutableList<Int> = mutableListOf(1, 2),
    var idl: MutableList<MutableIntData> = mutableListOf(MutableIntData()),
)


fun IntData.sum(): Int = i1 + i2
fun MutableIntData.sum(): Int = i1 + i2