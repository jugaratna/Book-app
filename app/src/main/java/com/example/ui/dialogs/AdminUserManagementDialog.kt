package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DocumentPermissionLevel
import com.example.data.model.MedicalDocument
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminUserManagementDialog(
    users: List<UserProfile>,
    documents: List<MedicalDocument>,
    currentUser: UserProfile,
    onDismiss: () -> Unit,
    onUpdateUserRole: (userId: String, newRole: UserRole) -> Unit,
    onDeleteUser: (userId: String) -> Unit,
    onSetDocumentPermission: (userId: String, docId: Long, permission: DocumentPermissionLevel) -> Unit,
    onSetBatchPermissions: (userId: String, docIds: List<Long>, permission: DocumentPermissionLevel) -> Unit,
    onCreateUser: (name: String, email: String, role: UserRole, specialty: String, title: String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Roles & Users, 1: Chapter Permissions Matrix
    var selectedUserForPermissions by remember { mutableStateOf(users.firstOrNull { it.role != UserRole.ADMIN } ?: users.firstOrNull()) }
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }
    var showAddUserModal by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Administration & Access Control",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SUPERADMIN",
                                        color = Color(0xFF7C3AED),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Logged in as ${currentUser.name} (${currentUser.role.badgeLabel})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs: Users & Roles vs Chapter Permissions Matrix
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Users & Roles (${users.size})", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chapter Permissions", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TAB 0: Users and Roles Management
                if (selectedTabIndex == 0) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Medical Staff & Author Accounts",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Button(
                                onClick = { showAddUserModal = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New User", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(users) { user ->
                                UserManagementCard(
                                    user = user,
                                    isCurrentUser = user.id == currentUser.id,
                                    totalAdmins = users.count { it.role == UserRole.ADMIN },
                                    documents = documents,
                                    onRoleChanged = { newRole ->
                                        onUpdateUserRole(user.id, newRole)
                                    },
                                    onDeleteClicked = {
                                        userToDelete = user
                                    },
                                    onConfigurePermissionsClicked = {
                                        selectedUserForPermissions = user
                                        selectedTabIndex = 1
                                    }
                                )
                            }
                        }
                    }
                }

                // TAB 1: Granular Chapter Permissions Matrix
                if (selectedTabIndex == 1) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        // User Selector Strip
                        Text(
                            text = "Select User to Manage Chapter Permissions:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            users.forEach { user ->
                                val isSelected = selectedUserForPermissions?.id == user.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedUserForPermissions = user },
                                    label = {
                                        Text("${user.name} (${user.role.badgeLabel})", fontSize = 12.sp)
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(user.avatarColorHex))
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        selectedUserForPermissions?.let { targetUser ->
                            // If target user is ADMIN
                            if (targetUser.role == UserRole.ADMIN) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF7C3AED).copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFF7C3AED),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "${targetUser.name} is an Administrator",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Administrators inherit unrestricted Full Edit & Administrative privileges across all ${documents.size} chapters and clinical protocols.",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Batch Actions Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Permissions for ${targetUser.name}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Role: ${targetUser.role.badgeLabel} (${targetUser.specialty})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Batch chips
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                onSetBatchPermissions(targetUser.id, documents.map { it.id }, DocumentPermissionLevel.FULL)
                                                // Refresh selection
                                                selectedUserForPermissions = selectedUserForPermissions?.copy(
                                                    customPermissions = documents.associate { it.id to DocumentPermissionLevel.FULL }
                                                )
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("All Edit", fontSize = 10.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                onSetBatchPermissions(targetUser.id, documents.map { it.id }, DocumentPermissionLevel.VIEW)
                                                selectedUserForPermissions = selectedUserForPermissions?.copy(
                                                    customPermissions = documents.associate { it.id to DocumentPermissionLevel.VIEW }
                                                )
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("All View", fontSize = 10.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Chapter list with permission toggles
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f, fill = false),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(documents) { doc ->
                                        val currentPerm = targetUser.getEffectivePermission(doc.id)

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        modifier = Modifier.weight(1f),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MenuBook,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = doc.title,
                                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            Text(
                                                                text = "${doc.docType} · ${doc.specialty}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Permission Selector for this chapter
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    PermissionOptionChip(
                                                        level = DocumentPermissionLevel.FULL,
                                                        isSelected = currentPerm == DocumentPermissionLevel.FULL,
                                                        onClick = {
                                                            onSetDocumentPermission(targetUser.id, doc.id, DocumentPermissionLevel.FULL)
                                                            val updated = targetUser.customPermissions.toMutableMap()
                                                            updated[doc.id] = DocumentPermissionLevel.FULL
                                                            selectedUserForPermissions = targetUser.copy(customPermissions = updated)
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    PermissionOptionChip(
                                                        level = DocumentPermissionLevel.VIEW,
                                                        isSelected = currentPerm == DocumentPermissionLevel.VIEW,
                                                        onClick = {
                                                            onSetDocumentPermission(targetUser.id, doc.id, DocumentPermissionLevel.VIEW)
                                                            val updated = targetUser.customPermissions.toMutableMap()
                                                            updated[doc.id] = DocumentPermissionLevel.VIEW
                                                            selectedUserForPermissions = targetUser.copy(customPermissions = updated)
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    PermissionOptionChip(
                                                        level = DocumentPermissionLevel.NONE,
                                                        isSelected = currentPerm == DocumentPermissionLevel.NONE,
                                                        onClick = {
                                                            onSetDocumentPermission(targetUser.id, doc.id, DocumentPermissionLevel.NONE)
                                                            val updated = targetUser.customPermissions.toMutableMap()
                                                            updated[doc.id] = DocumentPermissionLevel.NONE
                                                            selectedUserForPermissions = targetUser.copy(customPermissions = updated)
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Confirm User Account Deletion") },
            text = {
                Text(
                    "Are you sure you want to delete ${user.name} (${user.role.badgeLabel})? " +
                    "Their access permissions and profile will be revoked. Their past comments and peer review contributions will remain attributed."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(user.id)
                        userToDelete = null
                        if (selectedUserForPermissions?.id == user.id) {
                            selectedUserForPermissions = users.firstOrNull { it.id != user.id }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalRed)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add User Dialog Modal
    if (showAddUserModal) {
        AddUserModal(
            onDismiss = { showAddUserModal = false },
            onCreate = { name, email, role, specialty, title ->
                onCreateUser(name, email, role, specialty, title)
                showAddUserModal = false
            }
        )
    }
}

@Composable
fun UserManagementCard(
    user: UserProfile,
    isCurrentUser: Boolean,
    totalAdmins: Int,
    documents: List<MedicalDocument>,
    onRoleChanged: (UserRole) -> Unit,
    onDeleteClicked: () -> Unit,
    onConfigurePermissionsClicked: () -> Unit
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(user.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.avatarInitials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(You)",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = "${user.title} · ${user.specialty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }

                // Delete Button (disabled if only 1 admin)
                val canDelete = !(user.role == UserRole.ADMIN && totalAdmins <= 1)
                IconButton(
                    onClick = onDeleteClicked,
                    enabled = canDelete
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete User",
                        tint = if (canDelete) ClinicalRed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action row: Change Role Dropdown + Configure Permissions Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Role Dropdown Selector
                Box {
                    OutlinedButton(
                        onClick = { roleMenuExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = when (user.role) {
                                UserRole.ADMIN -> Icons.Default.Security
                                UserRole.EDITOR -> Icons.Default.Edit
                                UserRole.VIEW_ONLY -> Icons.Default.Visibility
                            },
                            contentDescription = null,
                            tint = Color(user.role.colorHex),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Role: ${user.role.badgeLabel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(user.role.colorHex)
                        )
                    }

                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(role.badgeLabel, fontWeight = FontWeight.Bold, color = Color(role.colorHex))
                                            if (user.role == role) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(role.colorHex),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(role.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    onRoleChanged(role)
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Configure Chapter Permissions
                if (user.role != UserRole.ADMIN) {
                    val permittedCount = documents.count { user.hasPermissionForDocument(it.id, DocumentPermissionLevel.VIEW) }
                    TextButton(
                        onClick = onConfigurePermissionsClicked,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chapters ($permittedCount/${documents.size})",
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Text(
                        text = "Full Access to All Chapters",
                        fontSize = 11.sp,
                        color = Color(0xFF7C3AED),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionOptionChip(
    level: DocumentPermissionLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when (level) {
        DocumentPermissionLevel.FULL -> Icons.Default.Edit to MedicalBluePrimary
        DocumentPermissionLevel.VIEW -> Icons.Default.Visibility to MedicalTealPrimary
        DocumentPermissionLevel.NONE -> Icons.Default.Lock to ClinicalRed
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = level.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserModal(
    onDismiss: () -> Unit,
    onCreate: (name: String, email: String, role: UserRole, specialty: String, title: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.EDITOR) }
    var roleExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Medical Staff Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name (with degrees)") },
                    placeholder = { Text("e.g. Dr. Laura Bailey, MD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Academic / Clinical Email") },
                    placeholder = { Text("l.bailey@hospital.org") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Specialty / Department") },
                    placeholder = { Text("e.g. Pediatric Surgery") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Clinical Position Title") },
                    placeholder = { Text("e.g. Senior Registrar / Fellow") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { roleExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Role: ${role.badgeLabel} (${role.title})", fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        UserRole.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text("${r.badgeLabel} - ${r.title}") },
                                onClick = {
                                    role = r
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Please enter user name"
                        return@Button
                    }
                    onCreate(
                        name,
                        email.ifBlank { "${name.lowercase().replace(" ", ".")}@documed.edu" },
                        role,
                        specialty.ifBlank { "General Medicine" },
                        title.ifBlank { role.title }
                    )
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
