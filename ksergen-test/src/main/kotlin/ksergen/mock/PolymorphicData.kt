package ksergen.mock

import ksergen.annotations.GenerateImmutable

@GenerateImmutable
sealed class MutablePolymorphicSealData

@GenerateImmutable
data class MutableSimpleMasterData(val s: Int = 9): MutablePolymorphicSealData()

@GenerateImmutable
sealed interface MutableDummyInterface