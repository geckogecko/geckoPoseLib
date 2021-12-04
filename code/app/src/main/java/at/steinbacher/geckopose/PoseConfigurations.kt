package at.steinbacher.geckopose

import at.steinbacher.geckoposelib.data.*
import com.google.mlkit.vision.pose.PoseLandmark

val cyclingConfiguration = listOf(
    GeckoPoseConfiguration(
        tag = "left_pose",
        points = listOf(
            Point(PoseLandmark.LEFT_HIP),
            Point(PoseLandmark.LEFT_KNEE),
            Point(PoseLandmark.LEFT_ANKLE),
            Point(PoseLandmark.LEFT_SHOULDER),
            Point(PoseLandmark.LEFT_ELBOW),
            Point(PoseLandmark.LEFT_WRIST),
        ),
        lines = listOf(
            Line(start = PoseLandmark.LEFT_KNEE, end = PoseLandmark.LEFT_HIP, tag = "knee_hip"),
            Line(start = PoseLandmark.LEFT_ANKLE, end = PoseLandmark.LEFT_KNEE, tag = "knee_ankle"),
            Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.LEFT_SHOULDER, tag = "hip_shoulder"),
            Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_ELBOW, tag = "shoulder_elbow"),
            Line(start = PoseLandmark.LEFT_ELBOW, end = PoseLandmark.LEFT_WRIST, tag = "elbow_wrist"),
        ),
        angles = listOf(
            Angle(startPointType = PoseLandmark.LEFT_HIP, middlePointType = PoseLandmark.LEFT_KNEE,
                endPointType = PoseLandmark.LEFT_ANKLE, tag = "a"),
            Angle(startPointType = PoseLandmark.LEFT_KNEE, middlePointType = PoseLandmark.LEFT_HIP,
                endPointType = PoseLandmark.LEFT_SHOULDER, tag = "b"),
            Angle(startPointType = PoseLandmark.LEFT_HIP, middlePointType = PoseLandmark.LEFT_SHOULDER,
                endPointType = PoseLandmark.LEFT_ELBOW, tag = "c"),
            Angle(startPointType = PoseLandmark.LEFT_SHOULDER, middlePointType = PoseLandmark.LEFT_ELBOW,
                endPointType = PoseLandmark.LEFT_WRIST, tag = "d"),
        ),
    ),
    GeckoPoseConfiguration(
        tag = "right_pose",
        points = listOf(
            Point(PoseLandmark.RIGHT_HIP),
            Point(PoseLandmark.RIGHT_KNEE),
            Point(PoseLandmark.RIGHT_ANKLE),
            Point(PoseLandmark.RIGHT_SHOULDER),
            Point(PoseLandmark.RIGHT_ELBOW),
            Point(PoseLandmark.RIGHT_WRIST),
        ),
        lines = listOf(
            Line(start = PoseLandmark.RIGHT_KNEE, end = PoseLandmark.RIGHT_HIP, tag = "knee_hip"),
            Line(start = PoseLandmark.RIGHT_ANKLE, end = PoseLandmark.RIGHT_KNEE, tag = "knee_ankle"),
            Line(start = PoseLandmark.RIGHT_HIP, end = PoseLandmark.RIGHT_SHOULDER, tag = "hip_shoulder"),
            Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_ELBOW, tag = "shoulder_elbow"),
            Line(start = PoseLandmark.RIGHT_ELBOW, end = PoseLandmark.RIGHT_WRIST, tag = "elbow_wrist"),
        ),
        angles = listOf(
            Angle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_KNEE,
                endPointType = PoseLandmark.RIGHT_ANKLE, tag = "a"),
            Angle(startPointType = PoseLandmark.RIGHT_KNEE, middlePointType = PoseLandmark.RIGHT_HIP,
                endPointType = PoseLandmark.RIGHT_SHOULDER, tag = "b"),
            Angle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_SHOULDER,
                endPointType = PoseLandmark.RIGHT_ELBOW, tag = "c"),
            Angle(startPointType = PoseLandmark.RIGHT_SHOULDER, middlePointType = PoseLandmark.RIGHT_ELBOW,
                endPointType = PoseLandmark.RIGHT_WRIST, tag = "d"),
        ),
    )
)

