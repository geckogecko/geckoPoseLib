package at.steinbacher.geckoposelib.room

import androidx.room.*
import at.steinbacher.geckoposelib.GeckoPose
import at.steinbacher.geckoposelib.PoseVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName = "pose_video_table")
class PoseVideoEntity(
    @PrimaryKey var id: Int,
    val PoseVideo: PoseVideo,
    val timestamp: Long,
    val isUserData: Boolean,
    val tag: String?
)

@Dao
interface PoseVideoDao {
    @Insert
    suspend fun insert(geckoPoseEntity: PoseVideoEntity)

    @Query("SELECT * FROM pose_video_table")
    fun getAll(): Flow<PoseVideoEntity>

    @Query("SELECT * FROM pose_video_table WHERE isUserData = :isUserData")
    fun getAllByUserData(isUserData: Boolean): Flow<PoseVideoEntity>

    @Query("SELECT * FROM pose_video_table WHERE tag LIKE :tag LIMIT 1")
    fun getByTag(tag: String): PoseVideoEntity
}

class PoseVideoTypeConverter {

    @TypeConverter
    fun geckoPoseToString(geckoPose: GeckoPose): String {
        return Json.encodeToString(GeckoPose.serializer(), geckoPose)
    }

    @TypeConverter
    fun stringToGeckoPose(geckoPoseString: String): GeckoPose {
        return Json.decodeFromString(GeckoPose.serializer(), geckoPoseString)
    }
}