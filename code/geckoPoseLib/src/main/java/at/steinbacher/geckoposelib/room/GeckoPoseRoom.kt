package at.steinbacher.geckoposelib.room

import androidx.room.*
import at.steinbacher.geckoposelib.GeckoPose
import at.steinbacher.geckoposelib.PoseVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName = "gecko_poses_table")
class GeckoPoseEntity(
    @PrimaryKey var id: Int,
    val geckoPose: GeckoPose,
    val timestamp: Long,
    val isUserData: Boolean,
    val tag: String?
)

@Dao
interface GeckoPoseDao {

    @Insert
    suspend fun insert(geckoPoseEntity: GeckoPoseEntity)

    @Query("SELECT * FROM gecko_poses_table")
    fun getAll(): Flow<GeckoPoseEntity>

    @Query("SELECT * FROM gecko_poses_table WHERE isUserData = :isUserData")
    fun getAllByUserData(isUserData: Boolean): Flow<GeckoPoseEntity>

    @Query("SELECT * FROM gecko_poses_table WHERE tag LIKE :tag LIMIT 1")
    fun getByTag(tag: String): GeckoPoseEntity
}

class GeckoPoseTypeConverter {

    @TypeConverter
    fun geckoPoseToString(geckoPose: GeckoPose): String {
        return Json.encodeToString(GeckoPose.serializer(), geckoPose)
    }

    @TypeConverter
    fun stringToGeckoPose(geckoPoseString: String): GeckoPose {
        return Json.decodeFromString(GeckoPose.serializer(), geckoPoseString)
    }
}