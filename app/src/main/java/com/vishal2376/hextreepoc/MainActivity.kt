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
				.background(Color.Black)
				.padding(innerPadding),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
		) {
			// Flag 1
			Button(onClick = { attack.flag1() }) {
				Text("Flag 1 - Basic Activity")
			}
		}
	}
}