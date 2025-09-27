package com.example.mediapicker

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mediapicker.ui.theme.MediaPickerTheme

class MainActivity : ComponentActivity() {
    val classicPicker = registerForActivityResult(
        ClassicContract
    ) { result ->
        Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show()
    }

    val modernPicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { result ->
        Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaPickerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Column(Modifier.padding(padding)) {
                        ClassicButtons(classicPicker)
                        ModernButtons(modernPicker)
                    }
                }
            }
        }
    }
}

object ClassicContract : ActivityResultContract<String, Uri?>() {
    override fun createIntent(context: Context, input: String) =
        Intent(Intent.ACTION_PICK).setType(input).setPackage("com.miui.gallery")

    override fun parseResult(resultCode: Int, intent: Intent?) =
        if (resultCode == RESULT_OK) intent?.data else null
}

@Composable
fun ModernButtons(
    picker: ActivityResultLauncher<PickVisualMediaRequest>
) {
    Text("AndroidX Picker (Single)")
    Row {
        Button({
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) { Text("Image Only") }
        Button({
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }) { Text("Video Only") }
        Button({
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }) { Text("Both") }
    }
}

@Composable
fun ClassicButtons(
    picker: ActivityResultLauncher<String>
) {
    Text("Intent.ACTION_PICK")
    Row {
        Button({
            picker.launch("image/*")
        }) { Text("Image Only") }
        Button({
            picker.launch("video/*")
        }) { Text("Video Only") }
        Button({
            picker.launch("*/*")
        }) { Text("Any") }
    }
}