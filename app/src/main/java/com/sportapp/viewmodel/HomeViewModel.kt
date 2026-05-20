package com.sportapp.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sportapp.data.*
import com.sportapp.data.SportRepository.WeeklyRanking
import com.sportapp.service.TrackingService
import com.sportapp.util.CalorieCalculator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class HomeViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getInstance(application)
    private val repository = SportRepository(db)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    companion object {
        private const val TAG = "HomeViewModel"
        private const val STEPS_GOAL = 10000
        private const val FALLBACK_INTERVAL_MS = 2000L  // 模拟步数间隔
    }

    // ─── 传感器状态 ───
    private var stepCounterBase = -1L         // TYPE_STEP_COUNTER 基数（设备总步数）
    private var stepDetectorCount = 0          // TYPE_STEP_DETECTOR 累计
    private var sensorType = -1                // 使用的传感器类型
    private var fallbackJob: Job? = null       // 模拟计步器任务

    // ─── 实时状态 ───
    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps.asStateFlow()

    val todayStepProgress: StateFlow<Float> = _todaySteps.map { (it / STEPS_GOAL.toFloat()).coerceAtMost(1f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    private val _todayWorkoutDuration = MutableStateFlow(0L)
    val todayWorkoutDuration: StateFlow<Long> = _todayWorkoutDuration.asStateFlow()

    private val _todayCalories = MutableStateFlow(0f)
    val todayCalories: StateFlow<Float> = _todayCalories.asStateFlow()

    val activeMinutes: StateFlow<Float> = _todaySteps.map { steps ->
        (steps / 80f).coerceAtMost(300f)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    val todayDistanceFromSteps: StateFlow<Float> = _todaySteps.map { steps ->
        CalorieCalculator.estimateDistanceFromSteps(steps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    val todayCaloriesFromSteps: StateFlow<Float> = _todaySteps.map { steps ->
        CalorieCalculator.calculateFromSteps(steps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0f)

    private val _streakDays = MutableStateFlow(7)
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    // ─── GPS 追踪状态 ───
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

    // ─── 排行 & 目标 ───
    private val _weeklyRankings = MutableStateFlow<List<WeeklyRanking>>(emptyList())
    val weeklyRankings: StateFlow<List<WeeklyRanking>> = _weeklyRankings.asStateFlow()

    private val _monthlyGoalProgress = MutableStateFlow(0f)
    val monthlyGoalProgress: StateFlow<Float> = _monthlyGoalProgress.asStateFlow()

    private val _monthlyGoalTarget = MutableStateFlow(100f)
    val monthlyGoalTarget: StateFlow<Float> = _monthlyGoalTarget.asStateFlow()

    private val _monthlyGoalValue = MutableStateFlow(0f)
    val monthlyGoalValue: StateFlow<Float> = _monthlyGoalValue.asStateFlow()

    init {
        observeDatabase()
        startStepCounter()
        viewModelScope.launch { repository.initMonthlyGoal() }
    }

    // ═══════════════════════════════════════════
    //  计步器（传感器 + 模拟回退）
    // ═══════════════════════════════════════════

    private fun startStepCounter() {
        // 优先使用 TYPE_STEP_COUNTER（累计步数传感器）
        val counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (counter != null) {
            sensorType = Sensor.TYPE_STEP_COUNTER
            sensorManager.registerListener(this, counter, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "使用 TYPE_STEP_COUNTER 传感器")
            return
        }

        // 备选：TYPE_STEP_DETECTOR（逐歩检测）
        val detector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (detector != null) {
            sensorType = Sensor.TYPE_STEP_DETECTOR
            sensorManager.registerListener(this, detector, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "使用 TYPE_STEP_DETECTOR 传感器")
            return
        }

        // 没有传感器：启动模拟计步器（仅用于演示）
        Log.w(TAG, "没有计步传感器，启动模拟计步器")
        startFallbackStepCounter()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentSteps = event.values[0].toLong()

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                // TYPE_STEP_COUNTER 返回的是设备总步数（自开机以来）
                // 需要记录基数，计算差值
                if (stepCounterBase < 0) {
                    stepCounterBase = currentSteps
                    // 从数据库恢复今日步数
                    viewModelScope.launch {
                        val savedSteps = repository.getTodaySteps()
                        if (savedSteps > 0) {
                            _todaySteps.value = savedSteps
                        }
                    }
                } else {
                    val delta = (currentSteps - stepCounterBase).toInt()
                    if (delta > _todaySteps.value) {
                        _todaySteps.value = delta
                        syncStepsToDb(delta)
                    }
                }
            }

            Sensor.TYPE_STEP_DETECTOR -> {
                // 每检测到一步就 +1
                stepDetectorCount++
                _todaySteps.value = stepDetectorCount
                syncStepsToDb(stepDetectorCount)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun syncStepsToDb(steps: Int) {
        viewModelScope.launch { repository.updateTodaySteps(steps) }
    }

    /**
     * 模拟计步器（无传感器时使用手机加速度传感器或定时器）
     * 这里使用定时器模拟，实际产品中应使用加速度传感器
     */
    private fun startFallbackStepCounter() {
        // 先从数据库恢复
        viewModelScope.launch {
            val saved = repository.getTodaySteps()
            _todaySteps.value = saved
        }

        fallbackJob = viewModelScope.launch(Dispatchers.Default) {
            // 每2秒随机增加步数，模拟走动
            while (isActive) {
                delay(FALLBACK_INTERVAL_MS)
                val increment = (1..5).random()
                val newSteps = _todaySteps.value + increment
                _todaySteps.value = newSteps
                syncStepsToDb(newSteps)
            }
        }
    }

    // ═══════════════════════════════════════════
    //  数据库观察
    // ═══════════════════════════════════════════

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.observeTodayDuration().collect { duration ->
                _todayWorkoutDuration.value = duration ?: 0L
            }
        }
        viewModelScope.launch {
            repository.observeTodayCalories().collect { cal ->
                _todayCalories.value = cal ?: 0f
            }
        }
        viewModelScope.launch {
            repository.observeMonthlyGoal().collect { goal ->
                if (goal != null) {
                    _monthlyGoalTarget.value = goal.targetValue
                    _monthlyGoalValue.value = goal.currentValue
                    _monthlyGoalProgress.value = (goal.currentValue / goal.targetValue).coerceAtMost(1f)
                }
            }
        }
        viewModelScope.launch {
            repository.observeWeekDistance().collect { distance ->
                _weeklyRankings.value = repository.getWeeklyRankings(
                    currentUserDistance = (distance ?: 0f) / 1000f
                )
            }
        }
    }

    // ═══════════════════════════════════════════
    //  GPS 运动追踪
    // ═══════════════════════════════════════════

    fun startTracking(type: String, context: Context) {
        _isTracking.value = true
        _trackingType.value = type
        _trackingDistance.value = 0f
        _trackingDuration.value = 0L
        _trackingCalories.value = 0f
        _trackingPace.value = 0

        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
            putExtra(TrackingService.EXTRA_WORKOUT_TYPE, type)
        }
        try {
            context.startForegroundService(intent)
        } catch (e: Exception) {
            context.startService(intent)
        }
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

        viewModelScope.launch {
            val record = WorkoutRecord(
                type = _trackingType.value,
                startTimeMs = System.currentTimeMillis() - _trackingDuration.value * 1000,
                endTimeMs = System.currentTimeMillis(),
                durationSeconds = _trackingDuration.value,
                distanceMeters = _trackingDistance.value,
                caloriesBurned = _trackingCalories.value,
                avgPaceSeconds = _trackingPace.value,
                routePoints = ""  // GPS轨迹点暂不序列化
            )
            repository.saveWorkout(record)
            repository.updateMonthlyGoal(_trackingDistance.value)
        }

        context.startService(Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        })
    }

    // ═══════════════════════════════════════════
    //  TrackingService 广播接收
    // ═══════════════════════════════════════════

    fun onTrackingUpdate(intent: Intent) {
        if (intent.action == TrackingService.BROADCAST_LOCATION) {
            _trackingDistance.value = intent.getFloatExtra(TrackingService.EXTRA_DISTANCE, 0f)
            _trackingDuration.value = intent.getLongExtra(TrackingService.EXTRA_DURATION, 0)
            _trackingCalories.value = intent.getFloatExtra(TrackingService.EXTRA_CALORIES, 0f)
            _trackingPace.value = intent.getIntExtra(TrackingService.EXTRA_PACE, 0)
        }
    }

    // ═══════════════════════════════════════════
    //  格式化
    // ═══════════════════════════════════════════

    fun formatDistance(meters: Float): String = CalorieCalculator.formatDistance(meters)
    fun formatCalories(calories: Float): String = CalorieCalculator.formatCalories(calories)
    fun formatPace(paceSeconds: Int): String = CalorieCalculator.formatPace(paceSeconds)

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    override fun onCleared() {
        super.onCleared()
        fallbackJob?.cancel()
        try { sensorManager.unregisterListener(this) } catch (_: Exception) {}
    }
}
