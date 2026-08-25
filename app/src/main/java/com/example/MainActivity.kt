package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.DocumentEditorScreen
import com.example.ui.screens.DocumentLibraryScreen
import com.example.ui.screens.ExportPreviewScreen
import com.example.ui.screens.KnowledgeBaseScreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DocuMedViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DocuMedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DocuMedMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DocuMedMainApp(viewModel: DocuMedViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppNavTab.LIBRARY,
                    onClick = { viewModel.navigateTo(AppNavTab.LIBRARY) },
                    icon = { Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Library") },
                    label = { Text("Library", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.LIBRARY) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalBluePrimary,
                        selectedTextColor = MedicalBluePrimary,
                        indicatorColor = MedicalBluePrimary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.EDITOR,
                    onClick = { viewModel.navigateTo(AppNavTab.EDITOR) },
                    icon = { Icon(imageVector = Icons.Default.EditNote, contentDescription = "Editor") },
                    label = { Text("Editor", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.EDITOR) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalBluePrimary,
                        selectedTextColor = MedicalBluePrimary,
                        indicatorColor = MedicalBluePrimary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.AI_STUDIO,
                    onClick = { viewModel.navigateTo(AppNavTab.AI_STUDIO) },
                    icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Studio") },
                    label = { Text("AI Studio", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.AI_STUDIO) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalTealPrimary,
                        selectedTextColor = MedicalTealPrimary,
                        indicatorColor = MedicalTealPrimary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.KNOWLEDGE_BASE,
                    onClick = { viewModel.navigateTo(AppNavTab.KNOWLEDGE_BASE) },
                    icon = { Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Knowledge") },
                    label = { Text("Knowledge", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.KNOWLEDGE_BASE) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalBluePrimary,
                        selectedTextColor = MedicalBluePrimary,
                        indicatorColor = MedicalBluePrimary.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppNavTab.EXPORT_PREVIEW,
                    onClick = { viewModel.navigateTo(AppNavTab.EXPORT_PREVIEW) },
                    icon = { Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Export") },
                    label = { Text("Export", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.EXPORT_PREVIEW) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MedicalBluePrimary,
                        selectedTextColor = MedicalBluePrimary,
                        indicatorColor = MedicalBluePrimary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.LIBRARY -> DocumentLibraryScreen(viewModel = viewModel)
                AppNavTab.EDITOR -> DocumentEditorScreen(viewModel = viewModel)
                AppNavTab.AI_STUDIO -> AiStudioScreen(viewModel = viewModel)
                AppNavTab.KNOWLEDGE_BASE -> KnowledgeBaseScreen(viewModel = viewModel)
                AppNavTab.EXPORT_PREVIEW -> ExportPreviewScreen(viewModel = viewModel)
            }
        }
    }
}
