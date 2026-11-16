package com.spark

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.spark.ui.navigation.SparkNavGraph
import com.spark.ui.screens.main.GlobalUserState
import com.spark.ui.screens.main.MindPalaceData
import com.spark.ui.theme.SparkTheme
import com.spark.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    var launchSparkFull by mutableStateOf(false)
    var sparkTitle by mutableStateOf("")
    var sparkCategory by mutableStateOf("")
    var sparkEmoji by mutableStateOf("")
    var sparkDescription by mutableStateOf("")
    var sparkSteps by mutableStateOf<Array<String>>(emptyArray())
    var sparkFunFact by mutableStateOf("")
    var sparkIsAI by mutableStateOf<Boolean?>(null)
    var initialTabRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load persisted theme & state & mind palace data before UI hierarchy renders
        ThemeManager.loadTheme(this)
        // Account-owned state is restored after DataStore identifies the session in SparkNavGraph.

        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            SparkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SparkNavGraph(
                        launchSparkFull = launchSparkFull,
                        sparkTitle = sparkTitle,
                        sparkCategory = sparkCategory,
                        sparkEmoji = sparkEmoji,
                        sparkDescription = sparkDescription,
                        sparkSteps = sparkSteps,
                        sparkFunFact = sparkFunFact,
                        sparkIsAI = sparkIsAI,
                        initialTabRoute = initialTabRoute,
                        onSparkConsumed = {
                            launchSparkFull = false
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("launch_spark_full", false)) {
                launchSparkFull = true
                sparkTitle = it.getStringExtra("spark_title") ?: ""
                sparkCategory = it.getStringExtra("spark_category") ?: ""
                sparkEmoji = it.getStringExtra("spark_emoji") ?: "🧠"
                sparkDescription = it.getStringExtra("spark_description") ?: ""
                sparkSteps = it.getStringArrayExtra("spark_steps") ?: emptyArray()
                sparkFunFact = it.getStringExtra("spark_fun_fact") ?: ""
                sparkIsAI = if (it.hasExtra("spark_is_ai")) it.getBooleanExtra("spark_is_ai", false) else null
            }
            if (it.hasExtra("navigate_tab")) {
                initialTabRoute = it.getStringExtra("navigate_tab")
            }
        }
    }
}