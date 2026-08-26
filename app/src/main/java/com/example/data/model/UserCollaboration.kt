package com.example.data.model

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class UserRole(
    val title: String,
    val badgeLabel: String,
    val description: String,
    val colorHex: Long
) {
    ADMIN(
        title = "Chief Medical Administrator",
        badgeLabel = "ADMIN",
        description = "Full privileges: manage user roles, delete users, configure chapter permissions, and full editing access.",
        colorHex = 0xFF7C3AED // Deep Purple
    ),
    EDITOR(
        title = "Contributing Author / Editor",
        badgeLabel = "EDITOR",
        description = "Content creator: author and edit assigned chapters, apply AI co-pilot tools, add annotations and resolve feedback.",
        colorHex = 0xFF0284C7 // Medical Ocean Blue
    ),
    VIEW_ONLY(
        title = "Clinical Peer Reviewer / Observer",
        badgeLabel = "VIEW ONLY",
        description = "Review & Comment: read assigned chapters, submit inline comments and review feedback, cannot edit document body.",
        colorHex = 0xFF0D9488 // Teal
    )
}

enum class DocumentPermissionLevel(
    val label: String,
    val description: String,
    val iconName: String
) {
    FULL("Full Edit", "Can view, edit content, and resolve comments", "Edit"),
    VIEW("View & Comment", "Can view content and submit comments / annotations", "Visibility"),
    NONE("No Access", "Access restricted to this document / chapter", "Lock")
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val specialty: String,
    val title: String,
    val avatarColorHex: Long,
    val avatarInitials: String,
    val customPermissions: Map<Long, DocumentPermissionLevel> = emptyMap(),
    val statusMessage: String = "Available for Clinical Review",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun hasPermissionForDocument(documentId: Long, requiredLevel: DocumentPermissionLevel): Boolean {
        // Admin always has full access
        if (role == UserRole.ADMIN) return true

        // Check custom granular document permission if set
        val explicitLevel = customPermissions[documentId]
        if (explicitLevel != null) {
            return when (requiredLevel) {
                DocumentPermissionLevel.NONE -> true
                DocumentPermissionLevel.VIEW -> explicitLevel == DocumentPermissionLevel.VIEW || explicitLevel == DocumentPermissionLevel.FULL
                DocumentPermissionLevel.FULL -> explicitLevel == DocumentPermissionLevel.FULL
            }
        }

        // Default role fallback when no specific custom override
        return when (role) {
            UserRole.ADMIN -> true
            UserRole.EDITOR -> true // Editors default to full access on standard docs
            UserRole.VIEW_ONLY -> requiredLevel == DocumentPermissionLevel.VIEW || requiredLevel == DocumentPermissionLevel.NONE
        }
    }

    fun getEffectivePermission(documentId: Long): DocumentPermissionLevel {
        if (role == UserRole.ADMIN) return DocumentPermissionLevel.FULL
        val explicit = customPermissions[documentId]
        if (explicit != null) return explicit
        return when (role) {
            UserRole.ADMIN -> DocumentPermissionLevel.FULL
            UserRole.EDITOR -> DocumentPermissionLevel.FULL
            UserRole.VIEW_ONLY -> DocumentPermissionLevel.VIEW
        }
    }
}

enum class CommentType(
    val label: String,
    val iconDesc: String,
    val colorHex: Long
) {
    CLINICAL_SAFETY("Clinical Safety", "Safety & Contraindications", 0xFFD97706), // Amber
    PEER_REVIEW("Peer Review", "Academic & Guideline critique", 0xFF4F46E5), // Indigo
    EVIDENCE_QUERY("Evidence & Trial", "Clinical trial & citation check", 0xFF0284C7), // Blue
    SUGGESTION("Suggested Edit", "Text replacement or addition", 0xFF059669), // Green
    TYPO_FIX("Typo / Nomenclature", "Spelling & Medical Terminology", 0xFF7C3AED), // Purple
    GENERAL("General Note", "General feedback or question", 0xFF64748B) // Slate
}

enum class CommentStatus {
    OPEN,
    RESOLVED,
    REJECTED
}

