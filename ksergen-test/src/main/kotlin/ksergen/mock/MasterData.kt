package ksergen.mock

import ksergen.annotations.GenerateImmutable
import ksergen.mock.base.MutableDoubleData
import ksergen.mock.base.MutableIntData

@GenerateImmutable
data class MutableMasterData(
    var dataName: String,
    var id: MutableIntData,
    var td: MutableDoubleData,
    val intList: MutableList<MutableIntData>,
    val doubleSet: MutableSet<MutableDoubleData>,
    var doubleMap: MutableMap<Int, MutableDoubleData>,
    val nestedMap: MutableMap<Int, MutableMap<Int, MutableDoubleData>>,
    val weirdCollection: MutableMap<MutableIntData, MutableMap<Int, MutableMap<String, MutableDoubleData>>>,
)

//val intList: MutableList<MutableIntData>,
//val doubleSet: MutableSet<MutableDoubleData>,
//var doubleMap: MutableMap<Int, MutableDoubleData>,
//val weirdCollection: MutableMap<MutableIntData, MutableMap<Int, MutableMap<String, MutableDoubleData>>>,
