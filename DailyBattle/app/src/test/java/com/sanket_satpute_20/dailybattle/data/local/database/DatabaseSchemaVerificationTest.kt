package com.sanket_satpute_20.dailybattle.data.local.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DatabaseSchemaVerificationTest {

    @Test
    fun exportedSchemaFileForVersionOneExists() {
        val schemaFile = findSchemaFile(version = 1)
        assertNotNull("Room schema file for Version 1 must exist", schemaFile)
        assertTrue("Schema file must exist on disk", schemaFile!!.exists())
        assertTrue("Schema file must not be empty", schemaFile.length() > 0)
    }

    @Test
    fun exportedSchemaDeclaresVersionOneAndAll12Entities() {
        val schemaFile = findSchemaFile(version = 1)
        assertNotNull("Room schema file for Version 1 must exist", schemaFile)
        val content = schemaFile!!.readText()

        // Verify version declaration
        assertTrue("Schema JSON must declare version 1", content.contains("\"version\": 1"))

        // Verify all 12 database tables exist in the exported schema
        val requiredTables = listOf(
            "users",
            "battles",
            "challenges",
            "battle_sessions",
            "challenge_sessions",
            "challenge_results",
            "battle_results",
            "friends",
            "rivals",
            "rival_match_history",
            "profiles",
            "battle_history_items",
        )

        for (tableName in requiredTables) {
            assertTrue(
                "Exported Room schema must contain table '$tableName'",
                content.contains("\"tableName\": \"$tableName\""),
            )
        }
    }

    @Test
    fun exportedSchemaContainsRequiredColumnsForCoreEntities() {
        val schemaFile = findSchemaFile(version = 1)
        assertNotNull("Room schema file for Version 1 must exist", schemaFile)
        val content = schemaFile!!.readText()

        // Verify representative key columns for data integrity
        assertTrue(content.contains("\"columnName\": \"userId\""))
        assertTrue(content.contains("\"columnName\": \"battleId\""))
        assertTrue(content.contains("\"columnName\": \"sessionId\""))
        assertTrue(content.contains("\"columnName\": \"challengeId\""))
        assertTrue(content.contains("\"columnName\": \"rivalUserId\""))
    }

    private fun findSchemaFile(version: Int): File? {
        val candidates = listOf(
            File("schemas/com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase/$version.json"),
            File("app/schemas/com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase/$version.json"),
            File("../app/schemas/com.sanket_satpute_20.dailybattle.data.local.database.DailyBattleDatabase/$version.json"),
        )
        return candidates.find { it.exists() }
    }
}
