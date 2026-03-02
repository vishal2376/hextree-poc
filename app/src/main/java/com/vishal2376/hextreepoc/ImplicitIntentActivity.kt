package com.vishal2376.hextreepoc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ImplicitIntentActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		Utils.showDialog(this, intent)

		setResult(RESULT_OK)
		finish()
	}
}