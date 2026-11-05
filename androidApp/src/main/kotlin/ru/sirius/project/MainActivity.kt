package ru.sirius.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // defaultComponentContext() связывает lifecycle, «назад» и сохранение состояния с Activity.
        val root = createRoot(defaultComponentContext())

        setContent {
            App(root)
        }
    }
}
