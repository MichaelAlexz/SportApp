package com.sportapp.data

import com.sportapp.util.CalorieCalculator
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

/**
 * 运动数据仓库
 * 统一管理数据库访问、数据聚合和格式化
 */
class SportRepository(private val db: AppDatabase) {

    private val workoutDao = db.workoutDao()
    private val stepDao = db.stepDao()
    private val goalDao = db.goalDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    // ───── 今日数据 ─────

    /** 获取今日步数记录 */
    suspend fun getTodaySteps(): Int {
        val today = dateFormat.format(Date())
        return stepDao.getStepRecord(today)?.totalSteps ?: 0
    }

    /** 更新今日步数 */
    suspend fun updateTodaySteps(steps: Int) {
        val today = dateFormat.format(Date())
        val record = stepDao.getStepRecord(today) ?: StepRecord(date = today)
        stepDao.upsert(record.copy(totalSteps = steps))
    }

    /** 观察今日步数 */
    fun observeTodaySteps(): Flow<StepRecord?> {
        val today = dateFormat.format(Date())
        return stepDao.observeStepRecord(today)
    }

    /** 获取一周步数 */
    fun observeWeekSteps(): Flow<List<StepRecord>> {
        return stepDao.getWeekSteps()
    }

    // ───── 运动记录 ─────

    /** 插入运动记录 */
    suspend fun saveWorkout(record: WorkoutRecord): Long {
        return workoutDao.insert(record)
    }

    /** 更新运动记录 */
    suspend fun updateWorkout(record: WorkoutRecord) {
        workoutDao.update(record)
    }

    /** 获取所有运动记录 */
    fun observeAllWorkouts(): Flow<List<WorkoutRecord>> {
        return workoutDao.getAllWorkouts()
    }

    /** 获取今日开始的时间戳（毫秒） */
    private fun getTodayStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** 获取本月开始的时间戳 */
    private fun getMonthStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** 获取本周开始的时间戳（周一） */
    private fun getWeekStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // ───── 今日聚合数据 ─────

    /** 今日运动时长（秒） */
    fun observeTodayDuration(): Flow<Long?> {
        return workoutDao.getTotalDurationSince(getTodayStartMs())
    }

    /** 今日燃脂 */
    fun observeTodayCalories(): Flow<Float?> {
        return workoutDao.getTotalCaloriesSince(getTodayStartMs())
    }

    /** 今日运动次数 */
    fun observeTodayWorkoutCount(): Flow<Int?> {
        return workoutDao.getWorkoutCountSince(getTodayStartMs())
    }

    /** 本周总里程 */
    fun observeWeekDistance(): Flow<Float?> {
        return workoutDao.getTotalDistanceSince(getWeekStartMs())
    }

    // ───── 月度目标 ─────

    /** 获取本月跑步总里程 */
    fun observeMonthRunningDistance(): Flow<Float?> {
        return workoutDao.getTotalDistanceSince(getMonthStartMs())
    }

    /** 初始化/更新月度目标 */
    suspend fun initMonthlyGoal() {
        val monthStart = getMonthStartMs()
        val cal = Calendar.getInstance()
        cal.timeInMillis = monthStart
        cal.add(Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis

        val existing = goalDao.getGoal("monthly_run")
        if (existing == null) {
            goalDao.upsert(
                GoalEntity(
                    id = "monthly_run",
                    title = "本月跑步目标",
                    targetValue = 100f,
                    currentValue = 0f,
                    periodStartMs = monthStart,
                    periodEndMs = monthEnd
                )
            )
        }
    }

    /** 观察月度目标 */
    fun observeMonthlyGoal() = goalDao.observeGoal("monthly_run")

    /** 更新月度目标进度 */
    suspend fun updateMonthlyGoal(distanceMeters: Float) {
        goalDao.addProgress("monthly_run", distanceMeters / 1000f)
    }

    // ───── 排行榜数据 ─────
    data class WeeklyRanking(
        val name: String,
        val distanceKm: Float,
        val streakDays: Int,
        val avatarColor: Long,
        val isMe: Boolean = false
    )

    /** 获取周排行数据（当前用户 + 模拟好友） */
    fun getWeeklyRankings(currentUserDistance: Float): List<WeeklyRanking> {
        return listOf(
            WeeklyRanking("张小雅", 58.2f, 12, 0xFFFFD54F),
            WeeklyRanking("王大勇", 42.6f, 8, 0xFFC0C0C5),
            WeeklyRanking("李明轩", 36.8f, 5, 0xFFCD7F32),
            WeeklyRanking("陈雨桐", currentUserDistance, 7, 0xFF5BC0B8, isMe = true),
            WeeklyRanking("赵小萌", 21.0f, 3, 0xFFFF6B6B)
        )
    }
}
