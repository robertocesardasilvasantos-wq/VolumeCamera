package com.roberto.volumecamera

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat

class VolumeAccessibilityService : AccessibilityService() {

    private var lastVolumeTime = 0L
    private var lastKey = 0

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
    }

    override fun onInterrupt() {
    }

    override fun onKeyEvent(
        event: KeyEvent
    ): Boolean {

        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        val key = event.keyCode

        if (
            key != KeyEvent.KEYCODE_VOLUME_UP &&
            key != KeyEvent.KEYCODE_VOLUME_DOWN
        ) {
            return false
        }

        val now = System.currentTimeMillis()

        if (
            key == lastKey &&
            now - lastVolumeTime <= 600
        ) {

            val intent = Intent(
                this,
                CameraService::class.java
            ).setAction(CameraService.TOGGLE)

            try {

                startService(intent)

            } catch (_: Exception) {

                ContextCompat.startForegroundService(
                    this,
                    intent
                )
            }

            lastVolumeTime = 0L
            lastKey = 0

            return false
        }

        lastKey = key
        lastVolumeTime = now

        return false
    }
}
