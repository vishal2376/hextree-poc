package com.vishal2376.hextreepoc

/**
 * Author: Vishal Singh (vishal2376)
 */

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import java.io.FileOutputStream

object Utils {

	private const val TAG = "@@@"

	fun dumpIntent(context: Context, intent: Intent?, title: String) {
		Log.d(TAG, "----- $title -----  \n${dumpIntent(context, intent)}")
	}

	fun dumpIntent(context: Context, intent: Intent?): String = dumpIntent(context, intent, 0)

	private fun dumpIntent(context: Context, intent: Intent?, indentLevel: Int): String {
		if (intent == null) return "Intent is null"

		val sb = StringBuilder()
		val indent = "    ".repeat(indentLevel)

		sb.append("$indent[Action]    ${intent.action}\n")
		intent.categories?.forEach { sb.append("$indent[Category]  $it\n") }
		sb.append("$indent[Data]      ${intent.dataString}\n")
		sb.append("$indent[Component] ${intent.component}\n")
		sb.append("$indent[Flags]     ${getFlagsString(intent.flags)}\n")

		intent.extras?.let { extras ->
			for (key in extras.keySet()) {
				when (val value = extras.get(key)) {
					is Intent -> {
						sb.append("$indent[Extra:'$key'] -> Intent\n")
						sb.append(dumpIntent(context, value, indentLevel + 1))
					}

					is Bundle -> {
						sb.append("$indent[Extra:'$key'] -> Bundle\n")
						sb.append(dumpBundle(value, indentLevel + 1))
					}

					else -> sb.append("$indent[Extra:'$key']: $value\n")
				}
			}
		}

		return sb.toString()
	}

	fun dumpBundle(bundle: Bundle?): String = dumpBundle(bundle, 0)

