package com.example.data.repository

import com.example.data.model.CollaboratorPresence
import com.example.data.model.CommentReply
import com.example.data.model.CommentStatus
import com.example.data.model.CommentType
import com.example.data.model.DocumentComment
import com.example.data.model.DocumentPermissionLevel
import com.example.data.model.PredefinedUsers
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CollaborationRepository {

    // Users list
    private val _users = MutableStateFlow<List<UserProfile>>(PredefinedUsers.DEFAULT_USERS)
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    // Current active user session
    private val _currentUser = MutableStateFlow<UserProfile>(PredefinedUsers.ADMIN_SARAH)
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Comments mapped by documentId
    private val _documentComments = MutableStateFlow<Map<Long, List<DocumentComment>>>(createInitialComments())
    val documentComments: StateFlow<Map<Long, List<DocumentComment>>> = _documentComments.asStateFlow()

    // Collaborators online presence
    private val _activeCollaborators = MutableStateFlow<List<CollaboratorPresence>>(createInitialCollaborators())
    val activeCollaborators: StateFlow<List<CollaboratorPresence>> = _activeCollaborators.asStateFlow()

    // Switch Current User
    fun switchUser(userId: String) {
        val user = _users.value.find { it.id == userId } ?: return
        _currentUser.value = user
        updateUserPresence(user)
    }

    // Add New User
    fun createUser(
        name: String,
        email: String,
        role: UserRole,
        specialty: String,
        title: String,
        customPermissions: Map<Long, DocumentPermissionLevel> = emptyMap()
    ): UserProfile {
        val initials = name.split(" ")
            .filter { it.isNotBlank() && !it.contains(".") }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { name.take(2).uppercase() }

        val colors = listOf(
            0xFF7C3AED, 0xFF0284C7, 0xFF0D9488, 0xFFE11D48,
            0xFFEA580C, 0xFF4F46E5, 0xFF059669, 0xFFD97706
        )
        val colorHex = colors[(_users.value.size) % colors.size]

        val newUser = UserProfile(
            id = "usr_" + UUID.randomUUID().toString().take(8),
            name = name.trim(),
            email = email.trim(),
            role = role,
            specialty = specialty.trim().ifBlank { "General Medicine" },
            title = title.trim().ifBlank { role.title },
            avatarColorHex = colorHex,
            avatarInitials = initials,
            customPermissions = customPermissions,
            statusMessage = "Active in DocuMed Studio"
        )

        _users.value = _users.value + newUser
        return newUser
    }

    // Admin: Change User Role
    fun updateUserRole(userId: String, newRole: UserRole) {
        _users.value = _users.value.map { user ->
            if (user.id == userId) {
                user.copy(role = newRole, title = if (user.title == user.role.title) newRole.title else user.title)
            } else {
                user
            }
        }
        // Update current user if modified
        if (_currentUser.value.id == userId) {
            _currentUser.value = _currentUser.value.copy(
                role = newRole,
                title = if (_currentUser.value.title == _currentUser.value.role.title) newRole.title else _currentUser.value.title
            )
        }
    }

    // Admin: Delete User
    fun deleteUser(userId: String): Boolean {
        // Prevent deleting if only 1 user or cannot delete yourself if you are the only admin
        val currentList = _users.value
        val userToDelete = currentList.find { it.id == userId } ?: return false
        val adminCount = currentList.count { it.role == UserRole.ADMIN }

        if (userToDelete.role == UserRole.ADMIN && adminCount <= 1) {
            return false // Cannot delete the sole administrator
        }

        val updatedList = currentList.filter { it.id != userId }
        _users.value = updatedList

        // If the deleted user was active, switch to first available user
        if (_currentUser.value.id == userId) {
            _currentUser.value = updatedList.firstOrNull() ?: PredefinedUsers.ADMIN_SARAH
        }
        return true
    }

    // Admin: Set Granular Document Permission for specific user
    fun setDocumentPermission(userId: String, docId: Long, permission: DocumentPermissionLevel) {
        _users.value = _users.value.map { user ->
            if (user.id == userId) {
                val updatedPermissions = user.customPermissions.toMutableMap()
                updatedPermissions[docId] = permission
                user.copy(customPermissions = updatedPermissions)
            } else {
                user
            }
        }
        if (_currentUser.value.id == userId) {
            val updatedPermissions = _currentUser.value.customPermissions.toMutableMap()
            updatedPermissions[docId] = permission
            _currentUser.value = _currentUser.value.copy(customPermissions = updatedPermissions)
        }
    }

    // Admin: Batch Set Permissions for all documents
    fun setBatchPermissions(userId: String, docIds: List<Long>, permission: DocumentPermissionLevel) {
        _users.value = _users.value.map { user ->
            if (user.id == userId) {
                val updatedPermissions = user.customPermissions.toMutableMap()
                docIds.forEach { docId ->
                    updatedPermissions[docId] = permission
                }
                user.copy(customPermissions = updatedPermissions)
            } else {
                user
            }
        }
        if (_currentUser.value.id == userId) {
            val updatedPermissions = _currentUser.value.customPermissions.toMutableMap()
            docIds.forEach { docId ->
                updatedPermissions[docId] = permission
            }
            _currentUser.value = _currentUser.value.copy(customPermissions = updatedPermissions)
        }
    }

    // ==========================================
    // COMMENTS & ANNOTATIONS
    // ==========================================

    fun getCommentsForDocument(documentId: Long): List<DocumentComment> {
        return _documentComments.value[documentId] ?: emptyList()
    }

    fun addComment(
        documentId: Long,
        author: UserProfile,
        commentText: String,
        selectedText: String = "",
        textRangeStart: Int = -1,
        textRangeEnd: Int = -1,
        sectionTitle: String = "General Document",
        commentType: CommentType = CommentType.GENERAL,
        suggestedReplacement: String = ""
    ): DocumentComment {
        val newComment = DocumentComment(
            id = "cmt_" + UUID.randomUUID().toString().take(8),
            documentId = documentId,
            authorId = author.id,
            authorName = author.name,
            authorRole = author.role,
            authorSpecialty = author.specialty,
            authorAvatarColor = author.avatarColorHex,
            selectedText = selectedText.trim(),
            textRangeStart = textRangeStart,
            textRangeEnd = textRangeEnd,
            sectionTitle = sectionTitle.ifBlank { "General Document" },
            commentText = commentText.trim(),
            suggestedReplacement = suggestedReplacement.trim(),
            commentType = commentType,
            status = CommentStatus.OPEN,
            replies = emptyList()
        )

        val currentMap = _documentComments.value.toMutableMap()
        val list = currentMap[documentId]?.toMutableList() ?: mutableListOf()
        list.add(0, newComment) // Add to top
        currentMap[documentId] = list
        _documentComments.value = currentMap

        return newComment
    }

    fun addReply(
        commentId: String,
        documentId: Long,
        author: UserProfile,
        replyText: String
    ): CommentReply {
        val reply = CommentReply(
            id = "rpl_" + UUID.randomUUID().toString().take(8),
            commentId = commentId,
            authorId = author.id,
            authorName = author.name,
            authorRole = author.role,
            authorSpecialty = author.specialty,
            authorAvatarColor = author.avatarColorHex,
            text = replyText.trim()
        )

        val currentMap = _documentComments.value.toMutableMap()
        val list = currentMap[documentId]?.toMutableList() ?: return reply
        val index = list.indexOfFirst { it.id == commentId }
        if (index != -1) {
            val target = list[index]
            val updatedReplies = target.replies + reply
            list[index] = target.copy(replies = updatedReplies)
            currentMap[documentId] = list
            _documentComments.value = currentMap
        }

        return reply
    }

    fun resolveComment(commentId: String, documentId: Long, resolver: UserProfile) {
        val currentMap = _documentComments.value.toMutableMap()
        val list = currentMap[documentId]?.toMutableList() ?: return
        val index = list.indexOfFirst { it.id == commentId }
        if (index != -1) {
            val target = list[index]
            list[index] = target.copy(
                status = CommentStatus.RESOLVED,
                resolvedBy = resolver.name,
                resolvedAt = System.currentTimeMillis()
            )
            currentMap[documentId] = list
            _documentComments.value = currentMap
        }
    }

    fun reopenComment(commentId: String, documentId: Long) {
        val currentMap = _documentComments.value.toMutableMap()
        val list = currentMap[documentId]?.toMutableList() ?: return
        val index = list.indexOfFirst { it.id == commentId }
        if (index != -1) {
            val target = list[index]
            list[index] = target.copy(
                status = CommentStatus.OPEN,
                resolvedBy = "",
                resolvedAt = 0L
            )
            currentMap[documentId] = list
            _documentComments.value = currentMap
        }
    }

    fun deleteComment(commentId: String, documentId: Long) {
        val currentMap = _documentComments.value.toMutableMap()
        val list = currentMap[documentId]?.toMutableList() ?: return
        list.removeAll { it.id == commentId }
        currentMap[documentId] = list
        _documentComments.value = currentMap
    }

    private fun updateUserPresence(user: UserProfile) {
        _activeCollaborators.value = _activeCollaborators.value.map { presence ->
            if (presence.userId == user.id) {
                presence.copy(isOnline = true, lastActiveMillis = System.currentTimeMillis())
            } else {
                presence
            }
        }
    }

    fun updateCurrentSectionPresence(sectionTitle: String, isEditing: Boolean) {
        val current = _currentUser.value
        val list = _activeCollaborators.value.toMutableList()
        val index = list.indexOfFirst { it.userId == current.id }
        if (index != -1) {
            list[index] = list[index].copy(
                activeSection = sectionTitle,
                isEditing = isEditing,
                lastActiveMillis = System.currentTimeMillis()
            )
        } else {
            list.add(
                CollaboratorPresence(
                    userId = current.id,
                    name = current.name,
                    role = current.role,
                    specialty = current.specialty,
                    avatarColorHex = current.avatarColorHex,
                    avatarInitials = current.avatarInitials,
                    activeSection = sectionTitle,
                    isEditing = isEditing
                )
            )
        }
        _activeCollaborators.value = list
    }

    private companion object {
        fun createInitialCollaborators(): List<CollaboratorPresence> {
            return listOf(
                CollaboratorPresence(
                    userId = PredefinedUsers.ADMIN_SARAH.id,
                    name = PredefinedUsers.ADMIN_SARAH.name,
                    role = PredefinedUsers.ADMIN_SARAH.role,
                    specialty = PredefinedUsers.ADMIN_SARAH.specialty,
                    avatarColorHex = PredefinedUsers.ADMIN_SARAH.avatarColorHex,
                    avatarInitials = PredefinedUsers.ADMIN_SARAH.avatarInitials,
                    activeSection = "1.4 Treatment Protocol & Surgical Decision Algorithm",
                    isOnline = true,
                    isEditing = false
                ),
                CollaboratorPresence(
                    userId = PredefinedUsers.EDITOR_ALEX.id,
                    name = PredefinedUsers.EDITOR_ALEX.name,
                    role = PredefinedUsers.EDITOR_ALEX.role,
                    specialty = PredefinedUsers.EDITOR_ALEX.specialty,
                    avatarColorHex = PredefinedUsers.EDITOR_ALEX.avatarColorHex,
                    avatarInitials = PredefinedUsers.EDITOR_ALEX.avatarInitials,
                    activeSection = "1.2 Garden Classification System",
                    isOnline = true,
                    isEditing = true
                ),
                CollaboratorPresence(
                    userId = PredefinedUsers.VIEWER_ELENA.id,
                    name = PredefinedUsers.VIEWER_ELENA.name,
                    role = PredefinedUsers.VIEWER_ELENA.role,
                    specialty = PredefinedUsers.VIEWER_ELENA.specialty,
                    avatarColorHex = PredefinedUsers.VIEWER_ELENA.avatarColorHex,
                    avatarInitials = PredefinedUsers.VIEWER_ELENA.avatarInitials,
                    activeSection = "1.5 Complications & Postoperative Rehabilitation",
                    isOnline = true,
                    isEditing = false
                )
            )
        }

        fun createInitialComments(): Map<Long, List<DocumentComment>> {
            val doc1Comments = listOf(
                DocumentComment(
                    id = "cmt_hip_001",
                    documentId = 1L,
                    authorId = PredefinedUsers.EDITOR_ALEX.id,
                    authorName = PredefinedUsers.EDITOR_ALEX.name,
                    authorRole = PredefinedUsers.EDITOR_ALEX.role,
                    authorSpecialty = PredefinedUsers.EDITOR_ALEX.specialty,
                    authorAvatarColor = PredefinedUsers.EDITOR_ALEX.avatarColorHex,
                    selectedText = "In patients >65 years with displaced fractures (Garden III/IV), arthroplasty yields superior functional outcomes and lower revision rates",
                    sectionTitle = "1.2 Garden Classification System",
                    commentText = "Should we cite the landmark HEALTH (2019) and FAITH (2017) trials here? They specifically compared THA versus Hemiarthroplasty and cannulated screws in this age bracket.",
                    suggestedReplacement = "In patients >65 years with displaced fractures (Garden III/IV), Total Hip Arthroplasty (THA) yields superior 2-year functional scores and reduced revision rates compared to internal fixation (HEALTH & FAITH trials).",
                    commentType = CommentType.EVIDENCE_QUERY,
                    status = CommentStatus.OPEN,
                    replies = listOf(
                        CommentReply(
                            id = "rpl_hip_001",
                            commentId = "cmt_hip_001",
                            authorId = PredefinedUsers.ADMIN_SARAH.id,
                            authorName = PredefinedUsers.ADMIN_SARAH.name,
                            authorRole = PredefinedUsers.ADMIN_SARAH.role,
                            authorSpecialty = PredefinedUsers.ADMIN_SARAH.specialty,
                            authorAvatarColor = PredefinedUsers.ADMIN_SARAH.avatarColorHex,
                            text = "Agreed! I will insert the Vancouver citation key for Bhandari M et al. NEJM 2019.",
                            createdAt = System.currentTimeMillis() - 3600000
                        )
                    ),
                    createdAt = System.currentTimeMillis() - 7200000
                ),
                DocumentComment(
                    id = "cmt_hip_002",
                    documentId = 1L,
                    authorId = PredefinedUsers.VIEWER_ELENA.id,
                    authorName = PredefinedUsers.VIEWER_ELENA.name,
                    authorRole = PredefinedUsers.VIEWER_ELENA.role,
                    authorSpecialty = PredefinedUsers.VIEWER_ELENA.specialty,
                    authorAvatarColor = PredefinedUsers.VIEWER_ELENA.avatarColorHex,
                    selectedText = "Chemical thromboprophylaxis is recommended for a minimum of 28-35 days postoperatively.",
                    sectionTitle = "1.5 Complications & Postoperative Rehabilitation",
                    commentText = "Clinical Safety Note: Highlight that Aspirin 81-100mg BID can be used after initial 5 days of LMWH/DOAC according to latest 2024 AAOS / ACCP VTE guidelines.",
                    suggestedReplacement = "Chemical thromboprophylaxis (LMWH, DOACs, or Aspirin following 5-day lead-in) is recommended for a minimum of 28-35 days postoperatively.",
                    commentType = CommentType.CLINICAL_SAFETY,
                    status = CommentStatus.OPEN,
                    createdAt = System.currentTimeMillis() - 14400000
                ),
                DocumentComment(
                    id = "cmt_hip_003",
                    documentId = 1L,
                    authorId = PredefinedUsers.ADMIN_SARAH.id,
                    authorName = PredefinedUsers.ADMIN_SARAH.name,
                    authorRole = PredefinedUsers.ADMIN_SARAH.role,
                    authorSpecialty = PredefinedUsers.ADMIN_SARAH.specialty,
                    authorAvatarColor = PredefinedUsers.ADMIN_SARAH.avatarColorHex,
                    selectedText = "Prolonged surgical delay (>48 hours) in geriatric hip fractures is directly correlated with increased 30-day mortality",
                    sectionTitle = "1.4 Treatment Protocol & Surgical Decision Algorithm",
                    commentText = "Peer review approved. This is an essential audit metric for national hip fracture databases (NHFD).",
                    commentType = CommentType.PEER_REVIEW,
                    status = CommentStatus.RESOLVED,
                    resolvedBy = PredefinedUsers.ADMIN_SARAH.name,
                    resolvedAt = System.currentTimeMillis() - 1800000,
                    createdAt = System.currentTimeMillis() - 28800000
                )
            )

            val doc2Comments = listOf(
                DocumentComment(
                    id = "cmt_stemi_001",
                    documentId = 2L,
                    authorId = PredefinedUsers.VIEWER_ELENA.id,
                    authorName = PredefinedUsers.VIEWER_ELENA.name,
                    authorRole = PredefinedUsers.VIEWER_ELENA.role,
                    authorSpecialty = PredefinedUsers.VIEWER_ELENA.specialty,
                    authorAvatarColor = PredefinedUsers.VIEWER_ELENA.avatarColorHex,
                    selectedText = "Ticagrelor 180 mg (or Prasugrel 60 mg if proceeding to PCI; Clopidogrel 600 mg if fibrinolysis)",
                    sectionTitle = "1.2 Emergency Pharmacotherapy",
                    commentText = "Contraindication reminder: Prasugrel is contraindicated in patients with a history of prior stroke or TIA.",
                    commentType = CommentType.CLINICAL_SAFETY,
                    status = CommentStatus.OPEN,
                    createdAt = System.currentTimeMillis() - 10800000
                )
            )

            return mapOf(
                1L to doc1Comments,
                2L to doc2Comments
            )
        }
    }
}
