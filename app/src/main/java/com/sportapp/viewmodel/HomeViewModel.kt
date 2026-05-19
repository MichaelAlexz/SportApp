package com.sportapp.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sportapp.data.*
import com.sportapp.data.SportRepository.WeeklyRanking
import com.sportapp.service.StepCounterService
import com.sportapp.service.TrackingService
import com.sportapp.util.CalorieCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getInstance(application)
    private val repository = SportRepository(db)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // ─── 实时状态 ───

    /** 今日步数 */
    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps.asStateFlow()

    /** 今日步数目标进度 (0~1) */
    val todayStepProgress: StateFlow<Float> = _todaySteps.map { (it / 10000f).coerceAtMost(1f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    /** 今日运动数据 */
    private val _todayWorkoutDuration = MutableStateFlow(0L)
    val todayWorkoutDuration: StateFlow<Long> = _todayWorkoutDuration.asStateFlow()

    private val _todayCalories = MutableStateFlow(0f)
    val todayCalories: StateFlow<Float> = _todayCalories.asStateFlow()

    /** 活跃分钟（步数估算） */
    val activeMinutes: StateFlow<Float> = _todaySteps.map { steps ->
        (steps / 80f).coerceAtMost(300f) // 约80步/分钟
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    /** 今日步数估算距离 */
    val todayDistanceFromSteps: StateFlow<Float> = _todaySteps.map { steps ->
        CalorieCalculator.estimateDistanceFromSteps(steps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    /** 今日步数估算卡路里 */
    val todayCaloriesFromSteps: StateFlow<Float> = _todaySteps.map { steps ->
        CalorieCalculator.calculateFromSteps(steps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    /** 连续打卡天数 */
    private val _streakDays = MutableStateFlow(7)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    // ─── 运动追踪状态 ───

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _trackingType = MutableStateFlow("running")
    val trackingType: StateFlow<String> = _trackingType.asStateFlow()

    private val _trackingDistance = MutableStateFlow(0f)
    val trackingDistance: StateFlow<Float> = _trackingDistance.asStateFlow()

    private val _trackingDuration = MutableStateFlow(0L)
    val trackingDuration: StateFlow<Long> = _trackingDuration.asStateFlow()

    private val _trackingCalories = MutableStateFlow(0f)
    val trackingCalories: StateFlow<Float> = _trackingCalories.asStateFlow()

    private val _trackingPace = MutableStateFlow(0)
    val trackingPace: StateFlow<Int> = _trackingPace.asStateFlow()

    private val _trackingRouteJson = MutableStateFlow("")
    val trackingRouteJson: StateFlow<String> = _trackingRouteJson.asStateFlow()

    // ─── 排行榜 ───
    private val _weeklyRankings = MutableStateFlow<List<WeeklyRanking>>(emptyList())
    val weeklyRankings: StateFlow<List<WeeklyRanking>> = _weeklyRankings.asStateFlow()

    // ─── 月度目标 ───
    private val _monthlyGoalProgress = MutableStateFlow(0f)
    val monthlyGoalProgress: StateFlow<Float> = _monthlyGoalProgress.asStateFlow()

    private val _monthlyGoalTarget = MutableStateFlow(100f)
    val monthlyGoalTarget: StateFlow<Float> = _monthlyGoalTarget.asStateFlow()

    private val _monthlyGoalValue = MutableStateFlow(0f)
    val monthlyGoalValue: StateFlow<Float> = _monthlyGoalValue.asStateFlow()

    // ─── 运动记录 ───
    private val _recentWorkouts = MutableStateFlow<List<WorkoutRecord>>(emptyList())
    val recentWorkouts: StateFlow<List<WorkoutRecord>> = _recentWorkouts.asStateFlow()

    init {
        // 1. 从数据库读取持久化数据
        observeDatabase()

        // 2. 注册计步器传感器
        registerStepSensor()

        // 3. 注册广播接收器
        registerBroadcastReceivers(application)

        // 4. 初始化数据库默认数据
        viewModelScope.launch {
            repository.initMonthlyGoal()
        }
    }

    private fun observeDatabase() {
        // 观察步数
        viewModelScope.launch {
            repository.observeTodaySteps().collect { record ->
                _todaySteps.value = record?.totalSteps ?: 0
            }
        }

        // 观察今日运动时长
        viewModelScope.launch {
            repository.observeTodayDuration().collect { duration ->
                _todayWorkoutDuration.value = duration ?: 0L
            }
        }

        // 观察今日卡路里
        viewModelScope.launch {
            repository.observeTodayCalories().collect { cal ->
                _todayCalories.value = cal ?: 0f
            }
        }

        // 观察月度目标
        viewModelScope.launch {
            repository.observeMonthlyGoal().collect { goal ->
                if (goal != null) {
                    _monthlyGoalTarget.value = goal.targetValue
                    _monthlyGoalValue.value = goal.currentValue
                    _monthlyGoalProgress.value = (goal.currentValue / goal.targetValue).coerceAtMost(1f)
                }
            }
        }

        // 观察周排行
        viewModelScope.launch {
            repository.observeWeekDistance().collect { distance ->
                _weeklyRankings.value = repository.getWeeklyRankings(
                    currentUserDistance = (distance ?: 0f) / 1000f
                )
            }
        }

        // 观察运动记录
        viewModelScope.launch {
            repository.observeAllWorkouts().collect { workouts ->
                _recentWorkouts.value = workouts
            }
        }
    }

    // ─── 计步器 ───

    private fun registerStepSensor() {
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        stepSensor?.let { sensor ->
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val steps = event.values[0].toInt()
        // 通过 ViewModel 更新步数（周期性同步到 DB）
        _todaySteps.value = steps.coerceAtLeast(_todaySteps.value)
        viewModelScope.launch {
            repository.updateTodaySteps(_todaySteps.value)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ─── GPS 运动追踪 ───

    fun startTracking(type: String, context: Context) {
        _isTracking.value = true
        _trackingType.value = type
        _trackingDistance.value = 0f
        _trackingDuration.value = 0L
        _trackingCalories.value = 0f
        _trackingPace.value = 0
        _trackingRouteJson.value = ""

        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
            putExtra(TrackingService.EXTRA_WORKOUT_TYPE, type)
        }
        context.startForegroundService(intent)
    }

    fun pauseTracking(context: Context) {
        context.startService(Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_PAUSE
        })
    }

    fun resumeTracking(context: Context) {
        context.startService(Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_RESUME
        })
    }

    fun stopTracking(context: Context) {
        _isTracking.value = false

        // 保存运动记录到数据库
        viewModelScope.launch {
            val record = WorkoutRecord(
                type = _trackingType.value,
                startTimeMs = System.currentTimeMillis() - _trackingDuration.value * 1000,
                endTimeMs = System.currentTimeMillis(),
                durationSeconds = _trackingDuration.value,
                distanceMeters = _trackingDistance.value,
                caloriesBurned = _trackingCalories.value,
                avgPaceSeconds = _trackingPace.value,
                routePoints = _trackingRouteJson.value
            )
            val recordId = repository.saveWorkout(record)

            // 更新月度目标
            repository.updateMonthlyGoal(_trackingDistance.value)
        }

        context.startService(Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        })
    }

    // ─── 广播接收 ───

    private fun registerBroadcastReceivers(application: Application) {
        val filter = IntentFilter(TrackingService.BROADCAST_LOCATION)
        LocalBroadcastManager.getInstance(application)
            .registerReceiver(trackingReceiver, filter)
    }

    private val trackingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == TrackingService.BROADCAST_LOCATION) {
                _trackingDistance.value = intent.getFloatExtra(TrackingService.EXTRA_DISTANCE, 0f)
                _trackingDuration.value = intent.getLongExtra(TrackingService.EXTRA_DURATION, 0)
                _trackingCalories.value = intent.getFloatExtra(TrackingService.EXTRA_CALORIES, 0f)
                _trackingPace.value = intent.getIntExtra(TrackingService.EXTRA_PACE, 0)
                _trackingRouteJson.value = intent.getStringExtra(TrackingService.EXTRA_LOCATIONS_JSON) ?: ""
            }
        }
    }

    // ─── 格式化辅助 ───

    fun formatDistance(meters: Float): String {
        return CalorieCalculator.formatDistance(meters)
    }

    fun formatCalories(calories: Float): String {
        return CalorieCalculator.formatCalories(calories)
    }

    fun formatPace(paceSeconds: Int): String {
        return CalorieCalculator.formatPace(paceSeconds)
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        try {
            getApplication<android.app.Application>().let {
                LocalBroadcastManager.getInstance(it).unregisterReceiver(trackingReceiver)
            }
        } catch (_: Exception) {}
    }
}
