package ksergen.mock.base

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ksergen.serializers.module.GeneratedModule
import kotlin.test.Test
import kotlin.test.assertEquals


internal class MutableIntDataTest {
    @Test
    fun sumTest() {
        val format = Json {
            encodeDefaults = true
            serializersModule = GeneratedModule.serializersModule
        }

        val mid = MutableIntData()
        val id: IntData = format.decodeFromString(format.encodeToString(mid))


        val s1 = mid.sum()
        val s2 = id.sum()

        assertEquals(s1, s2)
    }
}