package at.steinbacher.geckoposelib.room

import androidx.room.*
import at.steinbacher.geckoposelib.data.GeckoPose
import at.steinbacher.geckoposelib.data.PoseVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

@Entity(tableName = "pose_video_table")
class PoseVideoEntity(
    val PoseVideo: PoseVideo,
    val timestamp: Long,
    val isUserData: Boolean,
    val tag: String?
) {
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}

@Dao
interface PoseVideoDao {
    @Insert
    fun insert(geckoPoseEntity: PoseVideoEntity)

    @Query("SELECT * FROM pose_video_table")
    fun getAll(): Flow<List<PoseVideoEntity>>

    @Query("SELECT * FROM pose_video_table WHERE isUserData = :isUserData")
    fun getAllByUserData(isUserData: Boolean): Flow<List<PoseVideoEntity>>

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