package zed.rainxch.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SeenReposRepository {
    fun getAllSeenRepoIds(): Flow<Set<Long>>

    suspend fun markAsSeen(repoId: Long)

    suspend fun clearAll()
}
