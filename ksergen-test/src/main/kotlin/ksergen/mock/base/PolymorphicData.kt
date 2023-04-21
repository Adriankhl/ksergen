package ksergen.mock.base

import ksergen.annotations.GenerateImmutable

@GenerateImmutable
sealed class MutablePolymorphicSealData

@GenerateImmutable
data class MutableSimpleMasterData(val s: Int = 9)