	private fun dumpBundle(bundle: Bundle?, indentLevel: Int): String {
		if (bundle == null) return "Bundle is null"

		val sb = StringBuilder()
		val indent = "    ".repeat(indentLevel)

		for (key in bundle.keySet()) {
			when (val value = bundle.get(key)) {
				is Bundle -> sb.append(
					"$indent['$key']: Bundle[\n${
						dumpBundle(
							value,
							indentLevel + 1
						)
					}$indent]\n"
				)

				else -> sb.append("$indent['$key']: ${value?.toString() ?: "null"}\n")
			}
		}
		return sb.toString()
	}

	private fun getFlagsString(flags: Int): String {
		val flagMap = mapOf(
			Intent.FLAG_GRANT_READ_URI_PERMISSION to "GRANT_READ_URI_PERMISSION",
			Intent.FLAG_GRANT_WRITE_URI_PERMISSION to "GRANT_WRITE_URI_PERMISSION",
			Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION to "GRANT_PERSISTABLE_URI_PERMISSION",
			Intent.FLAG_GRANT_PREFIX_URI_PERMISSION to "GRANT_PREFIX_URI_PERMISSION",
			Intent.FLAG_ACTIVITY_NEW_TASK to "ACTIVITY_NEW_TASK",
			Intent.FLAG_ACTIVITY_SINGLE_TOP to "ACTIVITY_SINGLE_TOP",
			Intent.FLAG_ACTIVITY_NO_HISTORY to "ACTIVITY_NO_HISTORY",
			Intent.FLAG_ACTIVITY_CLEAR_TOP to "ACTIVITY_CLEAR_TOP",
			Intent.FLAG_ACTIVITY_FORWARD_RESULT to "ACTIVITY_FORWARD_RESULT",
			Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP to "ACTIVITY_PREVIOUS_IS_TOP",
			Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS to "ACTIVITY_EXCLUDE_FROM_RECENTS",
			Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT to "ACTIVITY_BROUGHT_TO_FRONT",
			Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED to "ACTIVITY_RESET_TASK_IF_NEEDED",
			Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY to "ACTIVITY_LAUNCHED_FROM_HISTORY",
			Intent.FLAG_ACTIVITY_NEW_DOCUMENT to "ACTIVITY_NEW_DOCUMENT",
			Intent.FLAG_ACTIVITY_NO_USER_ACTION to "ACTIVITY_NO_USER_ACTION",
			Intent.FLAG_ACTIVITY_REORDER_TO_FRONT to "ACTIVITY_REORDER_TO_FRONT",
			Intent.FLAG_ACTIVITY_NO_ANIMATION to "ACTIVITY_NO_ANIMATION",
			Intent.FLAG_ACTIVITY_CLEAR_TASK to "ACTIVITY_CLEAR_TASK",
			Intent.FLAG_ACTIVITY_TASK_ON_HOME to "ACTIVITY_TASK_ON_HOME",
			Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS to "ACTIVITY_RETAIN_IN_RECENTS",
			Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT to "ACTIVITY_LAUNCH_ADJACENT",
			Intent.FLAG_ACTIVITY_MULTIPLE_TASK to "ACTIVITY_MULTIPLE_TASK",
			Intent.FLAG_RECEIVER_REGISTERED_ONLY to "RECEIVER_REGISTERED_ONLY",
			Intent.FLAG_RECEIVER_REPLACE_PENDING to "RECEIVER_REPLACE_PENDING",
			Intent.FLAG_RECEIVER_FOREGROUND to "RECEIVER_FOREGROUND",
			Intent.FLAG_RECEIVER_NO_ABORT to "RECEIVER_NO_ABORT",
			Intent.FLAG_RECEIVER_VISIBLE_TO_INSTANT_APPS to "RECEIVER_VISIBLE_TO_INSTANT_APPS"
		)

		return flagMap.entries
			.filter { (flag, _) -> (flags and flag) != 0 }
			.joinToString(" | ") { it.value }
	}

	fun showDialog(context: Context, intent: Intent?) {
		if (intent == null) return

		val dialog = Dialog(context)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.setCancelable(true)

		val layout = LinearLayout(context).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(20, 50, 20, 50)
			setBackgroundColor(0xffefeff5.toInt())
		}

		val title = TextView(context).apply {
			text = "Intent Details: "
			textSize = 16f
			setTextColor(0xff000000.toInt())
			setTypeface(Typeface.DEFAULT, Typeface.BOLD)
			setPadding(0, 0, 0, 40)
			gravity = Gravity.CENTER
			setBackgroundColor(0xffefeff5.toInt())
		}
		layout.addView(title)

		val message = TextView(context).apply {
			text = dumpIntent(context, intent)
			typeface = Typeface.MONOSPACE
			textSize = 12f
			setTextColor(0xff000000.toInt())
			setPadding(0, 0, 0, 30)
			gravity = Gravity.START
			setBackgroundColor(0xffefeff5.toInt())
		}
		layout.addView(message)

		val positiveButton = Button(context).apply {
			text = "OK"
			setTextColor(0xff000000.toInt())
			setOnClickListener { dialog.dismiss() }
		}
		layout.addView(positiveButton)

		dialog.setContentView(layout)

		dialog.window?.let { window ->
			window.setLayout(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
			)
			window.setBackgroundDrawableResource(android.R.color.transparent)
			val wlp = window.attributes
			wlp.gravity = Gravity.BOTTOM
			wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
			window.attributes = wlp
		}

		dialog.show()

		layout.translationY = 2000f
		layout.alpha = 0f

		AnimatorSet().apply {
			playTogether(
				ObjectAnimator.ofFloat(layout, "translationY", 0f),
				ObjectAnimator.ofFloat(layout, "alpha", 1f)
			)
			duration = 300
			startDelay = 100
			start()
		}
	}

	/**
	 * Queries a Content Provider URI and logs all rows to Logcat.
	 */
	fun dumpCursor(
		context: Context,
		uri: String,
		projection: Array<String>? = null,
		selection: String? = null,
		selectionArgs: Array<String>? = null,
		sortOrder: String? = null,
		title: String = "CP",
	) {
		try {
			val cursor = context.contentResolver.query(
				uri.toUri(), projection, selection, selectionArgs, sortOrder
			)

			if (cursor == null) {
				Log.w(TAG, "[$title] cursor = null")
				return
			}

			cursor.use { c ->
				Log.d(
					TAG,
					"[$title] URI=$uri rows=${c.count} cols=${c.columnNames.joinToString()}"
				)
				while (c.moveToNext()) {
					val row = (0 until c.columnCount).joinToString {
						"${c.getColumnName(it)}=${
							c.getString(it)
						}"
					}
					Log.d(TAG, "[$title] Row ${c.position}: $row")
				}
			}

		} catch (e: SecurityException) {
			Log.e(TAG, "[$title] SecurityException: ${e.message}")
		} catch (e: Exception) {
			Log.e(TAG, "[$title] Exception: ${e.message}")
		}
	}

	/**
	 * Reads the content of a file received via a content:// Uri
	 * (e.g. from a FileProvider result intent) and logs it.
	 */
	fun readFileFromUri(context: Context, uri: Uri?, title: String = "FILE") {
		if (uri == null) {
			Log.w(TAG, "[$title] uri = null")
			return
		}

		try {
			val content = context.contentResolver.openInputStream(uri)?.use { input ->
				input.bufferedReader().readText()
			}

			if (content == null) {
				Log.w(TAG, "[$title] uri=$uri InputStream = null")
			} else {
				Log.d(TAG, "[$title] uri=$uri\n$content")
			}
		} catch (e: Exception) {
			Log.e(TAG, "[$title] Exception: ${e.message}")
		}
	}

	/**
	 * Writes content to a file via its content:// Uri using a write-capable
	 * ParcelFileDescriptor (mode "w" — truncates existing content first).
	 * Requires FLAG_GRANT_WRITE_URI_PERMISSION on the given Uri.
	 */
	fun writeFileToUri(context: Context, uri: Uri?, content: String, title: String = "FILE") {
		if (uri == null) {
			Log.w(TAG, "[$title] uri = null")
			return
		}

		try {
			context.contentResolver.openFileDescriptor(uri, "w")?.use { pfd ->
				FileOutputStream(pfd.fileDescriptor).use { out ->
					out.write(content.toByteArray())
				}
			} ?: Log.w(TAG, "[$title] uri=$uri ParcelFileDescriptor = null")

			Log.d(TAG, "[$title] uri=$uri wrote ${content.length} chars")
		} catch (e: Exception) {
			Log.e(TAG, "[$title] Exception: ${e.message}")
		}
	}
}