package com.vishal2376.hextreepoc

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vishal2376.hextreepoc.ui.theme.HextreePoCTheme

class HextreeActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		// Observe incoming intents
		Utils.showDialog(this, intent)

		setContent {
			HextreePoCTheme {
				MainScreen(this)
			}
		}
	}

	/**
	 * Result Handler — Intercepts data returned by target activities
	 * Technique: onActivityResult Data Capture
	 * Called automatically when target activity calls setResult() + finish().
	 * requestCode matches what we passed in startActivityForResult() to identify which flag.
	 * data contains the intent with flag/sensitive info sent back by target activity.
	 * We display the returned intent extras using Utils.showDialog() to reveal the flag.
	 */
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		// Flag8 result — display returned intent data
		if (requestCode == 8) {
			Utils.showDialog(this, data)
		}

		// Flag9 result — capture and display stolen flag from setResult()
		if (requestCode == 9) {
			Utils.showDialog(this, data)
		}
	}
}

data class Flag(val name: String, val action: () -> Unit)

@Composable
fun MainScreen(activity: HextreeActivity) {
	val context = LocalContext.current
	val attack by remember { mutableStateOf(Attack(context, activity)) }

	val flags = remember(attack) {
		listOf(
			Flag("Flag 1 - Basic Activity") { attack.flag1() },
			Flag("Flag 2 - Intent with Extras") { attack.flag2() },
			Flag("Flag 3 - Data URI") { attack.flag3() },
			Flag("Flag 4 - State Machine") { attack.flag4() },
			Flag("Flag 5 - Intent in intent") { attack.flag5() },
			Flag("Flag 6 - Not Exported") {attack.flag6() },
			Flag("Flag 7 - Activity Lifecycle") { attack.flag7() },
			Flag("Flag 8 - Activity Result") { attack.flag8() },
			Flag("Flag 9 - Activity Result Flag") { attack.flag9() },
			Flag("Flag 12 - Implicit Intent") { attack.flag12() },
		)
	}

	Scaffold(
		modifier = Modifier.fillMaxSize()
	) { innerPadding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black)
				.padding(innerPadding),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
		) {
			items(flags) { flag ->
				Button(onClick = flag.action) {
					Text(flag.name)
				}
			}
		}
	}
}