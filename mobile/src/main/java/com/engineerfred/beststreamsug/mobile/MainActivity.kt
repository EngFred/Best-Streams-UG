package com.engineerfred.beststreamsug.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.mobile.core.notification.ContentNotificationManager
import com.engineerfred.beststreamsug.mobile.presentation.BestStreamsMobileApp
import com.engineerfred.beststreamsug.mobile.presentation.navigation.asContentKind
import com.engineerfred.beststreamsug.mobile.ui.theme.BestStreamsUGTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var targetContentId by mutableStateOf<Int?>(null)
    private var targetContentKind by mutableStateOf<ContentKind?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        extractNotificationTarget(intent)
        requestNotificationPermission()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            BestStreamsUGTheme {
                BestStreamsMobileApp(
                    initialContentId = targetContentId,
                    initialContentKind = targetContentKind,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationTarget(intent)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    private fun extractNotificationTarget(intent: Intent?) {
        val id = intent?.getIntExtra(ContentNotificationManager.EXTRA_CONTENT_ID, -1) ?: -1
        val kindStr = intent?.getStringExtra(ContentNotificationManager.EXTRA_CONTENT_KIND)
        if (id > 0 && !kindStr.isNullOrBlank()) {
            targetContentId = id
            targetContentKind = kindStr.asContentKind()
        }
    }
}