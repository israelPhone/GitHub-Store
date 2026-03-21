package zed.rainxch.core.data.local.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "seen_repos")
data class SeenRepoEntity(
    @PrimaryKey
    val repoId: Long,
    val seenAt: Long,
)
