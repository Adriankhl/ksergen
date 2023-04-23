package ksergen.mock

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import ksergen.testing.dependency.MutableSerializableParentData
import ksergen.testing.dependency.SerializableParentData

object SerializationModule {
    val module = SerializersModule {
        polymorphic(MutableSerializableParentData::class) {
            subclass(MutableExternalMasterData::class)
        }
        polymorphic(SerializableParentData::class) {
            subclass(ExternalMasterData::class)
        }
    }
}