package com.kroslabs.quickyshoppy

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.gson.Gson
import com.kroslabs.quickyshoppy.domain.model.ImportData
import com.kroslabs.quickyshoppy.domain.model.ImportItem
import com.kroslabs.quickyshoppy.ui.screens.debug.DebugLogsScreen
import com.kroslabs.quickyshoppy.ui.screens.import_items.ImportScreen
import com.kroslabs.quickyshoppy.ui.screens.ingredients.IngredientsScreen
import com.kroslabs.quickyshoppy.ui.screens.main.MainScreen
import com.kroslabs.quickyshoppy.ui.screens.settings.SettingsScreen
import com.kroslabs.quickyshoppy.ui.theme.QuickyShoppyTheme
import com.kroslabs.quickyshoppy.ui.viewmodel.ShoppingViewModel
import java.io.File

class MainActivity : ComponentActivity() {

    private val currentIntent = mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        currentIntent.value = intent
        setContent {
            QuickyShoppyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QuickyShoppyNavigation(
                        intent = currentIntent.value,
                        checkClipboard = { checkClipboardForImport() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent.value = intent
    }

    private fun checkClipboardForImport(): ImportData? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip
        if (clipData != null && clipData.itemCount > 0) {
            val text = clipData.getItemAt(0).text?.toString() ?: return null
            try {
                val importData = Gson().fromJson(text, ImportData::class.java)
                if (importData.items.isNotEmpty()) {
                    return importData
                }
            } catch (_: Exception) {
                // Not valid import JSON
            }
        }
        return null
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QuickyShoppyNavigation(
    intent: Intent?,
    checkClipboard: () -> ImportData?
) {
    val navController = rememberNavController()
    val viewModel: ShoppingViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri?.let { uri ->
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    viewModel.analyzeRecipePhoto(bytes)
                }
            }
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                viewModel.analyzeRecipePhoto(bytes)
            }
        }
    }

    // Handle deep links, shared text, and clipboard
    LaunchedEffect(intent) {
        var handled = false

        // Handle ACTION_VIEW (deep links)
        val data = intent?.data
        if (data != null && data.scheme == "quickyshoppy" && data.host == "import") {
            val base64Data = data.getQueryParameter("data")
            if (base64Data != null) {
                try {
                    val json = String(Base64.decode(base64Data, Base64.DEFAULT))
                    val importData = Gson().fromJson(json, ImportData::class.java)
                    viewModel.setImportItems(importData.items)
                    handled = true
                } catch (e: Exception) {
                    Toast.makeText(context, "Invalid import data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle ACTION_SEND (shared text)
        if (!handled && intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                // Look for quickyshoppy:// URL in the shared text
                val regex = Regex("quickyshoppy://import\\?data=([A-Za-z0-9+/=]+)")
                val match = regex.find(sharedText)
                if (match != null) {
                    val base64Data = match.groupValues[1]
                    try {
                        val json = String(Base64.decode(base64Data, Base64.DEFAULT))
                        val importData = Gson().fromJson(json, ImportData::class.java)
                        viewModel.setImportItems(importData.items)
                        handled = true
                    } catch (e: Exception) {
                        Toast.makeText(context, "Invalid import data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Check clipboard for import data if nothing else handled
        if (!handled) {
            val clipboardData = checkClipboard()
            if (clipboardData != null) {
                viewModel.setImportItems(clipboardData.items)
            }
        }
    }

    // Show ingredients screen when available
    if (uiState.ingredients != null) {
        IngredientsScreen(
            viewModel = viewModel,
            onDismiss = { /* ingredients cleared in ViewModel */ }
        )
        return
    }

    // Show import screen when available
    if (uiState.importItems != null) {
        ImportScreen(
            viewModel = viewModel,
            onDismiss = { /* items cleared in ViewModel */ }
        )
        return
    }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onCameraClick = { showImageSourceDialog = true }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDebugLogs = { navController.navigate("debug_logs") }
            )
        }
        composable("debug_logs") {
            DebugLogsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Add Recipe Photo") },
            text = { Text("Choose how to add a recipe photo") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    if (cameraPermissionState.status.isGranted) {
                        val photoFile = File(context.cacheDir, "recipe_${System.currentTimeMillis()}.jpg")
                        photoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        cameraLauncher.launch(photoUri!!)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }) {
                    Text("Take Photo")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) {
                    Text("Choose from Gallery")
                }
            }
        )
    }
}
