package ksergen.mock

import ksergen.annotations.GenerateImmutable
import ksergen.mock.base.MutableDoubleData
import ksergen.mock.base.MutableIntData

@GenerateImmutable
data class MutableMasterData(
    var dataName: String = "Hello",
    var id: MutableIntData = MutableIntData(1, 2),
    var td: MutableDoubleData = MutableDoubleData(1.3, 6.7),
    val intList: MutableList<MutableIntData> = mutableListOf(
        MutableIntData(15, 24),
        MutableIntData(-1, -4)
    ),
    val doubleSet: MutableSet<MutableDoubleData> = mutableSetOf(
        MutableDoubleData(0.2, 0.4),
        MutableDoubleData(9.5, 123.5)
    ),
    var doubleMap: MutableMap<Int, MutableDoubleData> = mutableMapOf(
        4 to MutableDoubleData(3.4, 5.6),
        900 to MutableDoubleData(3.5, 7.8)
    ),
    val nestedMap: MutableMap<Int, MutableMap<Int, MutableDoubleData>> = mutableMapOf(
        1 to mutableMapOf(
            345 to MutableDoubleData(2.3, 40.0),
            98 to MutableDoubleData(345.2, 45891.2)
        ),
        8 to mutableMapOf(
            34235 to MutableDoubleData(22342.3, 423420.0),
            92348 to MutableDoubleData(344565.2, 4582391.2)
        )
    ),
    val emptyMap: MutableMap<Int, MutableIntData> = mutableMapOf(),
)