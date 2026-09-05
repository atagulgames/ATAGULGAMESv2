package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.HarmonicAudioEngine
import com.example.data.EchoPreferences
import com.example.data.EchoRepository
import com.example.data.LevelCatalog
import com.example.data.local.LevelEntity
import com.example.game.CollisionEngine
import com.example.localization.Language
import com.example.model.DirectedEdge
import com.example.model.EchoStroke
import com.example.model.EchoTheme
import com.example.model.GameStatus
import com.example.model.LevelData
import com.example.model.LevelNode
import com.example.model.Node
import com.example.model.NodeType
import com.example.model.Point
import com.example.model.ScreenState
import com.example.model.Segment
import com.example.model.StrokeTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RewardClaimedInfo(
    val title: String,
    val subtitle: String,
    val rewardType: String,
    val tokensAdded: Int = 0,
    val breakersAdded: Int = 0
)

data class EchoUiState(
    val screenState: ScreenState = ScreenState.INTRO,
    val currentLevelIndex: Int = 0,
    val level: LevelData = LevelCatalog.entityToLevelData(LevelCatalog.create250Levels()[0]),
    val allLevels: List<LevelEntity> = emptyList(),
    val language: Language = Language.EN,
    val nodes: List<Node> = emptyList(),
    val visitedNodeIds: List<Int> = emptyList(),
    val collectedKeyIds: Set<Int> = emptySet(),
    val currentStrokeSegments: List<Segment> = emptyList(),
    val currentPointerPos: Point? = null,
    val isDrawing: Boolean = false,
    val echoes: List<EchoStroke> = emptyList(),
    val echoCountForLevel: Int = 0,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val tokens: Int = 5,
    val echoBreakers: Int = 2,
    val isEchoShrinkerActive: Boolean = false,
    val isAdFree: Boolean = false,
    val totalEchoes: Int = 0,
    val completedLevels: Set<Int> = emptySet(),
    val totalStars: Int = 0,
    val isCollisionAlertActive: Boolean = false,
    val isProximityAlertActive: Boolean = false,
    val isHintActive: Boolean = false,
    val isRewardedSimulating: Boolean = false,
    val rewardSimulatingType: String = "CLEAR_ECHOES",
    val rewardCountdownSeconds: Int = 3,
    val isShopDialogVisible: Boolean = false,
    val isLevelSelectVisible: Boolean = false,
    val isSettingsDialogVisible: Boolean = false,
    val isSkinsDialogVisible: Boolean = false,
    val isDailyChallenge: Boolean = false,
    val isDailyCompletedToday: Boolean = false,
    val strokeTheme: StrokeTheme = StrokeTheme.NEON_CYAN,
    val echoTheme: EchoTheme = EchoTheme.ELECTRIC_RED,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val testAdsEnabled: Boolean = false,
    val isAdLoading: Boolean = false,
    val isAdBlockerDetected: Boolean = false,
    val isCheckingAdBlocker: Boolean = false,
    val rewardClaimedData: RewardClaimedInfo? = null,
    val toastMessage: String? = null
)

class EchoGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = EchoPreferences(application)
    private val repo = EchoRepository(application)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _uiState = MutableStateFlow(EchoUiState())
    val uiState: StateFlow<EchoUiState> = _uiState.asStateFlow()

    private var alertResetJob: Job? = null
    private var rewardJob: Job? = null
    private var toastResetJob: Job? = null
    private var ghostFadeJob: Job? = null

    private val nodeHitRadius: Float = 34f
    private val deadlockThreshold: Int = 5

    init {
        HarmonicAudioEngine.init(application)
        HarmonicAudioEngine.isSoundEnabled = prefs.soundEnabled

        viewModelScope.launch {
            repo.ensureLevelsPopulated()
            if (!prefs.hasFreshStartV2) {
                repo.resetAllProgress()
                prefs.resetAllProgress()
                prefs.hasFreshStartV2 = true
                loadSavedPreferences()
            }
        }

        viewModelScope.launch {
            repo.getAllLevels().collect { list ->
                _uiState.update { state ->
                    val completed = list.filter { it.isCompleted }.map { it.id - 1 }.toSet()
                    val stars = list.sumOf { it.stars }
                    state.copy(
                        allLevels = list,
                        completedLevels = completed,
                        totalStars = stars
                    )
                }
            }
        }

        loadSavedPreferences()
        checkAdBlocker()
    }

    private fun loadSavedPreferences() {
        val stroke = try { StrokeTheme.valueOf(prefs.strokeThemeName) } catch (_: Exception) { StrokeTheme.NEON_CYAN }
        val echo = try { EchoTheme.valueOf(prefs.echoThemeName) } catch (_: Exception) { EchoTheme.ELECTRIC_RED }
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val isDailyDone = prefs.lastDailyCompletedDate == todayStr

        _uiState.update {
            it.copy(
                currentLevelIndex = prefs.currentLevelIndex.coerceIn(0, 249),
                language = Language.fromCode(prefs.languageCode),
                tokens = prefs.tokens,
                echoBreakers = prefs.echoBreakers,
                isAdFree = prefs.isAdFree,
                totalEchoes = prefs.totalEchoes,
                strokeTheme = stroke,
                echoTheme = echo,
                soundEnabled = prefs.soundEnabled,
                hapticsEnabled = prefs.hapticsEnabled,
                testAdsEnabled = prefs.isTestAdsEnabled,
                isDailyCompletedToday = isDailyDone
            )
        }
    }

    fun setLanguage(lang: Language) {
        prefs.languageCode = lang.code
        _uiState.update { it.copy(language = lang) }
    }

    fun startPlayingLevel(levelIndex: Int? = null) {
        val targetIdx = levelIndex ?: _uiState.value.currentLevelIndex
        viewModelScope.launch {
            val levelData = repo.getLevelData(targetIdx + 1) ?: LevelCatalog.entityToLevelData(LevelCatalog.create250Levels()[0])
            _uiState.update {
                it.copy(
                    screenState = ScreenState.PLAYING_LEVEL,
                    currentLevelIndex = targetIdx,
                    level = levelData,
                    nodes = levelData.nodes.map { ln ->
                        Node(
                            id = ln.id,
                            x = ln.x,
                            y = ln.y,
                            connected = false,
                            type = ln.type,
                            keyForGateId = ln.keyForGateId
                        )
                    },
                    visitedNodeIds = emptyList(),
                    collectedKeyIds = emptySet(),
                    currentStrokeSegments = emptyList(),
                    currentPointerPos = null,
                    isDrawing = false,
                    echoes = emptyList(),
                    echoCountForLevel = 0,
                    isEchoShrinkerActive = false,
                    gameStatus = GameStatus.PLAYING,
                    isHintActive = false,
                    isDailyChallenge = false
                )
            }
        }
    }

    fun startDailyChallenge() {
        val todaySeed = (SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()).hashCode() and 0x7FFFFFFF) % 250
        viewModelScope.launch {
            val levelData = repo.getLevelData(todaySeed + 1) ?: LevelCatalog.entityToLevelData(LevelCatalog.create250Levels()[todaySeed])
            _uiState.update {
                it.copy(
                    screenState = ScreenState.DAILY_CHALLENGE,
                    currentLevelIndex = todaySeed,
                    level = levelData.copy(title = "Günün Bulmacası (2x Jeton)"),
                    nodes = levelData.nodes.map { ln ->
                        Node(id = ln.id, x = ln.x, y = ln.y, connected = false, type = ln.type, keyForGateId = ln.keyForGateId)
                    },
                    visitedNodeIds = emptyList(),
                    collectedKeyIds = emptySet(),
                    currentStrokeSegments = emptyList(),
                    currentPointerPos = null,
                    isDrawing = false,
                    echoes = emptyList(),
                    echoCountForLevel = 0,
                    isEchoShrinkerActive = false,
                    gameStatus = GameStatus.PLAYING,
                    isHintActive = false,
                    isDailyChallenge = true
                )
            }
        }
    }

    fun finishIntro() {
        _uiState.update { it.copy(screenState = ScreenState.MAIN_MENU) }
    }

    fun returnToMainMenu() {
        _uiState.update {
            it.copy(
                screenState = ScreenState.MAIN_MENU,
                isLevelSelectVisible = false,
                isShopDialogVisible = false,
                isSettingsDialogVisible = false,
                isSkinsDialogVisible = false
            )
        }
    }

    // --- Interactive Drawing & Engine Events ---

    fun onPointerDown(point: Point) {
        val state = _uiState.value
        if (state.gameStatus != GameStatus.PLAYING) return

        val hitNode = findNodeAtPoint(point, state.nodes)
        if (hitNode != null) {
            // "Karakter 1'den başlayacak" kuralı: Çizim mutlaka 1 numaralı başlangıç düğümünden başlamalıdır.
            if (hitNode.id != 1) {
                showToast("Karakter 1'den başlar! Çizime 1 numaralı noktadan başlayın.")
                triggerCollisionFeedback()
                return
            }

            // Check Lock & Key if starting at gate
            if (hitNode.type == NodeType.GATE && !state.collectedKeyIds.contains(hitNode.keyForGateId)) {
                showToast("Bu kapı kilitli! Önce anahtarı topla.")
                triggerCollisionFeedback()
                return
            }

            HarmonicAudioEngine.playNodeTone(0)
            triggerHapticClick()

            val newCollectedKeys = if (hitNode.type == NodeType.KEY) {
                state.collectedKeyIds + hitNode.keyForGateId
            } else state.collectedKeyIds

            _uiState.update {
                it.copy(
                    isDrawing = true,
                    visitedNodeIds = listOf(hitNode.id),
                    collectedKeyIds = newCollectedKeys,
                    currentStrokeSegments = emptyList(),
                    currentPointerPos = point,
                    nodes = it.nodes.map { n -> if (n.id == hitNode.id) n.copy(connected = true) else n }
                )
            }
        }
    }

    fun onPointerMove(point: Point) {
        val state = _uiState.value
        if (!state.isDrawing || state.gameStatus != GameStatus.PLAYING) return

        val lastVisitedId = state.visitedNodeIds.lastOrNull() ?: return
        val lastNode = state.nodes.firstOrNull { it.id == lastVisitedId } ?: return
        val candidateSegment = Segment(lastNode.toPoint(), point, lastVisitedId, -1)

        // 1. Proximity detection to past echoes: vibrates & alerts when close (< 16px)
        val allPastSegments = state.echoes.flatMap { it.segments }
        val minDist = CollisionEngine.minDistanceToEchoes(point, allPastSegments)
        val isNear = minDist < 18f
        if (isNear != state.isProximityAlertActive) {
            _uiState.update { it.copy(isProximityAlertActive = isNear) }
            if (isNear) triggerProximityHaptic()
        }

        // 2. Self-Intersection Check (Anlık Yol Çarpışması):
        // If the line currently being drawn crosses any earlier segments of this stroke!
        if (CollisionEngine.checkSelfIntersection(state.currentStrokeSegments, candidateSegment)) {
            triggerSelfIntersectionFailure("Kendi çizdiğin yola çarptın! (Self-Intersection)")
            return
        }

        // 3. Collision with past Echo Colliders:
        val hitboxScale = if (state.isEchoShrinkerActive) 0.5f else 1.0f
        val collidedEcho = CollisionEngine.checkCollisionWithEchoes(
            candidate = candidateSegment,
            echoes = allPastSegments,
            hitboxScale = hitboxScale
        )
        if (collidedEcho != null) {
            triggerCollisionFailure("Geçmiş yankı bariyerine çarptın!")
            return
        }

        // 4. Check if reaching a new node
        val hitNode = findNodeAtPoint(point, state.nodes)
        if (hitNode != null && hitNode.id != lastVisitedId) {
            // Already visited node?
            if (state.visitedNodeIds.contains(hitNode.id)) {
                triggerCollisionFailure("Ziyaret edilen düğüme geri dönemezsin!")
                return
            }

            // Directed edge constraint check:
            val illegalDirected = state.level.directedEdges.any { edge ->
                edge.fromId == hitNode.id && edge.toId == lastVisitedId
            }
            if (illegalDirected) {
                showToast("Bu kenar tek yönlü! Yalnızca ok yönünde çizilebilir.")
                triggerCollisionFailure("Ters yönlü kenar!")
                return
            }

            // User Rule: Sıra numaraları önemli olsun!
            // Sıradaki bağlanacak düğüm bir önceki düğümün ardışık numarası (lastVisitedId + 1) olmalıdır.
            val expectedNextId = lastVisitedId + 1
            if (hitNode.id != expectedNextId) {
                showToast("Sıradaki numara $expectedNextId olmalı!")
                HarmonicAudioEngine.playCollisionBuzz()
                triggerCollisionFeedback()
                return
            }

            // Lock & Key Gate check:
            if (hitNode.type == NodeType.GATE && !state.collectedKeyIds.contains(hitNode.keyForGateId)) {
                showToast("Kapı kilitli! Önce anahtar düğümünü bağla.")
                triggerCollisionFailure("Kilitli kapıya çarptın!")
                return
            }

            // Collect key if this node is a key
            val updatedKeys = if (hitNode.type == NodeType.KEY) {
                state.collectedKeyIds + hitNode.keyForGateId
            } else state.collectedKeyIds

            val finishedSegment = Segment(lastNode.toPoint(), hitNode.toPoint(), lastVisitedId, hitNode.id)
            val newStrokeList = state.currentStrokeSegments + finishedSegment
            val newVisited = state.visitedNodeIds + hitNode.id
            val newNodes = state.nodes.map { n -> if (n.id == hitNode.id) n.copy(connected = true) else n }

            HarmonicAudioEngine.playNodeTone(newVisited.size - 1)
            triggerHapticClick()

            // Check Victory (All nodes connected)
            if (newVisited.size == state.nodes.size) {
                handleVictory(newStrokeList)
                return
            }

            _uiState.update {
                it.copy(
                    visitedNodeIds = newVisited,
                    collectedKeyIds = updatedKeys,
                    currentStrokeSegments = newStrokeList,
                    currentPointerPos = hitNode.toPoint(),
                    nodes = newNodes
                )
            }
            return
        }

        _uiState.update { it.copy(currentPointerPos = point) }
    }

    fun onPointerUp() {
        val state = _uiState.value
        if (!state.isDrawing || state.gameStatus != GameStatus.PLAYING) return

        // Lifting finger before completion turns current path into an echo barrier
        if (state.currentStrokeSegments.isNotEmpty() && state.visitedNodeIds.size < state.nodes.size) {
            triggerEarlyLiftFailure("Hamle yarım bırakıldı!")
        } else {
            _uiState.update {
                it.copy(
                    isDrawing = false,
                    currentPointerPos = null,
                    visitedNodeIds = emptyList(),
                    currentStrokeSegments = emptyList(),
                    nodes = it.nodes.map { n -> n.copy(connected = false) }
                )
            }
        }
    }

    private fun handleVictory(completedSegments: List<Segment>) {
        HarmonicAudioEngine.playVictoryCascade()
        triggerHapticVictory()

        val state = _uiState.value
        val echoCount = state.echoCountForLevel
        val parEchoes = state.level.parEchoes

        if (state.isDailyChallenge) {
            // 2x Token reward for Daily Challenge
            prefs.addTokens(4)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            prefs.lastDailyCompletedDate = todayStr
            showToast("Tebrikler! Günün Bulmacası Tamamlandı (+4 Jeton!)")
        } else {
            viewModelScope.launch {
                repo.recordVictory(state.level.levelId, echoCount, parEchoes)
            }
            prefs.markLevelCompleted(state.currentLevelIndex)
        }

        _uiState.update {
            it.copy(
                isDrawing = false,
                currentPointerPos = null,
                currentStrokeSegments = completedSegments,
                gameStatus = GameStatus.VICTORY,
                tokens = prefs.tokens,
                isDailyCompletedToday = if (state.isDailyChallenge) true else it.isDailyCompletedToday
            )
        }
    }

    private fun triggerSelfIntersectionFailure(message: String) {
        showToast(message)
        commitStrokeAsEcho("Kendi Yoluyla Kesişti")
    }

    private fun triggerCollisionFailure(message: String) {
        showToast(message)
        commitStrokeAsEcho("Yankıya Çarpıldı")
    }

    private fun triggerEarlyLiftFailure(message: String) {
        showToast(message)
        commitStrokeAsEcho("Yarım Hamle")
    }

    private fun commitStrokeAsEcho(reason: String) {
        HarmonicAudioEngine.playCollisionBuzz()
        triggerCollisionFeedback()

        val state = _uiState.value
        val newEchoSegments = state.currentStrokeSegments.toList()
        prefs.incrementTotalEchoes()

        // Decaying Echoes logic
        val isDecaying = state.level.mechanicType == "DECAYING"
        val isGhost = state.level.isGhostEchoes

        val newEchoStroke = if (newEchoSegments.isNotEmpty()) {
            EchoStroke(
                id = (state.echoes.size + 1),
                segments = newEchoSegments,
                remainingAttempts = state.level.decayLifetime,
                maxLifetime = state.level.decayLifetime,
                isGhost = isGhost
            )
        } else null

        // Decrement lifetime of past decaying echoes
        val updatedPastEchoes = state.echoes.mapNotNull { echo ->
            if (isDecaying) {
                val remaining = echo.remainingAttempts - 1
                if (remaining <= 0) null else echo.copy(remainingAttempts = remaining)
            } else echo
        }

        val finalEchoes = if (newEchoStroke != null) updatedPastEchoes + newEchoStroke else updatedPastEchoes
        val newEchoCount = state.echoCountForLevel + (if (newEchoSegments.isNotEmpty()) 1 else 0)
        val isDeadlocked = newEchoCount >= deadlockThreshold

        _uiState.update {
            it.copy(
                isDrawing = false,
                currentPointerPos = null,
                visitedNodeIds = emptyList(),
                collectedKeyIds = emptySet(),
                currentStrokeSegments = emptyList(),
                echoes = finalEchoes,
                echoCountForLevel = newEchoCount,
                totalEchoes = prefs.totalEchoes,
                isCollisionAlertActive = true,
                nodes = it.nodes.map { n -> n.copy(connected = false) },
                gameStatus = if (isDeadlocked) GameStatus.DEADLOCK else GameStatus.PLAYING
            )
        }

        alertResetJob?.cancel()
        alertResetJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isCollisionAlertActive = false) }
        }
    }

    // --- Power-ups & Monetization ---

    fun useEchoBreaker() {
        val state = _uiState.value
        if (state.echoes.isEmpty()) {
            showToast("Kırılacak aktif yankı bulunmuyor.")
            return
        }
        if (prefs.useBreaker()) {
            HarmonicAudioEngine.playDrillBeam()
            triggerHapticClick()
            // Remove the most recent echo stroke
            val remainingEchoes = state.echoes.dropLast(1)
            _uiState.update {
                it.copy(
                    echoes = remainingEchoes,
                    echoBreakers = prefs.echoBreakers
                )
            }
            showToast("Matkap lazeri son yankıyı imha etti!")
        } else {
            showToast("Yetersiz Matkap Jetonu! Mağazadan temin edebilirsin.")
            setShopVisible(true)
        }
    }

    fun activateEchoShrinker() {
        _uiState.update {
            it.copy(
                isEchoShrinkerActive = true,
                gameStatus = GameStatus.PLAYING
            )
        }
        showToast("Esnek Alan Aktif! Yankı bariyerleri %50 inceldi.")
    }

    fun grantRewardedReward(type: String) {
        val rewardInfo = when (type) {
            "CLEAR_ECHOES" -> {
                clearAllEchoes()
                prefs.addTokens(1)
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        tokens = prefs.tokens,
                        gameStatus = GameStatus.PLAYING
                    )
                }
                RewardClaimedInfo(
                    title = "Yankılar Temizlendi!",
                    subtitle = "Bütün yankı bariyerleri silindi ve +1 İpucu Jetonu kazandınız.",
                    rewardType = type,
                    tokensAdded = 1
                )
            }
            "SHRINKER" -> {
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        isEchoShrinkerActive = true,
                        gameStatus = GameStatus.PLAYING
                    )
                }
                RewardClaimedInfo(
                    title = "Esnek Alan Aktif!",
                    subtitle = "Yankı bariyerleri %50 inceltildi.",
                    rewardType = type
                )
            }
            "FREE_BREAKER", "REWARD_DAILY" -> {
                prefs.addBreakers(1)
                prefs.addTokens(2)
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        echoBreakers = prefs.echoBreakers,
                        tokens = prefs.tokens
                    )
                }
                RewardClaimedInfo(
                    title = "Ödül Başarıyla Kazanıldı!",
                    subtitle = "+1 Matkap Lazeri & +2 İpucu Jetonu hesabınıza eklendi.",
                    rewardType = type,
                    tokensAdded = 2,
                    breakersAdded = 1
                )
            }
            "HINT_TOKEN" -> {
                prefs.addTokens(2)
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        tokens = prefs.tokens
                    )
                }
                RewardClaimedInfo(
                    title = "Jeton Ödülü Kazanıldı!",
                    subtitle = "+2 İpucu Jetonu hesabınıza başarıyla eklendi.",
                    rewardType = type,
                    tokensAdded = 2
                )
            }
            else -> {
                prefs.addTokens(1)
                _uiState.update {
                    it.copy(
                        isRewardedSimulating = false,
                        isAdLoading = false,
                        tokens = prefs.tokens
                    )
                }
                RewardClaimedInfo(
                    title = "Ödül Alındı!",
                    subtitle = "+1 İpucu Jetonu hesabınıza tanımlandı.",
                    rewardType = type,
                    tokensAdded = 1
                )
            }
        }
        _uiState.update {
            it.copy(
                rewardClaimedData = rewardInfo,
                isShopDialogVisible = false
            )
        }
        showToast(rewardInfo.title)
    }

    fun dismissRewardClaimedDialog() {
        _uiState.update { it.copy(rewardClaimedData = null) }
    }

    private var adLoadingTimeoutJob: Job? = null

    fun setAdLoading(loading: Boolean, type: String = "HINT_TOKEN") {
        adLoadingTimeoutJob?.cancel()
        _uiState.update { it.copy(isAdLoading = loading) }
        if (loading) {
            adLoadingTimeoutJob = viewModelScope.launch {
                delay(4500)
                if (_uiState.value.isAdLoading) {
                    _uiState.update { it.copy(isAdLoading = false) }
                    startRewardSimulation(type)
                }
            }
        }
    }

    fun startRewardSimulation(type: String) {
        rewardJob?.cancel()
        _uiState.update {
            it.copy(
                isRewardedSimulating = true,
                rewardSimulatingType = type,
                rewardCountdownSeconds = 3,
                isAdLoading = false
            )
        }
        rewardJob = viewModelScope.launch {
            for (sec in 3 downTo 1) {
                _uiState.update { it.copy(rewardCountdownSeconds = sec) }
                delay(1000)
            }
            _uiState.update { it.copy(rewardCountdownSeconds = 0) }
            delay(300)
            grantRewardedReward(type)
        }
    }

    fun dismissRewardSimulation() {
        rewardJob?.cancel()
        _uiState.update { it.copy(isRewardedSimulating = false) }
    }

    fun onAdUnavailable(type: String, reason: String) {
        // Automatically start the fallback reward flow so user never loses reward
        startRewardSimulation(type)
    }

    fun toggleTestAds(enabled: Boolean) {
        prefs.isTestAdsEnabled = enabled
        _uiState.update { it.copy(testAdsEnabled = enabled) }
        com.example.ads.StartIoManager.setTestModeEnabled(enabled, getApplication())
        showToast(
            if (enabled) "Test Reklam Modu Aktif"
            else "Canlı Reklam Modu Aktif (Google Play)"
        )
    }

    fun checkAdBlocker() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingAdBlocker = true) }
            val isBlocked = com.example.ads.AdBlockDetector.isAdBlockerActive(getApplication())
            _uiState.update {
                it.copy(
                    isAdBlockerDetected = isBlocked,
                    isCheckingAdBlocker = false
                )
            }
        }
    }

    fun clearEchoesAction() {
        clearAllEchoes()
        showToast("Yankılar temizlendi!")
    }

    fun useHint() {
        val state = _uiState.value
        if (state.isHintActive) return
        if (prefs.useToken()) {
            _uiState.update {
                it.copy(
                    isHintActive = true,
                    tokens = prefs.tokens
                )
            }
            showToast("İdeal rota ve ipucu düğüm sırası gösteriliyor.")
        } else {
            showToast("Yetersiz İpucu Jetonu! Mağazadan veya reklamla al.")
            setShopVisible(true)
        }
    }

    fun restartLevel(clearEchoes: Boolean = false) {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                visitedNodeIds = emptyList(),
                collectedKeyIds = emptySet(),
                currentStrokeSegments = emptyList(),
                currentPointerPos = null,
                isDrawing = false,
                echoes = if (clearEchoes) emptyList() else it.echoes,
                echoCountForLevel = if (clearEchoes) 0 else it.echoCountForLevel,
                gameStatus = GameStatus.PLAYING,
                nodes = it.nodes.map { n -> n.copy(connected = false) }
            )
        }
    }

    fun clearAllEchoes() {
        if (_uiState.value.echoes.isNotEmpty()) {
            HarmonicAudioEngine.playBrokenRedLine()
        }
        _uiState.update {
            it.copy(
                echoes = emptyList(),
                echoCountForLevel = 0,
                gameStatus = GameStatus.PLAYING
            )
        }
    }

    fun nextLevel() {
        HarmonicAudioEngine.playNextLevel()
        val nextIdx = _uiState.value.currentLevelIndex + 1
        if (nextIdx < 250) {
            startPlayingLevel(nextIdx)
        } else {
            showToast("Tebrikler! 250 bölümün tamamını fethettiniz!")
            returnToMainMenu()
        }
    }

    fun resetAllGameProgress() {
        viewModelScope.launch {
            repo.resetAllProgress()
            prefs.resetAllProgress()
            loadSavedPreferences()
            startPlayingLevel(0)
            returnToMainMenu()
            showToast("Tüm ilerleme sıfırlandı! 1. Bölümden başlıyorsunuz.")
        }
    }

    // --- Customization & Settings ---

    fun setStrokeTheme(theme: StrokeTheme) {
        prefs.strokeThemeName = theme.name
        _uiState.update { it.copy(strokeTheme = theme) }
    }

    fun setEchoTheme(theme: EchoTheme) {
        prefs.echoThemeName = theme.name
        _uiState.update { it.copy(echoTheme = theme) }
    }

    fun toggleSound(enabled: Boolean) {
        prefs.soundEnabled = enabled
        HarmonicAudioEngine.isSoundEnabled = enabled
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        prefs.hapticsEnabled = enabled
        _uiState.update { it.copy(hapticsEnabled = enabled) }
    }

    fun purchaseTokens(amount: Int) {
        prefs.addTokens(amount)
        _uiState.update { it.copy(tokens = prefs.tokens) }
        showToast("$amount adet İpucu Jetonu eklendi!")
    }

    fun purchaseBreakers(amount: Int) {
        prefs.addBreakers(amount)
        _uiState.update { it.copy(echoBreakers = prefs.echoBreakers) }
        showToast("$amount adet Matkap Lazeri eklendi!")
    }

    // Modal Visibilities
    fun setLevelSelectVisible(visible: Boolean) { _uiState.update { it.copy(isLevelSelectVisible = visible) } }
    fun setShopVisible(visible: Boolean) { _uiState.update { it.copy(isShopDialogVisible = visible) } }
    fun setSettingsVisible(visible: Boolean) { _uiState.update { it.copy(isSettingsDialogVisible = visible) } }
    fun setSkinsVisible(visible: Boolean) { _uiState.update { it.copy(isSkinsDialogVisible = visible) } }

    private fun findNodeAtPoint(point: Point, nodes: List<Node>): Node? {
        return nodes.firstOrNull { node ->
            node.toPoint().distanceTo(point) <= nodeHitRadius
        }
    }

    private fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
        toastResetJob?.cancel()
        toastResetJob = viewModelScope.launch {
            delay(2400)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    private fun triggerHapticClick() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    private fun triggerProximityHaptic() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(12, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(12)
            }
        } catch (_: Exception) {}
    }

    private fun triggerCollisionFeedback() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 40, 60), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(120)
            }
        } catch (_: Exception) {}
    }

    private fun triggerHapticVictory() {
        if (!_uiState.value.hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40, 60, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        } catch (_: Exception) {}
    }
}
