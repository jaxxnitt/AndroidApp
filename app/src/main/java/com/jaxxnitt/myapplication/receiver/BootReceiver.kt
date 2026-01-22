package com.jaxxnitt.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.worker.WorkerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as AreYouDeadApplication
            CoroutineScope(Dispatchers.IO).launch {
                val settings = app.settingsDataStore.getSettings()
                WorkerScheduler.scheduleWorkersOnBoot(context, settings)
            }
        }
    }
}
