package ksergen.mock

import kotlinx.serialization.SerialName
import ksergen.annotations.GenerateImmutable
import ksergen.testing.dependency.MutableSerializableParentData

@GenerateImmutable
sealed class MutablePolymorphicSealData

@GenerateImmutable
data class MutableSimpleMasterData(val s: Int = 9): MutablePolymorphicSealData()

@GenerateImmutable
sealed interface MutableDummyInterface

@GenerateImmutable
@SerialName("PolymorphicData")
data class MutablePolymorphicData(
    val d: MutablePolymorphicSealData = MutableMasterData(),
    val dm: MutableMap<Int, MutablePolymorphicSealData> = mutableMapOf(
        1 to MutableMasterData(),
        2 to MutableSimpleMasterData(),
    )
)

@GenerateImmutable
data class MutableExternalPolymorphicData(
    val d: MutableSerializableParentData = MutableExternalMasterData(),
    val dm: MutableMap<Int, MutableSerializableParentData> = mutableMapOf(
        1 to MutableExternalMasterData(),
    )
)
