package ksergen

import ksergen.annotations.GenerateImmutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

@GenerateImmutable
@SerialName("HelloData")
data class TestData(val num: Int)

@GenerateImmutable
data class BigTestData(val td: TestData)

internal class GenerateImmutableTest {
    @Test
    fun serializationTest() {
        val format = Json {
            encodeDefaults = true
        }

        val a = TestData(1)
        val b = BigTestData(a)

        val c: BigTestData = format.decodeFromString(format.encodeToString(b))

        assert(b == c)

        assert(TestData.serializer().descriptor.serialName == "HelloData")
    }
}