package com.example

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    @Test
    fun testZipSlipPathTraversalBlocked() {
        val destinationDir = File("/tmp/safe_dest_dir")
        val maliciousEntry = "../../etc/passwd"
        val targetFile = File(destinationDir, maliciousEntry)

        val isSafe = targetFile.canonicalPath.startsWith(destinationDir.canonicalPath + File.separator)
        assertEquals("Zip slip traversal attempt should be rejected", false, isSafe)
    }

    @Test
    fun testSafeZipEntryAllowed() {
        val destinationDir = File("/tmp/safe_dest_dir")
        val safeEntry = "content/script.js"
        val targetFile = File(destinationDir, safeEntry)

        val isSafe = targetFile.canonicalPath.startsWith(destinationDir.canonicalPath + File.separator)
        assertEquals("Safe entry inside folder should be allowed", true, isSafe)
    }
}
