package com.example.home

import android.content.Context
import android.graphics.Color
import android.os.*
import android.view.View
import android.view.HapticFeedbackConstants
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.materialswitch.MaterialSwitch
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val mqtt = MqttManager()
    private var isConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("SecureHome", Context.MODE_PRIVATE)
        val loginCard = findViewById<View>(R.id.loginCard)
        val dashboard = findViewById<View>(R.id.dashboardContainer)
        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)
        val loader = findViewById<ProgressBar>(R.id.loadingSpinner)

        // Pre-fill saved credentials
        findViewById<EditText>(R.id.editServer).setText(prefs.getString("server", ""))
        findViewById<EditText>(R.id.editUser).setText(prefs.getString("user", ""))

        btnConnect.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            val server = findViewById<EditText>(R.id.editServer).text.toString().trim()
            val user = findViewById<EditText>(R.id.editUser).text.toString().trim()
            val pass = findViewById<EditText>(R.id.editPass).text.toString().trim()

            loader.visibility = View.VISIBLE
            btnConnect.isEnabled = false

            thread {
                val clientId = "Phone_${Build.MODEL}_${System.currentTimeMillis().toString().takeLast(4)}"
                mqtt.connect(server, clientId, user, pass) { connected ->
                    runOnUiThread {
                        isConnected = connected
                        loader.visibility = View.GONE
                        btnConnect.isEnabled = true
                        
                        if (connected) {
                            prefs.edit().putString("server", server).putString("user", user).apply()
                            txtStatus.text = "● SYSTEM ONLINE"
                            txtStatus.setTextColor(Color.parseColor("#4CAF50"))
                            animateTransition(loginCard, dashboard)
                            setupRelays()
                        } else {
                            txtStatus.text = "● SYSTEM OFFLINE"
                            txtStatus.setTextColor(Color.RED)
                        }
                    }
                }
            }
        }
    }

    private fun animateTransition(from: View, to: View) {
        from.animate().alpha(0f).translationY(-100f).setDuration(500).withEndAction {
            from.visibility = View.GONE
            to.apply {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = 100f
                animate().alpha(1f).translationY(0f)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(600).start()
            }
        }.start()
    }

    private fun setupRelays() {
        val ids = arrayOf(R.id.relay1, R.id.relay2, R.id.relay3, R.id.relay4)
        val colors = arrayOf("#FACC15", "#60A5FA", "#FACC15", "#60A5FA")

        for (i in ids.indices) {
            val view = findViewById<View>(ids[i]) ?: continue
            val sw = view.findViewById<MaterialSwitch>(R.id.deviceSwitch)
            val img = view.findViewById<ImageView>(R.id.deviceIcon)

            sw.setOnCheckedChangeListener { _, isChecked ->
                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                val topic = "home/esp32_001/relay/${i + 1}/cmd"
                
                if (isChecked) img.setColorFilter(Color.parseColor(colors[i]))
                else img.clearColorFilter()
                
                thread { mqtt.publish(topic, if (isChecked) "ON" else "OFF") }
            }
        }
    }
}