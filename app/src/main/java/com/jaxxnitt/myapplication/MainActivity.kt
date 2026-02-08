package com.jaxxnitt.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.jaxxnitt.myapplication.ui.navigation.NavGraph
import com.jaxxnitt.myapplication.ui.theme.MyApplicationTheme
import com.jaxxnitt.myapplication.worker.WorkerScheduler
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : ComponentActivity(), PaymentResultListener {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Checkout.preload(applicationContext)

        requestPermissions()
        initializeWorkers()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }

    fun startRazorpayPayment(amount: Int) {
        val checkout = Checkout()
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID)

        val options = JSONObject().apply {
            put("name", "Are You Dead Yet?")
            put("description", "Buy Me a Coffee")
            put("currency", "INR")
            put("amount", amount * 100) // Amount in paise
            put("prefill", JSONObject().apply {
                put("email", "")
                put("contact", "")
            })
        }

        checkout.open(this, options)
    }

    override fun onPaymentSuccess(paymentId: String?) {
        Toast.makeText(this, "Thank you for the coffee! Payment ID: $paymentId", Toast.LENGTH_LONG).show()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }

        // Contacts permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
        }

        // Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun initializeWorkers() {
        val app = application as AreYouDeadApplication
        lifecycleScope.launch {
            val settings = app.settingsDataStore.getSettings()
            if (settings.isEnabled) {
                WorkerScheduler.scheduleWorkers(this@MainActivity, settings)
            }
        }
    }
}
