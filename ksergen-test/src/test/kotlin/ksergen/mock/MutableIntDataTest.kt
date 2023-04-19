package ksergen.mock

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MutableIntDataTest {
    @Test
    fun serializationTest() {
        val format = Json {
            encodeDefaults = true
        }

        val o = IntData(1)

        val a = MutableIntData(1)
        val b: String = format.encodeToString(a)
        val c: IntData = format.decodeFromString(b)

        assertEquals(o, c)
    }
}