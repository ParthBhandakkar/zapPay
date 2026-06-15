package com.zappay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.zappay.app.navigation.ZapPayNavGraph
import com.zappay.app.ui.theme.ZapPayTheme
import com.zappay.app.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ZapPayTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = White) {
                    val navController = rememberNavController()
                    ZapPayNavGraph(navController = navController)
                }
            }
        }
    }
}
