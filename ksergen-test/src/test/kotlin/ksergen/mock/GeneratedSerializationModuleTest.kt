package ksergen.mock

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ksergen.GeneratedModule
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GeneratedSerializationModuleTest {
    @Test
    fun serializationTest() {
        val format = Json {
            encodeDefaults = true
            serializersModule = GeneratedModule.serializersModule
        }

        val a = MutableExternalPolymorphicData()
        val b: String = format.encodeToString(a)
        val c: ExternalPolymorphicData = format.decodeFromString(b)
        val d: String = format.encodeToString(c)
        val e: MutableExternalPolymorphicData = format.decodeFromString(d)

        assertEquals(a, e)
    }
}