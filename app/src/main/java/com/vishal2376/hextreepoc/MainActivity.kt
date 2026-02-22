package com.vishal2376.hextreepoc

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

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		// Observe incoming intents
		Utils.showDialog(this, intent)

		setContent {
			HextreePoCTheme {
				MainScreen()
			}
		}
	}
}

data class Flag(val name: String, val action: () -> Unit)

@Composable
fun MainScreen() {
	val context = LocalContext.current
	val attack by remember { mutableStateOf(Attack(context)) }

	val flags = remember(attack) {
		listOf(
			Flag("Flag 1 - Basic Activity") { attack.flag1() },
			Flag("Flag 2 - Intent with Extras") { attack.flag2() },
			Flag("Flag 3 - Data URI") { attack.flag3() },
			Flag("Flag 4 - State Machine") { attack.flag4() },
			Flag("Flag 5 - Intent in intent") { attack.flag5() },
			Flag("Flag 6") { /* TODO */ },
			Flag("Flag 7") { /* TODO */ },
			Flag("Flag 8") { /* TODO */ },
			Flag("Flag 9") { /* TODO */ },
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