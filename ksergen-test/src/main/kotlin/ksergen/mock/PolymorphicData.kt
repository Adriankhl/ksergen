package ksergen.mock

import ksergen.annotations.GenerateImmutable

@GenerateImmutable
sealed class MutablePolymorphicSealData

@GenerateImmutable
data class MutableSimpleMasterData(val s: Int = 9): MutablePolymorphicSealData()

@GenerateImmutable
sealed interface MutableDummyInterface

@GenerateImmutable
data class MutablePolymorphicData(
    val d: MutablePolymorphicSealData = MutableMasterData(),
    val dm: MutableMap<Int, MutablePolymorphicSealData> = mutableMapOf(
        1 to MutableMasterData(),
        2 to MutableSimpleMasterData(),
    )
)