val poseDrawConfiguration = GeckoPoseDrawConfiguration(
    defaultPointColorLight = R.color.white,
    defaultPointColorDark = R.color.black,
    defaultSelectedPointColor = R.color.red,
    defaultLineColor = R.color.blue,
    defaultAngleColor = R.color.color_angle_ok,
    defaultNOKAngleColor = R.color.color_angle_nok,
)

val tennisConfiguration = listOf(
    GeckoPoseConfiguration(
        tag = "main_pose",
        points = listOf(
            Point(PoseLandmark.LEFT_SHOULDER),
            Point(PoseLandmark.LEFT_ELBOW),
            Point(PoseLandmark.LEFT_WRIST),
            Point(PoseLandmark.RIGHT_SHOULDER),
            Point(PoseLandmark.RIGHT_ELBOW),
            Point(PoseLandmark.RIGHT_WRIST),
            Point(PoseLandmark.LEFT_HIP),
            Point(PoseLandmark.RIGHT_HIP),
            Point(PoseLandmark.LEFT_KNEE),
            Point(PoseLandmark.RIGHT_KNEE),
            Point(PoseLandmark.LEFT_ANKLE),
            Point(PoseLandmark.RIGHT_ANKLE),

            ),
        lines = listOf(
            Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_ELBOW, tag = "left_shoulder_elbow"),
            Line(start = PoseLandmark.LEFT_ELBOW, end = PoseLandmark.LEFT_WRIST, tag = "left_elbow_wrist"),
            Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.RIGHT_SHOULDER, tag = "shoulder_shoulder"),
            Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_ELBOW, tag = "right_shoulder_elbow"),
            Line(start = PoseLandmark.RIGHT_ELBOW, end = PoseLandmark.RIGHT_WRIST, tag = "right_elbow_wrist"),
            Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_HIP, tag = "right_shoulder_hip"),
            Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_HIP, tag = "left_shoulder_hip"),
            Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.LEFT_KNEE, tag = "left_hip_shoulder"),
            Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.RIGHT_HIP, tag = "left_hip_right_hip"),
            Line(start = PoseLandmark.RIGHT_HIP, end = PoseLandmark.RIGHT_KNEE, tag = "right_hip_shoulder"),
            Line(start = PoseLandmark.LEFT_KNEE, end = PoseLandmark.LEFT_ANKLE, tag = "left_knee_ankle"),
            Line(start = PoseLandmark.RIGHT_KNEE, end = PoseLandmark.RIGHT_ANKLE, tag = "right_knee_ankle"),
        ),
        angles = listOf(
            Angle(startPointType = PoseLandmark.LEFT_SHOULDER, middlePointType = PoseLandmark.LEFT_ELBOW,
                endPointType = PoseLandmark.LEFT_WRIST, tag = "left_a"),
            Angle(startPointType = PoseLandmark.RIGHT_SHOULDER, middlePointType = PoseLandmark.RIGHT_ELBOW,
                endPointType = PoseLandmark.RIGHT_WRIST, tag = "right_a"),
            Angle(startPointType = PoseLandmark.LEFT_ELBOW, middlePointType = PoseLandmark.LEFT_SHOULDER,
                endPointType = PoseLandmark.LEFT_HIP, tag = "left_b"),
            Angle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_SHOULDER,
                endPointType = PoseLandmark.RIGHT_ELBOW, tag = "right_b"),
            Angle(startPointType = PoseLandmark.LEFT_SHOULDER, middlePointType = PoseLandmark.LEFT_HIP,
                endPointType = PoseLandmark.LEFT_KNEE, tag = "left_c"),
            Angle(startPointType = PoseLandmark.RIGHT_KNEE, middlePointType = PoseLandmark.RIGHT_HIP,
                endPointType = PoseLandmark.RIGHT_SHOULDER, tag = "right_c"),
            Angle(startPointType = PoseLandmark.LEFT_ANKLE, middlePointType = PoseLandmark.LEFT_KNEE,
                endPointType = PoseLandmark.LEFT_HIP, tag = "left_d"),
            Angle(startPointType = PoseLandmark.RIGHT_ANKLE, middlePointType = PoseLandmark.RIGHT_KNEE,
                endPointType = PoseLandmark.RIGHT_HIP, tag = "right_d"),
        ),
        poseCenterPointsTargets = listOf(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
    ),
    GeckoPoseConfiguration(
        tag = "main_pose",
        points = listOf(
            Point(PoseLandmark.LEFT_SHOULDER),
            Point(PoseLandmark.LEFT_ELBOW),
            Point(PoseLandmark.LEFT_WRIST),
            Point(PoseLandmark.RIGHT_SHOULDER),
            Point(PoseLandmark.RIGHT_ELBOW),
            Point(PoseLandmark.RIGHT_WRIST),
            Point(PoseLandmark.LEFT_HIP),
            Point(PoseLandmark.RIGHT_HIP),
            Point(PoseLandmark.LEFT_KNEE),
            Point(PoseLandmark.RIGHT_KNEE),
            Point(PoseLandmark.LEFT_ANKLE),
            Point(PoseLandmark.RIGHT_ANKLE),

            ),
        lines = listOf(
            Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_ELBOW, tag = "left_shoulder_elbow"),
            Line(start = PoseLandmark.LEFT_ELBOW, end = PoseLandmark.LEFT_WRIST, tag = "left_elbow_wrist"),
            Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.RIGHT_SHOULDER, tag = "shoulder_shoulder"),
            Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_ELBOW, tag = "right_shoulder_elbow"),
            Line(start = PoseLandmark.RIGHT_ELBOW, end = PoseLandmark.RIGHT_WRIST, tag = "right_elbow_wrist"),
            Line(start = PoseLandmark.RIGHT_SHOULDER, end = PoseLandmark.RIGHT_HIP, tag = "right_shoulder_hip"),
            Line(start = PoseLandmark.LEFT_SHOULDER, end = PoseLandmark.LEFT_HIP, tag = "left_shoulder_hip"),
            Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.LEFT_KNEE, tag = "left_hip_shoulder"),
            Line(start = PoseLandmark.LEFT_HIP, end = PoseLandmark.RIGHT_HIP, tag = "left_hip_right_hip"),
            Line(start = PoseLandmark.RIGHT_HIP, end = PoseLandmark.RIGHT_KNEE, tag = "right_hip_shoulder"),
            Line(start = PoseLandmark.LEFT_KNEE, end = PoseLandmark.LEFT_ANKLE, tag = "left_knee_ankle"),
            Line(start = PoseLandmark.RIGHT_KNEE, end = PoseLandmark.RIGHT_ANKLE, tag = "right_knee_ankle"),
        ),
        angles = listOf(
            Angle(startPointType = PoseLandmark.LEFT_SHOULDER, middlePointType = PoseLandmark.LEFT_ELBOW,
                endPointType = PoseLandmark.LEFT_WRIST, tag = "left_a"),
            Angle(startPointType = PoseLandmark.RIGHT_SHOULDER, middlePointType = PoseLandmark.RIGHT_ELBOW,
                endPointType = PoseLandmark.RIGHT_WRIST, tag = "right_a"),
            Angle(startPointType = PoseLandmark.LEFT_ELBOW, middlePointType = PoseLandmark.LEFT_SHOULDER,
                endPointType = PoseLandmark.LEFT_HIP, tag = "left_b"),
            Angle(startPointType = PoseLandmark.RIGHT_HIP, middlePointType = PoseLandmark.RIGHT_SHOULDER,
                endPointType = PoseLandmark.RIGHT_ELBOW, tag = "right_b"),
            Angle(startPointType = PoseLandmark.LEFT_SHOULDER, middlePointType = PoseLandmark.LEFT_HIP,
                endPointType = PoseLandmark.LEFT_KNEE, tag = "left_c"),
            Angle(startPointType = PoseLandmark.RIGHT_KNEE, middlePointType = PoseLandmark.RIGHT_HIP,
                endPointType = PoseLandmark.RIGHT_SHOULDER, tag = "right_c"),
            Angle(startPointType = PoseLandmark.LEFT_ANKLE, middlePointType = PoseLandmark.LEFT_KNEE,
                endPointType = PoseLandmark.LEFT_HIP, tag = "left_d"),
            Angle(startPointType = PoseLandmark.RIGHT_ANKLE, middlePointType = PoseLandmark.RIGHT_KNEE,
                endPointType = PoseLandmark.RIGHT_HIP, tag = "right_d"),
        ),
        poseCenterPointsTargets = listOf(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
    )
)