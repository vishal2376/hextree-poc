package com.vishal2376.hextreepoc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DeeplinkActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Utils.dumpIntent(this, intent, "Deeplink")
	}
}