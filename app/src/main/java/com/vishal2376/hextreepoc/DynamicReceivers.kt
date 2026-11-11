package com.vishal2376.hextreepoc

/**
 * Author: Vishal Singh (vishal2376)
 */


import android.annotation.SuppressLint
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

//	private val flag18Receiver = object : BroadcastReceiver() {
//		override fun onReceive(context: Context, intent: Intent) {
//			Log.d(TAG, "[Flag18] action=${intent.action} extras=${intent.extras}")
//		}
//	}
//
//	private val flag19Receiver = object : BroadcastReceiver() {
//		override fun onReceive(context: Context, intent: Intent) {
//			Log.d(TAG, "[Flag19] action=${intent.action} extras=${intent.extras}")
//		}
//	}

	// Pair each receiver with the action(s) it should listen for
	private fun all(): List<Pair<BroadcastReceiver, IntentFilter>> = listOf(
//		flag18Receiver to IntentFilter("io.hextree.broadcast.FREE_FLAG"),
//		flag19Receiver to IntentFilter("io.hextree.broadcast.ANOTHER_ACTION"),
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