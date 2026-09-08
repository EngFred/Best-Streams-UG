package com.engineerfred.beststreamsug.mobile.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.mobile.MainActivity
import com.engineerfred.beststreamsug.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications for newly released movies and series"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun showNewContentNotification(
        contentId: Int,
        kind: ContentKind,
        title: String,
        vjName: String?,
        genres: List<String>,
        overview: String?,
        posterUrl: String?,
    ) {
        // Format title: "Title by VJ <Name>" (e.g. "Spider-Man by VJ Junior")
        val formattedTitle = if (!vjName.isNullOrBlank()) {
            val vjDisplay = if (vjName.startsWith("VJ", ignoreCase = true)) vjName else "VJ $vjName"
            "$title by $vjDisplay"
        } else {
            title
        }

        // Format genres and overview
        val genresText = genres.filter { it.isNotBlank() }.joinToString(" • ")
        val shortSummary = if (genresText.isNotBlank()) genresText else overview.orEmpty()
        val fullBodyText = buildString {
            if (genresText.isNotBlank()) {
                append(genresText)
            }
            if (!overview.isNullOrBlank()) {
                if (isNotEmpty()) append("\n\n")
                append(overview)
            }
        }

        // Load poster bitmap via Coil
        val posterBitmap = posterUrl?.let { loadBitmap(it) }

        // PendingIntent to launch content details screen in MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONTENT_ID, contentId)
            putExtra(EXTRA_CONTENT_KIND, kind.name)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            contentId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(formattedTitle)
            .setContentText(shortSummary)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        if (posterBitmap != null) {
            builder.setLargeIcon(posterBitmap)
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(posterBitmap)
                    .setBigContentTitle(formattedTitle)
                    .setSummaryText(if (genresText.isNotBlank()) genresText else overview),
            )
        } else if (fullBodyText.isNotBlank()) {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(fullBodyText)
                    .setBigContentTitle(formattedTitle),
            )
        }

        try {
            notificationManager.notify(contentId, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted on Android 13+
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                result.drawable.toBitmap()
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    companion object {
        const val CHANNEL_ID = "best_streams_new_releases"
        const val CHANNEL_NAME = "New Releases"
        const val EXTRA_CONTENT_ID = "extra_notification_content_id"
        const val EXTRA_CONTENT_KIND = "extra_notification_content_kind"
    }
}
