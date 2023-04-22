package ksergen.mock

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import ksergen.testing.dependency.MutableSerializableParentData

object SerializationModule {
    val module = SerializersModule {
        polymorphic(MutableSerializableParentData::class) {
            subclass(MutableExternalMasterData::class)
        }
    }
}