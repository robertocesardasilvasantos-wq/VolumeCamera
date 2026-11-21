package com.roberto.volumecamera

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 50, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Volume Camera"
            textSize = 28f
        }

        val info = TextView(this).apply {
            text = """
                1. Permita câmera e microfone.
                2. Ative o serviço de acessibilidade.
                3. Toque em "Ativar modo volume".
                4. Dois toques rápidos em Volume + ou Volume − iniciam/param a gravação.

                O Android mantém uma notificação enquanto o serviço de câmera estiver ativo.
            """.trimIndent()

            textSize = 17f
            setPadding(0, 30, 0, 30)
        }

        val permissions = Button(this).apply {
            text = "Permitir câmera e microfone"

            setOnClickListener {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    ),
                    10
                )
            }
        }

        val accessibility = Button(this).apply {
            text = "Ativar serviço de volume"

            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                )
            }
        }

        val activate = Button(this).apply {
            text = "Ativar modo volume"

            setOnClickListener {
                val intent = Intent(
                    this@MainActivity,
                    CameraService::class.java
                ).setAction(CameraService.START_SERVICE)

                ContextCompat.startForegroundService(
                    this@MainActivity,
                    intent
                )
            }
        }

        val start = Button(this).apply {
            text = "Iniciar gravação agora"

            setOnClickListener {
                val intent = Intent(
                    this@MainActivity,
                    CameraService::class.java
                ).setAction(CameraService.START)

                ContextCompat.startForegroundService(
                    this@MainActivity,
                    intent
                )
            }
        }

        val stop = Button(this).apply {
            text = "Parar gravação"

            setOnClickListener {
                startService(
                    Intent(
                        this@MainActivity,
                        CameraService::class.java
                    ).setAction(CameraService.STOP)
                )
            }
        }

        root.addView(title)
        root.addView(info)
        root.addView(permissions)
        root.addView(accessibility)
        root.addView(activate)
        root.addView(start)
        root.addView(stop)

        setContentView(root)
    }
}
