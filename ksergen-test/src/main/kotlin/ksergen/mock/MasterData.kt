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
)