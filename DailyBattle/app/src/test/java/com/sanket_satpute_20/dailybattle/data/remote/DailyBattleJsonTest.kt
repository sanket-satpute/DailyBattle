package com.sanket_satpute_20.dailybattle.data.remote

import com.sanket_satpute_20.dailybattle.data.remote.serialization.DailyBattleJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@Serializable
private data class SampleDto(
    val id: String,
    val score: Int,
    val active: Boolean = true,
)

class DailyBattleJsonTest {

    @Test
    fun roundtripSerializationPreservesData() {
        val original = SampleDto(id = "item-1", score = 95, active = false)
        val jsonString = DailyBattleJson.encodeToString(SampleDto.serializer(), original)
        val decoded = DailyBattleJson.decodeFromString(SampleDto.serializer(), jsonString)

        assertEquals(original, decoded)
    }

    @Test
    fun ignoresUnknownKeysSafelyForFutureSchemaEvolution() {
        val jsonWithExtraFields = """
            {
                "id": "item-2",
                "score": 100,
                "active": true,
                "unexpectedField": "future_version_data",
                "nestedExtra": { "foo": "bar" }
            }
        """.trimIndent()

        val decoded = DailyBattleJson.decodeFromString(SampleDto.serializer(), jsonWithExtraFields)

        assertEquals("item-2", decoded.id)
        assertEquals(100, decoded.score)
        assertEquals(true, decoded.active)
    }

    @Test(expected = SerializationException::class)
    fun throwsSerializationExceptionOnMalformedPayload() {
        val malformedJson = "{ id: missing_quotes, score: non_number }"
        DailyBattleJson.decodeFromString(SampleDto.serializer(), malformedJson)
    }
}
