package com.example.mediapicker

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.mediapicker.ui.DropdownMenuChip
import com.example.mediapicker.ui.Indicator
import com.example.mediapicker.ui.InfoBar
import com.example.mediapicker.ui.applyInfoBarPadding
import com.example.mediapicker.ui.theme.MediaPickerTheme

enum class MediaType(
    val text: String,
    val type: PickVisualMedia.VisualMediaType
) {
    UNSPECIFIED("Unspecified", PickVisualMedia.ImageAndVideo),
    IMAGE_ONLY("Image Only", PickVisualMedia.ImageOnly),
    VIDEO_ONLY("Video Only", PickVisualMedia.VideoOnly)
}

val PickVisualMedia.VisualMediaType.mime: String
    get() = when (this) {
        PickVisualMedia.ImageOnly -> "image/*"
        PickVisualMedia.VideoOnly -> "video/*"
        PickVisualMedia.ImageAndVideo -> "*/*"
        is PickVisualMedia.SingleMimeType -> this.mimeType
    }

data class PickRequest(
    val media: MediaType,
    val multiple: Boolean
)

object PickVisualMediaViaGallery : ActivityResultContract<PickRequest, List<Uri>>() {
    override fun createIntent(context: Context, input: PickRequest): Intent {
        val intent = Intent(Intent.ACTION_PICK)
            .setType(input.media.type.mime)
        if (Build.MANUFACTURER.lowercase() == "xiaomi") {
            intent.setPackage("com.miui.gallery")
        }
        if (input.multiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        val uris = mutableListOf<Uri>()
        if (resultCode == RESULT_OK) {
            intent?.clipData?.let {
                for (i in 0 until it.itemCount) {
                    it.getItemAt(i).uri?.let(uris::add)
                }
            }
            intent?.data?.let(uris::add)
        }
        return uris
    }
}

class MainActivity : ComponentActivity() {
    val selections = mutableStateListOf<Uri>()

    val galleryPicker = registerForActivityResult(PickVisualMediaViaGallery) { result ->
        if (result.isEmpty()) {
            this.showEmptyToast()
        } else {
            this.selections.clear()
            this.selections.addAll(result)
        }
    }

    val singlePicker = registerForActivityResult(PickVisualMedia()) { result ->
        if (result === null) {
            this.showEmptyToast()
        } else {
            this.selections.clear()
            this.selections.add(result)
        }
    }

    val multiplePicker = registerForActivityResult(PickMultipleVisualMedia()) { result ->
        if (result.isEmpty()) {
            this.showEmptyToast()
        } else {
            this.selections.clear()
            this.selections.addAll(result)
        }
    }

    fun showEmptyToast() {
        Toast.makeText(this, "No Selection", Toast.LENGTH_SHORT).show()
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        this.setContent {
            MediaPickerTheme {
                val cutout = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                val scaffoldState = rememberBottomSheetScaffoldState(
                    rememberStandardBottomSheetState(
                        initialValue = SheetValue.Expanded
                    )
                )
                var multiple by rememberSaveable { mutableStateOf(false) }
                val mediaType = rememberSaveable { mutableStateOf(MediaType.UNSPECIFIED) }
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetPeekHeight = with(LocalDensity.current) {
                        cutout.getBottom(this).toDp()
                    } + 36.dp,
                    sheetDragHandle = null,
                    sheetContent = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            BottomSheetDefaults.DragHandle()
                        }
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            InfoBar(
                                title = "Media Type",
                                modifier = Modifier.applyInfoBarPadding()
                            ) {
                                DropdownMenuChip(
                                    selected = mediaType,
                                    options = MediaType.entries,
                                    modifier = Modifier.fillMaxWidth()
                                ) { it.text }
                            }
                            HorizontalDivider(Modifier.padding(horizontal = 12.dp))
                            InfoBar(
                                title = "Allow Multiple",
                                modifier = Modifier
                                    .toggleable(
                                        value = multiple,
                                        onValueChange = { multiple = !multiple },
                                        role = Role.Switch,
                                    )
                                    .applyInfoBarPadding()
                            ) {
                                Switch(checked = multiple, onCheckedChange = { multiple = it })
                            }
                            Picker(
                                "Intent.ACTION_PICK", if (multiple) {
                                    "Intent.EXTRA_ALLOW_MULTIPLE"
                                } else {
                                    "No Extra"
                                }
                            ) {
                                this@MainActivity.galleryPicker.launch(
                                    PickRequest(mediaType.value, multiple)
                                )
                            }
                            Picker(
                                "MediaStore.ACTION_PICK_IMAGES",
                                if (multiple) {
                                    "MediaStore.EXTRA_PICK_IMAGES_MAX"
                                } else {
                                    "No Extra"
                                }
                            ) {
                                if (multiple) {
                                    this@MainActivity.multiplePicker
                                } else {
                                    this@MainActivity.singlePicker
                                }.launch(
                                    PickVisualMediaRequest(mediaType.value.type)
                                )
                            }
                            Spacer(Modifier.height(with(LocalDensity.current) {
                                cutout.getBottom(this).toDp()
                            }))
                        }
                    },
                ) { padding ->
                    LazyColumn(contentPadding = padding) {
                        stickyHeader {
                            val color = MaterialTheme.colorScheme.surfaceContainer
                            Spacer(
                                Modifier
                                    .fillMaxWidth()
                                    .height(with(LocalDensity.current) {
                                        (cutout.getTop(this) * 1.2F).toDp()
                                    })
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                color.copy(alpha = 1f),
                                                color.copy(alpha = 0.8f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        items(this@MainActivity.selections) {
                            ListItem({ Text(it.toString()) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Picker(
    name: String,
    detail: String,
    onClick: () -> Unit
) {
    HorizontalDivider(Modifier.padding(horizontal = 12.dp))
    InfoBar(
        title = name,
        description = detail,
        modifier = Modifier
            .clickable(onClick = onClick)
            .applyInfoBarPadding(),
        indicator = {
            Indicator(Icons.AutoMirrored.Filled.KeyboardArrowRight)
        }
    )
}