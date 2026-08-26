package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentPermissionLevel
import com.example.data.model.MedicalDocument
import com.example.data.model.UserRole
import com.example.ui.components.CreateDocumentDialog
import com.example.ui.dialogs.AdminUserManagementDialog
import com.example.ui.dialogs.TemplateLibraryDialog
import com.example.ui.dialogs.UserLoginSwitcherDialog
import com.example.ui.dialogs.UserRoleBadge
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DocuMedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentLibraryScreen(
    viewModel: DocuMedViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.documents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSpecialty by viewModel.selectedSpecialty.collectAsState()
    val selectedDocType by viewModel.selectedDocType.collectAsState()

    // Multi-User and Collaboration State
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.users.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showUserLoginDialog by remember { mutableStateOf(false) }
    var showAdminManagementDialog by remember { mutableStateOf(false) }
    var restrictedDocTitle by remember { mutableStateOf<String?>(null) }

    val specialties = listOf("All", "Orthopedics", "Cardiology", "Neurology", "Surgery", "Pediatrics", "Internal Medicine", "Oncology", "Pathology")
    val docTypes = listOf("All", "Textbook Chapter", "Clinical Protocol", "Case Report", "Lecture Note", "Question Bank", "OSCE Guide")

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Banner with Multi-User Pill & Navigation Actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MedicalBluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DocuMed Medical Library",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${documents.size} Clinical Publications · Multi-User Sync Active",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    // Active User Profile Switcher Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(currentUser.avatarColorHex).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(currentUser.avatarColorHex).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { showUserLoginDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color(currentUser.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.avatarInitials,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentUser.name.split(" ").firstOrNull() ?: currentUser.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(currentUser.avatarColorHex)
                            )
                        }
                    }

                    if (currentUser.role == UserRole.ADMIN) {
                        IconButton(onClick = { showAdminManagementDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Roles & Permissions",
                                tint = Color(0xFF7C3AED)
                            )
                        }
                    }

                    IconButton(onClick = { showTemplateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Medical Templates",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(onClick = { viewModel.navigateTo(AppNavTab.SETTINGS) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search chapters, diagnoses, classifications, drugs...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MedicalBluePrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Specialty Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(specialties) { specialty ->
                    val isSelected = selectedSpecialty == specialty
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedSpecialty.value = specialty },
                        label = { Text(specialty, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedicalBluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Doc Type Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 10.dp)
            ) {
                items(docTypes) { type ->
                    val isSelected = selectedDocType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedDocType.value = type },
                        label = { Text(type, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedicalTealPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Documents List
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No documents found",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+' to create or generate a medical chapter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        val permission = currentUser.getEffectivePermission(doc.id)
                        DocumentItemCard(
                            document = doc,
                            permission = permission,
                            isAdmin = currentUser.role == UserRole.ADMIN,
                            onSelect = {
                                if (permission == DocumentPermissionLevel.NONE && currentUser.role != UserRole.ADMIN) {
                                    restrictedDocTitle = doc.title
                                } else {
                                    viewModel.selectDocument(doc)
                                }
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(doc) },
                            onDuplicate = { viewModel.duplicateDocument(doc) },
                            onDelete = { viewModel.deleteDocument(doc) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // FAB to create document (only if not strictly view-only without edit permissions)
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MedicalBluePrimary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Create Document")
        }
    }

    if (restrictedDocTitle != null) {
        AlertDialog(
            onDismissRequest = { restrictedDocTitle = null },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626)) },
            title = { Text("Chapter Access Restricted") },
            text = {
                Text(
                    "You are currently logged in as '${currentUser.name}' (${currentUser.role.badgeLabel}).\n\n" +
                    "Your role or account permission does not have read access for '$restrictedDocTitle'.\n\n" +
                    "Switch to an Administrator account or request permissions to review this document."
                )
            },
            confirmButton = {
                Button(onClick = {
                    restrictedDocTitle = null
                    showUserLoginDialog = true
                }) {
                    Text("Switch User")
                }
            },
            dismissButton = {
                TextButton(onClick = { restrictedDocTitle = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showUserLoginDialog) {
        UserLoginSwitcherDialog(
            users = allUsers,
            currentUser = currentUser,
            onDismiss = { showUserLoginDialog = false },
            onSelectUser = { userId ->
                viewModel.switchUser(userId)
            },
            onOpenAdminPanel = {
                showAdminManagementDialog = true
            },
            onCreateUser = { name, email, role, specialty, title ->
                viewModel.createNewUser(name, email, role, specialty, title)
            }
        )
    }

    if (showAdminManagementDialog) {
        AdminUserManagementDialog(
            users = allUsers,
            documents = documents,
            currentUser = currentUser,
            onDismiss = { showAdminManagementDialog = false },
            onUpdateUserRole = { userId, newRole ->
                viewModel.updateUserRole(userId, newRole)
            },
            onDeleteUser = { userId ->
                viewModel.deleteUser(userId)
            },
            onSetDocumentPermission = { userId, docId, permission ->
                viewModel.setDocumentPermission(userId, docId, permission)
            },
            onSetBatchPermissions = { userId, docIds, permission ->
                viewModel.setBatchDocumentPermissions(userId, docIds, permission)
            },
            onCreateUser = { name, email, role, specialty, title ->
                viewModel.createNewUser(name, email, role, specialty, title)
            }
        )
    }

    if (showCreateDialog) {
        CreateDocumentDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, type, specialty, audience ->
                viewModel.createNewDocument(title, type, specialty, audience)
                showCreateDialog = false
            },
            onNavigateToAi = {
                showCreateDialog = false
                viewModel.navigateTo(AppNavTab.AI_STUDIO)
            }
        )
    }

    if (showTemplateDialog) {
        TemplateLibraryDialog(
            onDismiss = { showTemplateDialog = false },
            onSelectTemplateForCurrentDoc = { template ->
                viewModel.createDocumentFromTemplate(template)
            },
            onCreateNewDocFromTemplate = { template ->
                viewModel.createDocumentFromTemplate(template)
            }
        )
    }
}

@Composable
fun DocumentItemCard(
    document: MedicalDocument,
    permission: DocumentPermissionLevel = DocumentPermissionLevel.FULL,
    isAdmin: Boolean = false,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (permission == DocumentPermissionLevel.NONE && !isAdmin) Color(0xFFDC2626).copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Specialty, Type, and Permission Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MedicalBluePrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = document.specialty.uppercase(),
                            color = MedicalBluePrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MedicalTealPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = document.docType,
                            color = MedicalTealPrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Permission badge if view-only or restricted
                    if (permission == DocumentPermissionLevel.VIEW && !isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ClinicalAmber.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = ClinicalAmber, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Reviewer",
                                    color = ClinicalAmber,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    } else if (permission == DocumentPermissionLevel.NONE && !isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Restricted",
                                    color = Color(0xFFDC2626),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (document.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorite",
                            tint = if (document.isFavorite) ClinicalAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = {
                                    menuExpanded = false
                                    onDuplicate()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null) }
                            )
                            if (isAdmin || permission == DocumentPermissionLevel.FULL) {
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    },
                                    leadingIcon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Snippet
            Text(
                text = document.content.take(160).replace("#", "").replace("*", "").trim(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${document.wordCount} words · v${document.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = document.getFormattedDate(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

