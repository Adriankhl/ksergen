package ksergen.mock

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

internal class MutableMasterDataTest {
    @Test
    fun serializationTest() {
        val format = Json {
            encodeDefaults = true
        }

        val a = MutableMasterData()
        val b: String = format.encodeToString(a)
        val c: MasterData = format.decodeFromString(b)
        val d: String = format.encodeToString(c)
        val e: MutableMasterData = format.decodeFromString(d)

        assertEquals(a, e)
    }
}