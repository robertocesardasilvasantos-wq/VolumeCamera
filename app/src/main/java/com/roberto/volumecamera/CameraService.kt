package com.roberto.volumecamera

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Environment
import androidx.camera.core.CameraSelector
import androidx.camera.core.Quality
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import java.io.File

class CameraService : LifecycleService() {

    companion object {
        const val START_SERVICE = "START_SERVICE"
        const val START = "START"
        const val STOP = "STOP"
        const val TOGGLE = "TOGGLE"

        private const val CHANNEL = "volume_camera"
        private const val ID = 42
    }

    private var recording: Recording? = null
    private var videoCapture: VideoCapture<Recorder>? = null

    override fun onCreate() {
        super.onCreate()

        createChannel()

        startForeground(
            ID,
            notification("Modo volume ativo"),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        )

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupCamera()
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            START_SERVICE -> {
                // Mantém o serviço ativo para receber os comandos de volume.
            }

            START -> {
                startRecording()
            }

            STOP -> {
                stopRecording()
            }

            TOGGLE -> {
                if (recording == null) {
                    startRecording()
                } else {
                    stopRecording()
                }
            }
        }

        return START_STICKY
    }

    private fun setupCamera() {

        val future = ProcessCameraProvider.getInstance(this)

        future.addListener({

            try {

                val provider = future.get()

                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(Quality.HIGHEST)
                    )
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                provider.unbindAll()

                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    videoCapture
                )

            } catch (_: Exception) {
                // A câmera será tentada novamente quando necessário.
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startRecording() {

        if (recording != null) {
            return
        }

        if (videoCapture == null) {

            setupCamera()

            android.os.Handler(mainLooper).postDelayed(
                {
                    startRecording()
                },
                1000
            )

            return
        }

        val directory =
            getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                ?: filesDir

        directory.mkdirs()

        val file = File(
            directory,
            "VIDEO_${System.currentTimeMillis()}.mp4"
        )

        val output =
            FileOutputOptions.Builder(file).build()

        var pending =
            videoCapture!!
                .output
                .prepareRecording(this, output)

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            pending = pending.withAudioEnabled()
        }

        recording = pending.start(
            ContextCompat.getMainExecutor(this)
        ) { event ->

            if (event is VideoRecordEvent.Finalize) {

                recording = null

                getSystemService(
                    NotificationManager::class.java
                ).notify(
                    ID,
                    notification(
                        "Gravação salva: ${file.name}"
                    )
                )
            }
        }

        getSystemService(
            NotificationManager::class.java
        ).notify(
            ID,
            notification("Gravando vídeo")
        )
    }

    private fun stopRecording() {

        recording?.stop()

        recording = null

        getSystemService(
            NotificationManager::class.java
        ).notify(
            ID,
            notification("Gravação parada")
        )
    }

    private fun notification(
        text: String
    ): Notification {

        return NotificationCompat.Builder(
            this,
            CHANNEL
        )
            .setContentTitle("Volume Camera")
            .setContentText(text)
            .setSmallIcon(
                android.R.drawable.ic_menu_camera
            )
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {

        getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(

            NotificationChannel(
                CHANNEL,
                "Volume Camera",
                NotificationManager.IMPORTANCE_LOW
            )
        )
    }

    override fun onDestroy() {

        recording?.stop()

        recording = null

        super.onDestroy()
    }
}
