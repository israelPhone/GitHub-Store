package zed.rainxch.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import zed.rainxch.core.data.local.db.entities.SeenRepoEntity

@Dao
interface SeenRepoDao {
    @Query("SELECT repoId FROM seen_repos")
    fun getAllSeenRepoIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SeenRepoEntity)

    @Query("DELETE FROM seen_repos")
    suspend fun clearAll()
}