data class CommentReply(
    val id: String = UUID.randomUUID().toString(),
    val commentId: String,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val authorSpecialty: String,
    val authorAvatarColor: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return sdf.format(Date(createdAt))
    }
}

data class DocumentComment(
    val id: String = UUID.randomUUID().toString(),
    val documentId: Long,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val authorSpecialty: String,
    val authorAvatarColor: Long,
    val selectedText: String = "", // Inline highlighted quote anchor
    val textRangeStart: Int = -1,
    val textRangeEnd: Int = -1,
    val sectionTitle: String = "General Chapter", // e.g. "1.2 Garden Classification System"
    val commentText: String,
    val suggestedReplacement: String = "",
    val commentType: CommentType = CommentType.GENERAL,
    val status: CommentStatus = CommentStatus.OPEN,
    val replies: List<CommentReply> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedBy: String = "",
    val resolvedAt: Long = 0L
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        return sdf.format(Date(createdAt))
    }
}

data class CollaboratorPresence(
    val userId: String,
    val name: String,
    val role: UserRole,
    val specialty: String,
    val avatarColorHex: Long,
    val avatarInitials: String,
    val activeSection: String = "Chapter Overview",
    val isOnline: Boolean = true,
    val isEditing: Boolean = false,
    val lastActiveMillis: Long = System.currentTimeMillis()
)

object PredefinedUsers {
    val ADMIN_SARAH = UserProfile(
        id = "usr_sarah_mitchell",
        name = "Prof. Sarah Mitchell, MD, FACS",
        email = "s.mitchell@documed.edu",
        role = UserRole.ADMIN,
        specialty = "Orthopedic Surgery & Trauma",
        title = "Chief Medical Officer & Academic Chair",
        avatarColorHex = 0xFF7C3AED,
        avatarInitials = "SM",
        statusMessage = "Reviewing Grand Rounds drafts & Chapter permissions",
        isDefault = true
    )

    val EDITOR_ALEX = UserProfile(
        id = "usr_alex_rivera",
        name = "Dr. Alex Rivera, MD",
        email = "a.rivera@documed.edu",
        role = UserRole.EDITOR,
        specialty = "Cardiology & Intensive Care",
        title = "Senior Cardiology Resident",
        avatarColorHex = 0xFF0284C7,
        avatarInitials = "AR",
        statusMessage = "Updating acute STEMI pathway with 2025 ACC guidelines"
    )

    val EDITOR_MARCUS = UserProfile(
        id = "usr_marcus_vance",
        name = "Dr. Marcus Vance, MD, PhD",
        email = "m.vance@documed.edu",
        role = UserRole.EDITOR,
        specialty = "Orthopedic Trauma",
        title = "Associate Clinical Professor",
        avatarColorHex = 0xFF0D9488,
        avatarInitials = "MV",
        statusMessage = "Authoring Garden Classification surgical algorithm"
    )

    val VIEWER_ELENA = UserProfile(
        id = "usr_elena_rostova",
        name = "Dr. Elena Rostova, MD",
        email = "e.rostova@documed.edu",
        role = UserRole.VIEW_ONLY,
        specialty = "Emergency Medicine & Peer Review",
        title = "External Clinical Reviewer",
        avatarColorHex = 0xFFE11D48,
        avatarInitials = "ER",
        statusMessage = "Performing peer review audit on triage protocols"
    )

    val VIEWER_JORDAN = UserProfile(
        id = "usr_jordan_taylor",
        name = "Jordan Taylor, MS4",
        email = "j.taylor@medschool.edu",
        role = UserRole.VIEW_ONLY,
        specialty = "Medical Student",
        title = "Clinical Clerkship Student",
        avatarColorHex = 0xFFEA580C,
        avatarInitials = "JT",
        statusMessage = "Studying hip fracture and cardiac protocols"
    )

    val DEFAULT_USERS = listOf(
        ADMIN_SARAH,
        EDITOR_ALEX,
        EDITOR_MARCUS,
        VIEWER_ELENA,
        VIEWER_JORDAN
    )
}
