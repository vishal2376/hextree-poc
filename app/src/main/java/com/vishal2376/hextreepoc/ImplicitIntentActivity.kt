package com.vishal2376.hextreepoc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ImplicitIntentActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Utils.showDialog(this, intent)

		// Flag11 : Sending Custom Intent to Caller App
		val resultIntent = Intent().apply {
			putExtra("token", 1094795585)
		}

		setResult(RESULT_OK,resultIntent)
		finish()
	}
}