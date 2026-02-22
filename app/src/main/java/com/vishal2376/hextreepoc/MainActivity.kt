package com.vishal2376.hextreepoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
		setContent {
			HextreePoCTheme {
				MainScreen()
			}
		}
	}
}

@Composable
fun MainScreen() {
	val context = LocalContext.current
	val attack by remember { mutableStateOf(Attack(context)) }

	Scaffold(
		modifier = Modifier.fillMaxSize()
	) { innerPadding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.background(Color.Black)
				.padding(innerPadding),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
		) {
			// Flag 1
			Button(onClick = { attack.flag1() }) {
				Text("Flag 1 - Basic Activity")
			}
			// Flag 2
			Button(onClick = {}) {
				Text("Flag 2")
			}
			// Flag 3
			Button(onClick = {}) {
				Text("Flag 3")
			}
			// Flag 4
			Button(onClick = {}) {
				Text("Flag 4")
			}
			// Flag 5
			Button(onClick = {}) {
				Text("Flag 5")
			}
			// Flag 6
			Button(onClick = {}) {
				Text("Flag 6")
			}
			// Flag 7
			Button(onClick = {}) {
				Text("Flag 7")
			}
			// Flag 8
			Button(onClick = {}) {
				Text("Flag 8")
			}
			// Flag 9
			Button(onClick = {}) {
				Text("Flag 9")
			}
		}
	}
}