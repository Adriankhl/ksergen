package ksergen.mock.base

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ksergen.serializers.module.GeneratedModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MutableDemoDataTest {
    @Test
    fun serializationTest() {
        val format = Json {
            encodeDefaults = true
            serializersModule = GeneratedModule.serializersModule
        }

       val a = MutableDemoData()
       val b: String = format.encodeToString(a)
       val c: DemoData = format.decodeFromString(b)
       val d: String = format.encodeToString(c)
       val e: MutableDemoData = format.decodeFromString(d)

       assertEquals(a, e)
    }
}