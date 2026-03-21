package zed.rainxch.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import zed.rainxch.core.data.local.db.dao.SeenRepoDao
import zed.rainxch.core.data.local.db.entities.SeenRepoEntity
import zed.rainxch.core.domain.repository.SeenReposRepository

class SeenReposRepositoryImpl(
    private val seenRepoDao: SeenRepoDao,
) : SeenReposRepository {
    override fun getAllSeenRepoIds(): Flow<Set<Long>> =
        seenRepoDao.getAllSeenRepoIds().map { it.toSet() }

    override suspend fun markAsSeen(repoId: Long) {
        seenRepoDao.insert(
            SeenRepoEntity(
                repoId = repoId,
                seenAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun clearAll() {
        seenRepoDao.clearAll()
    }
}
