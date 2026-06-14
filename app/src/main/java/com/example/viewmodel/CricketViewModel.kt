package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// Matches the exact innings data
data class InningsState(
    val battingTeam: Team = Team("", ""),
    val bowlingTeam: Team = Team("", ""),
    val runs: Int = 0,
    val wickets: Int = 0,
    val balls: Int = 0, // legal balls
    val extras: Int = 0,
    val currentOverSymbols: List<String> = emptyList(), // e.g., ["1", "4", "Wd", "W", "0"]
    val strikerId: String? = null,
    val nonStrikerId: String? = null,
    val activeBowlerId: String? = null,
    val playersStats: Map<String, Player> = emptyMap(), // Maps playerId to match-specific Player state
    val pastOversTotalRuns: List<Int> = emptyList(),
    val isCompleted: Boolean = false
) {
    val oversString: String
        get() {
            val overs = balls / 6
            val remainingBalls = balls % 6
            return "$overs.$remainingBalls"
        }
}

data class MatchUiState(
    val screen: GameScreen = GameScreen.TEAM_A_SETUP,
    
    // Setup States
    val teamAName: String = "Red Team",
    val teamANewPlayerName: String = "",
    val teamANewPlayerRole: PlayerRole = PlayerRole.BATSMAN,
    val teamANewPlayerIsCaptain: Boolean = false,
    val teamANewPlayerIsViceCaptain: Boolean = false,
    val teamAPlayers: List<Player> = emptyList(),
    
    val teamBName: String = "Blue Team",
    val teamBNewPlayerName: String = "",
    val teamBNewPlayerRole: PlayerRole = PlayerRole.BATSMAN,
    val teamBNewPlayerIsCaptain: Boolean = false,
    val teamBNewPlayerIsViceCaptain: Boolean = false,
    val teamBPlayers: List<Player> = emptyList(),
    
    // Match Setup
    val matchSettings: MatchSettings = MatchSettings(MatchType.STANDARD, 5),
    
    // Toss State
    val isTossing: Boolean = false,
    val tossResult: String = "",
    val tossWinnerName: String = "",
    val isTossCompleted: Boolean = false,
    
    // Live Match States
    val currentInningsIndex: Int = 1, // 1 or 2
    val innings1: InningsState? = null,
    val innings2: InningsState? = null,
    
    // Active gameplay details
    val targetToWin: Int? = null, // Set in 2nd innings
    val ballHistoryList: List<BallHistoryEntry> = emptyList(),
    val liveAiCommentary: String = "🎙️ Ready for the clash! Add players and complete the toss to start the AI Commentary...",
    val isAiCommentaryLoading: Boolean = false,
    
    // Dialog state for choosing new batsman / bowler / out confirmation
    val showOutDialog: Boolean = false,
    val outBatsmanId: String? = null, // The one being marked out
    val outBowlerId: String? = null,  // Bowler who took the wicket
    val showNewBatsmanSelection: Boolean = false,
    val showBowlerSelection: Boolean = false,

    // DRS (Decision Review System) States
    val teamADrsReviewsLeft: Int = 2,
    val teamBDrsReviewsLeft: Int = 2,
    val showDrsDialog: Boolean = false,
    val drsReviewerTeamName: String = "",
    val drsIsBattingTeamReview: Boolean = true, // true: batting team checking an OUT. false: bowling team checking NOT OUT
    val drsReviewType: String = "LBW", // "LBW" or "EDGE" (Caught behind / bat pad)
    val drsBatsmanId: String? = null,
    val drsBowlerId: String? = null,
    val drsCurrentStage: String = "NONE", // "NONE", "INITIATING", "ULTRA_EDGE", "BALL_TRACKING", "DECISION"
    val drsUltraEdgeHasSpike: Boolean = false,
    val drsUltraEdgeWaveform: List<Float> = emptyList(),
    val drsBallTrackingPitching: String = "In Line", // "In Line", "Outside Off", "Outside Leg"
    val drsBallTrackingImpact: String = "In Line", // "In Line", "Outside", "Umpire's Call"
    val drsBallTrackingWickets: String = "Hitting", // "Hitting", "Missing", "Umpire's Call"
    val drsLiveCommentary: String = "",
    val drsFinalDecisionOut: Boolean = true,
    val drsIsCompleted: Boolean = false,
    val drsIsRunning: Boolean = false,
    val drsBallTrackingLength: String = "Good Length",
    val drsBallTrackingPitchX: Float = 0.5f,
    val drsBallTrackingPitchY: Float = 0.6f,
    val drsUltraCleanCatchCheck: Boolean = true,
    val drsCatchFielderName: String = "Wicketkeeper",
    val activeTab: GameplayTab = GameplayTab.SCORING,
    val lastBallDrsSimulation: DrsSimulationData = DrsSimulationData()
)

class CricketViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MatchUiState())
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    init {
        // Automatically populate with some nice defaults so that it's zero-friction to demo
        quickFillTeamA()
        quickFillTeamB()
    }

    // --- Core UI Navigation Actions ---
    fun setScreen(screen: GameScreen) {
        _uiState.update { it.copy(screen = screen) }
    }

    fun setGameplayTab(tab: GameplayTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    // --- Team A Setup Functions ---
    fun updateTeamAName(name: String) {
        _uiState.update { it.copy(teamAName = name) }
    }

    fun updateTeamANewPlayerName(name: String) {
        _uiState.update { it.copy(teamANewPlayerName = name) }
    }

    fun updateTeamANewPlayerRole(role: PlayerRole) {
        _uiState.update { it.copy(teamANewPlayerRole = role) }
    }

    fun updateTeamANewPlayerIsCaptain(isC: Boolean) {
        _uiState.update { it.copy(
            teamANewPlayerIsCaptain = isC,
            teamANewPlayerIsViceCaptain = if (isC) false else it.teamANewPlayerIsViceCaptain
        ) }
    }

    fun updateTeamANewPlayerIsViceCaptain(isVc: Boolean) {
        _uiState.update { it.copy(
            teamANewPlayerIsViceCaptain = isVc,
            teamANewPlayerIsCaptain = if (isVc) false else it.teamANewPlayerIsCaptain
        ) }
    }

    fun addPlayerToTeamA() {
        val state = _uiState.value
        if (state.teamANewPlayerName.isBlank()) return

        val hasCaptain = state.teamAPlayers.any { it.isCaptain } && state.teamANewPlayerIsCaptain
        val hasViceCaptain = state.teamAPlayers.any { it.isViceCaptain } && state.teamANewPlayerIsViceCaptain

        val player = Player(
            name = state.teamANewPlayerName.trim(),
            role = state.teamANewPlayerRole,
            isCaptain = if (hasCaptain) false else state.teamANewPlayerIsCaptain,
            isViceCaptain = if (hasViceCaptain) false else state.teamANewPlayerIsViceCaptain
        )

        _uiState.update {
            it.copy(
                teamAPlayers = it.teamAPlayers + player,
                teamANewPlayerName = "",
                teamANewPlayerRole = PlayerRole.BATSMAN,
                teamANewPlayerIsCaptain = false,
                teamANewPlayerIsViceCaptain = false
            )
        }
    }

    fun removePlayerFromTeamA(id: String) {
        _uiState.update {
            it.copy(teamAPlayers = it.teamAPlayers.filter { p -> p.id != id })
        }
    }

    fun quickFillTeamA() {
        val players = listOf(
            Player(name = "Virat Kohli", role = PlayerRole.BATSMAN, isCaptain = true),
            Player(name = "Rohit Sharma", role = PlayerRole.BATSMAN, isViceCaptain = true),
            Player(name = "Rishabh Pant", role = PlayerRole.WICKETKEEPER),
            Player(name = "Surya Kumar", role = PlayerRole.BATSMAN),
            Player(name = "Hardik Pandya", role = PlayerRole.ALL_ROUNDER),
            Player(name = "Ravindra Jadeja", role = PlayerRole.ALL_ROUNDER),
            Player(name = "Jasprit Bumrah", role = PlayerRole.BOWLER),
            Player(name = "Kuldeep Yadav", role = PlayerRole.BOWLER),
            Player(name = "Arshdeep Singh", role = PlayerRole.BOWLER),
            Player(name = "Siraj Ahmed", role = PlayerRole.BOWLER),
            Player(name = "Rinku Singh", role = PlayerRole.FIELDER)
        )
        _uiState.update { it.copy(teamAPlayers = players) }
    }

    // --- Team B Setup Functions ---
    fun updateTeamBName(name: String) {
        _uiState.update { it.copy(teamBName = name) }
    }

    fun updateTeamBNewPlayerName(name: String) {
        _uiState.update { it.copy(teamBNewPlayerName = name) }
    }

    fun updateTeamBNewPlayerRole(role: PlayerRole) {
        _uiState.update { it.copy(teamBNewPlayerRole = role) }
    }

    fun updateTeamBNewPlayerIsCaptain(isC: Boolean) {
        _uiState.update { it.copy(
            teamBNewPlayerIsCaptain = isC,
            teamBNewPlayerIsViceCaptain = if (isC) false else it.teamBNewPlayerIsViceCaptain
        ) }
    }

    fun updateTeamBNewPlayerIsViceCaptain(isVc: Boolean) {
        _uiState.update { it.copy(
            teamBNewPlayerIsViceCaptain = isVc,
            teamBNewPlayerIsCaptain = if (isVc) false else it.teamBNewPlayerIsCaptain
        ) }
    }

    fun addPlayerToTeamB() {
        val state = _uiState.value
        if (state.teamBNewPlayerName.isBlank()) return

        val hasCaptain = state.teamBPlayers.any { it.isCaptain } && state.teamBNewPlayerIsCaptain
        val hasViceCaptain = state.teamBPlayers.any { it.isViceCaptain } && state.teamBNewPlayerIsViceCaptain

        val player = Player(
            name = state.teamBNewPlayerName.trim(),
            role = state.teamBNewPlayerRole,
            isCaptain = if (hasCaptain) false else state.teamBNewPlayerIsCaptain,
            isViceCaptain = if (hasViceCaptain) false else state.teamBNewPlayerIsViceCaptain
        )

        _uiState.update {
            it.copy(
                teamBPlayers = it.teamBPlayers + player,
                teamBNewPlayerName = "",
                teamBNewPlayerRole = PlayerRole.BATSMAN,
                teamBNewPlayerIsCaptain = false,
                teamBNewPlayerIsViceCaptain = false
            )
        }
    }

    fun removePlayerFromTeamB(id: String) {
        _uiState.update {
            it.copy(teamBPlayers = it.teamBPlayers.filter { p -> p.id != id })
        }
    }

    fun quickFillTeamB() {
        val players = listOf(
            Player(name = "Pat Cummins", role = PlayerRole.BOWLER, isCaptain = true),
            Player(name = "Travis Head", role = PlayerRole.BATSMAN, isViceCaptain = true),
            Player(name = "Steven Smith", role = PlayerRole.BATSMAN),
            Player(name = "Mitch Marsh", role = PlayerRole.ALL_ROUNDER),
            Player(name = "Glenn Maxwell", role = PlayerRole.ALL_ROUNDER),
            Player(name = "Alex Carey", role = PlayerRole.WICKETKEEPER),
            Player(name = "Mitchell Starc", role = PlayerRole.BOWLER),
            Player(name = "Josh Hazlewood", role = PlayerRole.BOWLER),
            Player(name = "Adam Zampa", role = PlayerRole.BOWLER),
            Player(name = "Marcus Stoinis", role = PlayerRole.ALL_ROUNDER),
            Player(name = "Matthew Wade", role = PlayerRole.FIELDER)
        )
        _uiState.update { it.copy(teamBPlayers = players) }
    }

    // --- Match Settings ---
    fun updateMatchType(type: MatchType) {
        _uiState.update {
            it.copy(matchSettings = it.matchSettings.copy(matchType = type))
        }
    }

    fun updateMaxOvers(overs: Int) {
        _uiState.update {
            it.copy(matchSettings = it.matchSettings.copy(maxOvers = overs))
        }
    }

    // --- Toss Simulation with AI ---
    fun runTossWithAi() {
        val state = _uiState.value
        val nameA = state.teamAName.ifBlank { "Red Team" }
        val nameB = state.teamBName.ifBlank { "Blue Team" }

        _uiState.update { it.copy(isTossing = true) }

        viewModelScope.launch {
            val prompt = """
                You are a premium live TV Cricket commentator. Simulate a realistic and exciting coin toss.
                We have two teams:
                Team A: "$nameA" (Red Team)
                Team B: "$nameB" (Blue/Opposing Team)
                
                TOSS_SIMULATION
                Choose a winner of the toss (randomly). By default, cricket team captains in this fantasy app who win the toss always choose to BAT first.
                Provide a witty, engaging 3-sentence summary of the coin toss, pitch report (flat track, full of runs!), and why they chose to BAT first. Make it look super authentic and live-broadcast quality.
            """.trimIndent()

            val winner = if (Math.random() > 0.5) nameA else nameB
            val coinFlip = if (Math.random() > 0.5) "Heads" else "Tails"

            val fallbackText = "★ [AI Coin Toss Analyst] ★\n\n\"The coin is up in the air... and it lands **$coinFlip**! $winner wins the toss! Looking at this dry, sun-baked green pitch, it's a batsman's dream. The captains have made up their minds: **they will BAT first** to put maximum scoreboard pressure on the chasing side!\""

            val commentary = GeminiClient.getAiResponse(prompt, fallbackText)

            _uiState.update {
                it.copy(
                    isTossing = false,
                    isTossCompleted = true,
                    tossWinnerName = winner,
                    tossResult = commentary
                )
            }
        }
    }

    // --- Start Match Gameplay ---
    fun startMatch() {
        val state = _uiState.value
        val isTeamAWinner = state.tossWinnerName == state.teamAName
        
        // Toss winner chooses to bat first
        val battingTeam = if (isTeamAWinner) {
            Team(state.teamAName, "#FF3B30", state.teamAPlayers)
        } else {
            Team(state.teamBName, "#007AFF", state.teamBPlayers)
        }

        val bowlingTeam = if (isTeamAWinner) {
            Team(state.teamBName, "#007AFF", state.teamBPlayers)
        } else {
            Team(state.teamAName, "#FF3B30", state.teamAPlayers)
        }

        // Initialize players statistic map (keeps players state isolated and editable inside innings)
        val playerStats = (battingTeam.players + bowlingTeam.players).associate { it.id to it.copy() }

        // Find openers (first two players of batting team)
        val striker = battingTeam.players.getOrNull(0)?.id
        val nonStriker = battingTeam.players.getOrNull(1)?.id
        
        // Find default bowler (first bowler/all-rounder, or else first player associated with bowling team)
        val bowler = bowlingTeam.players.find { it.role == PlayerRole.BOWLER || it.role == PlayerRole.ALL_ROUNDER }?.id 
            ?: bowlingTeam.players.getOrNull(0)?.id

        val initialInnings = InningsState(
            battingTeam = battingTeam,
            bowlingTeam = bowlingTeam,
            strikerId = striker,
            nonStrikerId = nonStriker,
            activeBowlerId = bowler,
            playersStats = playerStats
        )

        _uiState.update {
            it.copy(
                screen = GameScreen.GAMEPLAY,
                currentInningsIndex = 1,
                innings1 = initialInnings,
                innings2 = null,
                targetToWin = null,
                ballHistoryList = emptyList(),
                liveAiCommentary = "🎙️ Match begins! ${battingTeam.name} batsmen take guard at the crease. ${bowlingTeam.name} fielding unit spreads out. Let's play cricket!"
            )
        }
    }

    // Get active state of the current innings
    private val activeInnings: InningsState?
        get() = if (_uiState.value.currentInningsIndex == 1) _uiState.value.innings1 else _uiState.value.innings2

    fun selectActiveBowler(bowlerId: String) {
        if (_uiState.value.currentInningsIndex == 1) {
            _uiState.update { it.copy(
                innings1 = it.innings1?.copy(activeBowlerId = bowlerId),
                showBowlerSelection = false
            ) }
        } else {
            _uiState.update { it.copy(
                innings2 = it.innings2?.copy(activeBowlerId = bowlerId),
                showBowlerSelection = false
            ) }
        }
    }

    fun selectNewBatsman(newBatsmanId: String) {
        val state = _uiState.value
        val innings = activeInnings ?: return
        val outBatsmanId = state.outBatsmanId ?: return

        // Update active innings striker/non-striker swap or replacement
        val isStrikerReplacement = innings.strikerId == outBatsmanId
        val nextStriker = if (isStrikerReplacement) newBatsmanId else innings.strikerId
        val nextNonStriker = if (!isStrikerReplacement) newBatsmanId else innings.nonStrikerId

        updateInningsState {
            it.copy(
                strikerId = nextStriker,
                nonStrikerId = nextNonStriker
            )
        }

        _uiState.update {
            it.copy(
                showNewBatsmanSelection = false,
                outBatsmanId = null,
                outBowlerId = null
            )
        }
    }

    fun openOutDialog() {
        val innings = activeInnings ?: return
        _uiState.update {
            it.copy(
                showOutDialog = true,
                outBatsmanId = innings.strikerId, // Default is striker is out
                outBowlerId = innings.activeBowlerId
            )
        }
    }

    fun cancelOutDialog() {
        _uiState.update { it.copy(showOutDialog = false) }
    }

    fun selectOutBatsman(id: String) {
        _uiState.update { it.copy(outBatsmanId = id) }
    }

    fun selectOutBowler(id: String) {
        _uiState.update { it.copy(outBowlerId = id) }
    }

    // --- Scoring Actions ---

    fun recordNormalRuns(runs: Int) {
        val innings = activeInnings ?: return
        val strikerId = innings.strikerId ?: return
        val bowlerId = innings.activeBowlerId ?: return

        val currentStriker = innings.playersStats[strikerId] ?: return
        val currentBowler = innings.playersStats[bowlerId] ?: return

        // Update striker batting stats
        val updatedStriker = currentStriker.copy(
            runsScored = currentStriker.runsScored + runs,
            ballsFaced = currentStriker.ballsFaced + 1,
            fours = currentStriker.fours + if (runs == 4) 1 else 0,
            sixes = currentStriker.sixes + if (runs == 6) 1 else 0
        )

        // Update bowler bowling stats
        val updatedBowler = currentBowler.copy(
            ballsBowled = currentBowler.ballsBowled + 1,
            runsConceded = currentBowler.runsConceded + runs,
            runsConcededThisOver = currentBowler.runsConcededThisOver + runs
        )

        val updatedStats = innings.playersStats.toMutableMap()
        updatedStats[strikerId] = updatedStriker
        updatedStats[bowlerId] = updatedBowler

        // Create symbol for current over Progress
        val overSymbol = runs.toString()
        val nextOverSymbols = innings.currentOverSymbols + overSymbol
        
        // Update runs and ball count
        val nextRuns = innings.runs + runs
        val nextBalls = innings.balls + 1

        // Record locally for history ticker
        val entry = BallHistoryEntry(
            bowlerName = currentBowler.name,
            batsmanName = currentStriker.name,
            description = if (runs == 0) "Dot ball" else "$runs run${if (runs > 1) "s" else ""}",
            isExtra = false,
            isWicket = false
        )

        updateInningsState {
            it.copy(
                runs = nextRuns,
                balls = nextBalls,
                currentOverSymbols = nextOverSymbols,
                playersStats = updatedStats
            )
        }

        _uiState.update {
            it.copy(ballHistoryList = listOf(entry) + it.ballHistoryList)
        }

        updateLastBallDrs(
            eventDescription = if (runs == 0) "Dot ball" else "$runs Runs",
            strikerName = currentStriker.name,
            bowlerName = currentBowler.name,
            isWicket = false,
            isWide = false,
            runs = runs
        )

        // Generate AI Live Commentary for boundaries
        if (runs == 4 || runs == 6) {
            triggerAiCommentary(runs, currentStriker.name, currentBowler.name)
        }

        checkInningsProgress(runs)
    }

    fun recordExtraBall(isWide: Boolean) {
        val innings = activeInnings ?: return
        val bowlerId = innings.activeBowlerId ?: return
        val strikerId = innings.strikerId ?: return
        
        val currentBowler = innings.playersStats[bowlerId] ?: return
        val currentStriker = innings.playersStats[strikerId] ?: return

        // In local rules: Wide ball gives 0 runs. No Ball still gives +1 run.
        val extraRunToAdd = if (isWide) 0 else 1

        val updatedBowler = currentBowler.copy(
            runsConceded = currentBowler.runsConceded + extraRunToAdd,
            runsConcededThisOver = currentBowler.runsConcededThisOver + extraRunToAdd
        )

        val updatedStats = innings.playersStats.toMutableMap()
        updatedStats[bowlerId] = updatedBowler

        val overSymbol = if (isWide) "Wd" else "Nb"
        val nextOverSymbols = innings.currentOverSymbols + overSymbol

        val nextRuns = innings.runs + extraRunToAdd
        val nextExtras = innings.extras + extraRunToAdd

        val entry = BallHistoryEntry(
            bowlerName = currentBowler.name,
            batsmanName = currentStriker.name,
            description = if (isWide) "Wide ball (+0 runs - Local Rule)" else "No ball (+1 run)",
            isExtra = true,
            isWicket = false
        )

        updateInningsState {
            it.copy(
                runs = nextRuns,
                extras = nextExtras,
                currentOverSymbols = nextOverSymbols,
                playersStats = updatedStats
            )
        }

        _uiState.update {
            it.copy(ballHistoryList = listOf(entry) + it.ballHistoryList)
        }

        updateLastBallDrs(
            eventDescription = if (isWide) "Wide ball" else "No Ball",
            strikerName = currentStriker.name,
            bowlerName = currentBowler.name,
            isWicket = false,
            isWide = isWide,
            runs = if (isWide) 0 else 1
        )

        // Extras check for targets
        checkInningsProgress(0) // No actual batsman runs but checks totals
    }

    fun submitWicket() {
        val state = _uiState.value
        val innings = activeInnings ?: return
        val outBatsmanId = state.outBatsmanId ?: return
        val outBowlerId = state.outBowlerId ?: return

        val currentStriker = innings.playersStats[outBatsmanId] ?: return
        val currentBowler = innings.playersStats[outBowlerId] ?: return

        // Update batsman as out
        val updatedBatsman = currentStriker.copy(
            isOut = true,
            ballsFaced = currentStriker.ballsFaced + (if (outBatsmanId == innings.strikerId) 1 else 0), // if striker gets out, they face other ball
            dismissalInfo = "b ${currentBowler.name}"
        )

        // Update bowler wickets taken, only if bowler took it (not run out, but standard bowler wicket)
        // Also bowler ball counts increase because wicket is a legal delivery!
        val updatedBowler = currentBowler.copy(
            ballsBowled = currentBowler.ballsBowled + 1,
            wicketsTaken = currentBowler.wicketsTaken + 1
        )

        val updatedStats = innings.playersStats.toMutableMap()
        updatedStats[outBatsmanId] = updatedBatsman
        updatedStats[outBowlerId] = updatedBowler

        val overSymbol = "W"
        val nextOverSymbols = innings.currentOverSymbols + overSymbol

        val nextWickets = innings.wickets + 1
        val nextBalls = innings.balls + 1

        val entry = BallHistoryEntry(
            bowlerName = currentBowler.name,
            batsmanName = currentStriker.name,
            description = "WICKET! ${currentStriker.name} departs! (${updatedBatsman.runsScored} runs in ${updatedBatsman.ballsFaced} balls)",
            isExtra = false,
            isWicket = true
        )

        updateInningsState {
            it.copy(
                wickets = nextWickets,
                balls = nextBalls,
                currentOverSymbols = nextOverSymbols,
                playersStats = updatedStats
            )
        }

        _uiState.update {
            it.copy(
                showOutDialog = false,
                ballHistoryList = listOf(entry) + it.ballHistoryList
            )
        }

        updateLastBallDrs(
            eventDescription = "Out Wicket",
            strikerName = currentStriker.name,
            bowlerName = currentBowler.name,
            isWicket = true,
            isWide = false,
            runs = 0
        )

        // AI Live Commentary for Wicket!
        triggerAiCommentary(-1, currentStriker.name, currentBowler.name)

        // Check if Team is All Out, or if they can select a new batsman
        val bittedIds = innings.battingTeam.players.map { it.id }
        // Players who haven't batted yet: not currently striker/nonstriker and not out
        val unbattedPlayers = innings.battingTeam.players.filter { player ->
            val finalStats = updatedStats[player.id] ?: player
            player.id != innings.strikerId && player.id != innings.nonStrikerId && !finalStats.isOut
        }

        val allOut = nextWickets >= (innings.battingTeam.players.size - 1)

        if (allOut || unbattedPlayers.isEmpty()) {
            // Innings is over!
            wrapInnings()
        } else {
            // Prompt to select a new batsman to come in
            _uiState.update {
                it.copy(
                    showNewBatsmanSelection = true,
                    outBatsmanId = outBatsmanId
                )
            }
            checkInningsProgress(0, forceSkipOverCheck = true)
        }
    }

    // --- Virtual DRS (vDRS) Game Loop Functions ---

    fun initiateDrsReview(isWicketReview: Boolean, reviewType: String) {
        val state = _uiState.value
        val innings = activeInnings ?: return
        
        val strikerId = innings.strikerId ?: return
        val bowlerId = innings.activeBowlerId ?: return
        
        val isTeamABatting = innings.battingTeam.name == state.teamAName
        
        // Under local/user rules, DRS reviews are UNLIMITED. We do not deduct reviews.
        val reviewerTeamName = if (isWicketReview) {
            innings.battingTeam.name
        } else {
            innings.bowlingTeam.name
        }
        
        // Generate random parameters for vDRS
        val hasSpike = Math.random() > 0.50
        val cleanCatch = Math.random() > 0.45
        val fielder = listOf("Wicketkeeper", "First Slip", "Second Slip", "Deep Midwicket", "Slips-Catcher").random()
        val pitching = listOf("In Line", "In Line", "Outside Off", "Outside Leg").random()
        val impact = listOf("In Line", "In Line", "Outside", "Umpire's Call").random()
        val wickets = listOf("Hitting", "Hitting", "Missing", "Umpire's Call").random()
        val length = listOf("Good Length", "Short Ball", "Full Pitch", "Yorker").random()
        
        val pitchX = when (pitching) {
            "In Line" -> 0.45f + Math.random().toFloat() * 0.1f
            "Outside Off" -> 0.65f + Math.random().toFloat() * 0.15f
            else -> 0.15f + Math.random().toFloat() * 0.15f
        }
        
        val pitchY = when (length) {
            "Yorker" -> 0.08f + Math.random().toFloat() * 0.08f
            "Full Pitch" -> 0.22f + Math.random().toFloat() * 0.12f
            "Good Length" -> 0.45f + Math.random().toFloat() * 0.15f
            else -> 0.72f + Math.random().toFloat() * 0.18f
        }
        
        // Determine final OUT or NOT OUT outcome
        val finalDecisionOut: Boolean
        if (reviewType == "EDGE") {
            // EDGE check: if there is a spike -> OUT (caught behind / catch!)
            finalDecisionOut = hasSpike
        } else if (reviewType == "CATCH") {
            // CATCH check: if clean catch from camera -> OUT
            finalDecisionOut = cleanCatch
        } else {
            // LBW check:
            // First check if there is an edge. If there is a spike -> ball hit bat first -> NOT OUT!
            if (hasSpike) {
                finalDecisionOut = false // No LBW possible because of edge!
            } else {
                // If no edge, checked by Hawkeye ball tracking:
                // Pitching must be In Line or Outside Off (not outside leg for LBW)
                // Impact must be In Line or Umpire's Call
                // Wickets must be Hitting or Umpire's Call
                finalDecisionOut = (pitching == "In Line" || pitching == "Outside Off") &&
                        (impact == "In Line" || impact == "Umpire's Call") &&
                        (wickets == "Hitting" || wickets == "Umpire's Call")
            }
        }
        
        // Generating waveform values for Snicko
        val waveformList = List(50) { i ->
            if (((reviewType == "EDGE" || reviewType == "LBW") && hasSpike) && i in 22..28) {
                // Generate a spike in the middle
                val dist = Math.abs(i - 25)
                val base = (1.5f - (dist * 0.25f)).coerceAtLeast(0.05f) * (if (Math.random() > 0.5) 1f else -1f)
                base * (1.2f + Math.random().toFloat() * 0.8f)
            } else {
                // Small ambient noise
                (Math.random().toFloat() * 0.2f - 0.1f)
            }
        }
        
        _uiState.update {
            it.copy(
                showOutDialog = false, // dismiss the standard out dialog
                showDrsDialog = true,
                drsReviewerTeamName = reviewerTeamName,
                drsIsBattingTeamReview = isWicketReview,
                drsReviewType = reviewType,
                drsBatsmanId = strikerId,
                drsBowlerId = bowlerId,
                drsCurrentStage = "INITIATING",
                drsUltraEdgeHasSpike = hasSpike,
                drsUltraEdgeWaveform = waveformList,
                drsBallTrackingPitching = pitching,
                drsBallTrackingImpact = impact,
                drsBallTrackingWickets = wickets,
                drsFinalDecisionOut = finalDecisionOut,
                drsIsCompleted = false,
                drsIsRunning = true,
                drsBallTrackingLength = length,
                drsBallTrackingPitchX = pitchX,
                drsBallTrackingPitchY = pitchY,
                drsUltraCleanCatchCheck = cleanCatch,
                drsCatchFielderName = fielder,
                drsLiveCommentary = "🎙️ DRS Activated! ${reviewerTeamName} has challenged the decision. Signal is going up to the Virtual Third Umpire..."
            )
        }
        
        // Start simulated delay progression
        runDrsStageSimulation()
    }

    private fun runDrsStageSimulation() {
        viewModelScope.launch {
            val startState = _uiState.value
            val innings = activeInnings ?: return@launch
            val striker = innings.playersStats[startState.drsBatsmanId]
            val bowler = innings.playersStats[startState.drsBowlerId]
            val batsmanName = striker?.name ?: "Batsman"
            val bowlerName = bowler?.name ?: "Bowler"
            
            // --- Stage 1: INITIATING to next stage ---
            kotlinx.coroutines.delay(1800)
            
            if (startState.drsReviewType == "CATCH") {
                _uiState.update {
                    it.copy(
                        drsCurrentStage = "CAMERA_CATCH_CHECK",
                        drsLiveCommentary = "🎙️ Camera Referee here. Zooming in with Side-On Ultra Slow-Motion High Frame Rate cameras to check the catch by ${startState.drsCatchFielderName}..."
                    )
                }
                kotlinx.coroutines.delay(2200)
                val catchComment = if (startState.drsUltraCleanCatchCheck) {
                    "🎙️ Beautiful zoom on Angle #4! We can see of the ball slipping into the slot and fingers are cleanly underneath! The ball stayed off the grass. It is a completely CLEAN CATCH!"
                } else {
                    "🎙️ Wait, side-on zoom reveals the ball actually double-bounced off the blades of grass right before resting inside the hands. Grounded catch! NOT OUT!"
                }
                _uiState.update {
                    it.copy(drsLiveCommentary = catchComment)
                }
                kotlinx.coroutines.delay(2000)
            } else {
                _uiState.update {
                    it.copy(
                        drsCurrentStage = "ULTRA_EDGE",
                        drsLiveCommentary = "🎙️ Third Umpire here. Checking UltraEdge/Snickometer... Let's check the lateral frames of the ball passing the bat."
                    )
                }
                
                kotlinx.coroutines.delay(2000)
                val spikeComment = if (startState.drsUltraEdgeHasSpike) {
                    if (startState.drsReviewType == "EDGE") {
                        "🎙️ Wait, there is a clear sharp SPIKE on the Snickometer as the ball rolls past the edge! Direct deviation detected. We have bat involved!"
                    } else {
                        "🎙️ There is a prominent spike on the Snickometer! The ball clearly takes the inside edge before hitting the pad, making the LBW appeal completely void!"
                    }
                } else {
                    "🎙️ Completely flat line on the Snickometer... clean gap between the bat and the ball. Moving on..."
                }
                _uiState.update {
                    it.copy(drsLiveCommentary = spikeComment)
                }
                
                // --- Stage 2: For LBW, check Hawkeye Ball Tracking ---
                kotlinx.coroutines.delay(2000)
                val intermediateState = _uiState.value
                if (intermediateState.drsReviewType == "LBW" && !intermediateState.drsUltraEdgeHasSpike) {
                    _uiState.update {
                        it.copy(
                            drsCurrentStage = "BALL_TRACKING",
                            drsLiveCommentary = "🎙️ UltraEdge cleared, now spinning the Virtual Hawkeye Ball Tracking... let's trace the path of the ball."
                        )
                    }
                    
                    kotlinx.coroutines.delay(2200)
                    val pathComment = "🎙️ Hawkeye Ball-Tracking Active:\n" +
                            "▶ Length: ${intermediateState.drsBallTrackingLength}\n" +
                            "▶ Pitching position: ${intermediateState.drsBallTrackingPitching}\n" +
                            "▶ Impact position: ${intermediateState.drsBallTrackingImpact}\n" +
                            "▶ Wickets projection: ${intermediateState.drsBallTrackingWickets}"
                    _uiState.update {
                        it.copy(drsLiveCommentary = pathComment)
                    }
                    kotlinx.coroutines.delay(2000)
                }
            }
            
            // --- Stage 3: DECISION ---
            _uiState.update {
                it.copy(
                    drsCurrentStage = "DECISION",
                    drsLiveCommentary = "🎙️ Loading final review broadcast... Third Umpire has made the final decision."
                )
            }
            
            kotlinx.coroutines.delay(1800)
            
            // Determine final text based on final Decision
            val finalOutcomeOut = startState.drsFinalDecisionOut
            val outcomeText: String
            
            if (startState.drsIsBattingTeamReview) {
                outcomeText = if (finalOutcomeOut) {
                    "❌ REVIEW UNSUCCESSFUL!\nDecision Upheld: OUT!\n${startState.drsReviewerTeamName}'s review was unsuccessful. (Unlimited DRS Active)"
                } else {
                    "✅ REVIEW SUCCESSFUL!\nDecision Overturned: NOT OUT!\n${batsmanName} remains at the crease. Review is RETAINED!"
                }
            } else {
                outcomeText = if (finalOutcomeOut) {
                    "✅ REVIEW SUCCESSFUL!\nDecision Overturned: OUT!\n${batsmanName} must depart! Review is RETAINED!"
                } else {
                    "❌ REVIEW UNSUCCESSFUL!\nDecision Upheld: NOT OUT!\n${startState.drsReviewerTeamName}'s review was unsuccessful. (Unlimited DRS Active)"
                }
            }
            
            _uiState.update {
                it.copy(
                    drsIsCompleted = true,
                    drsLiveCommentary = outcomeText
                )
            }
        }
    }

    fun concludeDrsReview() {
        val state = _uiState.value
        val innings = activeInnings ?: return
        
        val strikerId = state.drsBatsmanId ?: return
        val bowlerId = state.drsBowlerId ?: return
        
        val currentStriker = innings.playersStats[strikerId] ?: return
        val currentBowler = innings.playersStats[bowlerId] ?: return
        val isTeamABatting = innings.battingTeam.name == state.teamAName
        
        val finalDecisionIsOut = state.drsFinalDecisionOut
        
        // Close the dialog
        _uiState.update {
            it.copy(
                showDrsDialog = false,
                drsIsRunning = false,
                drsIsCompleted = false,
                drsCurrentStage = "NONE"
            )
        }
        
        if (state.drsIsBattingTeamReview) {
            // Batting team review of an OUT decision
            if (finalDecisionIsOut) {
                // OUT was UPHELD: Batsman gets OUT!
                submitWicket() // Call submitWicket to officially record it!
            } else {
                // NOT OUT was OVERTURNED -> Batsman is saved!
                // Unlimited DRS, no need to refund
                _uiState.update {
                    it.copy(
                        liveAiCommentary = "🎙️ [vDRS Center] Overturned! The batsman ${currentStriker.name} is saved by a successful vDRS review! Unlimited DRS active."
                    )
                }
            }
        } else {
            // Bowling team appeals a NOT OUT decision (appeal for a wicket!)
            if (finalDecisionIsOut) {
                // OUT was OVERTURNED -> Batsman gets OUT!
                // Proceed to record wicket! Wait, we need to mark who is out.
                _uiState.update {
                    it.copy(
                        outBatsmanId = strikerId,
                        outBowlerId = bowlerId
                    )
                }
                submitWicket()
                _uiState.update {
                    it.copy(
                        liveAiCommentary = "🎙️ [vDRS Center] Overturned! The bowling team ${state.drsReviewerTeamName} gets a monumental wicket as the vDRS review reveals the batsman is OUT!"
                    )
                }
            } else {
                // NOT OUT was UPHELD -> Batsman is saved (Not Out)
                // Just record a dot ball
                recordNormalRuns(0)
                _uiState.update {
                    it.copy(
                        liveAiCommentary = "🎙️ [vDRS Center] Upheld! The appeal is struck down. The batsman ${currentStriker.name} remains NOT OUT! Unlimited DRS active."
                    )
                }
            }
        }
    }

    // --- Core Innings Checks & State Transitions ---

    private fun checkInningsProgress(runsInPlay: Int, forceSkipOverCheck: Boolean = false) {
        val state = _uiState.value
        val innings = activeInnings ?: return

        // 1. Strike Rotation check based on odd runs scoring
        if (runsInPlay == 1 || runsInPlay == 3) {
            rotateStrike()
        }

        // 2. Chasing Target victory condition (2nd innings)
        if (state.currentInningsIndex == 2 && state.targetToWin != null) {
            if (innings.runs >= state.targetToWin) {
                // Target successfully chased!
                wrapMatch()
                return
            }
        }

        // 3. Over Completed Check
        if (!forceSkipOverCheck && innings.balls > 0 && innings.balls % 6 == 0 && innings.currentOverSymbols.isNotEmpty()) {
            // 6 legal balls completed, over wraps up!
            val updatedStats = innings.playersStats.toMutableMap()
            val currentBowlerId = innings.activeBowlerId
            if (currentBowlerId != null) {
                val bowler = updatedStats[currentBowlerId]
                if (bowler != null) {
                    // Reset single over runs conceding
                    updatedStats[currentBowlerId] = bowler.copy(runsConcededThisOver = 0)
                }
            }

            // Save the current over run log to list
            val finalOverRuns = innings.runs - (innings.pastOversTotalRuns.lastOrNull() ?: 0)

            updateInningsState {
                it.copy(
                    currentOverSymbols = emptyList(), // clear for next bowler
                    pastOversTotalRuns = it.pastOversTotalRuns + innings.runs,
                    playersStats = updatedStats
                )
            }

            // Standard over ending: rotate batsmen endpoints
            rotateStrike()

            // Overs limit checked
            val isStandardMatch = state.matchSettings.matchType == MatchType.STANDARD
            val currentOversCount = innings.balls / 6
            if (isStandardMatch && currentOversCount >= state.matchSettings.maxOvers) {
                // Maximum overs bowled!
                wrapInnings()
            } else {
                // Prompt user to select a new bowler (bowler cannot bowl consecutive overs!)
                _uiState.update { it.copy(showBowlerSelection = true) }
            }
        }
    }

    private fun rotateStrike() {
        updateInningsState {
            val temp = it.strikerId
            it.copy(
                strikerId = it.nonStrikerId,
                nonStrikerId = temp
            )
        }
    }

    private fun updateLastBallDrs(
        eventDescription: String,
        strikerName: String,
        bowlerName: String,
        isWicket: Boolean,
        isWide: Boolean,
        runs: Int = 0
    ) {
        val px = (0.42f + Math.random().toFloat() * 0.16f) // center-aligned around stumps line (0.35f to 0.65f)
        val py = when {
            isWide -> 0.35f
            runs == 6 -> 0.25f // bouncer/loft length
            runs == 4 -> 0.45f
            isWicket -> 0.55f // key crashing good length
            else -> 0.65f // short length
        }
        
        val hitStatus = when {
            isWide -> "Wide"
            isWicket -> "Hitting"
            runs == 6 || runs == 4 -> "Over"
            else -> {
                // Randomly hitting or over the wickets
                if (Math.random() > 0.5) "Hitting" else "Over"
            }
        }

        val hindiCommentaryText = when {
            isWide -> {
                listOf(
                    "🎙️ [कमेंट्री] क्या लाजवाब रफ्तार, लेकिन गेंद काफी बाहर! अंपायर ने बाहें फैलाईं... ये वाइड गेंद है!",
                    "🎙️ [कमेंट्री] बाहर की तरफ जाती हुई तेज गेंद, बल्लेबाज के दायरे से पूरी तरह दूर। एक और वाइड बॉल!"
                ).random()
            }
            isWicket -> {
                listOf(
                    "🎙️ [कमेंट्री] अरे वाह! क्लीन बोल्ड! गिल्लियां बिखर गईं, बिल्कुल सटीक यॉर्कर थी! बल्लेबाज के पास कोई जवाब नहीं था!",
                    "🎙️ [कमेंट्री] सीधे विकेटों पर हमला! एलबीडब्ल्यू (LBW) की जोरदार अपील... और अंपायर ने उंगली उठा दी! क्या गज़ब की गेंदबाजी थी!"
                ).random()
            }
            runs == 6 -> {
                listOf(
                    "🎙️ [कमेंट्री] गगनचुंबी छक्का! गेंद सीधे दर्शकों के बीच जाकर गिरी! क्या बेहतरीन टाइमिंग थी बल्लेबाज की!",
                    "🎙️ [कमेंट्री] कमाल का शॉट! बल्लेबाज ने क्रीज का बखूबी इस्तेमाल किया और गेंद को हवा में उड़ा दिया छह रनों के लिए!"
                ).random()
            }
            runs == 4 -> {
                listOf(
                    "🎙️ [कमेंट्री] क्लासिक शॉट! गेंद गोली की रफ्तार से कवर्स बाउंड्री के पार चार रनों के लिए चली गई!",
                    "🎙️ [कमेंट्री] शॉट! शॉर्ट पिच गेंद का पूरा फायदा उठाया, पुल किया बेहतरीन तरीके से... चौका!"
                ).random()
            }
            else -> {
                listOf(
                    "🎙️ [कमेंट्री] बढ़िया रक्षात्मक शॉट, बल्लेबाज ने गेंद को सम्मान दिया। कोई रन नहीं मिला इस गेंद पर।",
                    "🎙️ [कमेंट्री] हल्के हाथों से खेलकर बल्लेबाज ने एक रन चुरा लिया, बेहतरीन तालमेल दोनों खिलाड़ियों के बीच।"
                ).random()
            }
        }

        _uiState.update {
            it.copy(
                lastBallDrsSimulation = DrsSimulationData(
                    batsmanName = strikerName,
                    bowlerName = bowlerName,
                    eventDescription = eventDescription,
                    pitchX = px,
                    pitchY = py,
                    isExtra = isWide || eventDescription.contains("No ball") || eventDescription.contains("Wide"),
                    isWide = isWide,
                    wicketHitStatus = hitStatus,
                    hindiCommentary = hindiCommentaryText,
                    finalDecisionOut = isWicket
                )
            )
        }
    }

    private fun updateInningsState(transform: (InningsState) -> InningsState) {
        if (_uiState.value.currentInningsIndex == 1) {
            _uiState.update { it.copy(innings1 = it.innings1?.let { state -> transform(state) }) }
        } else {
            _uiState.update { it.copy(innings2 = it.innings2?.let { state -> transform(state) }) }
        }
    }

    private fun wrapInnings() {
        val state = _uiState.value
        if (state.currentInningsIndex == 1) {
            val innings1 = state.innings1 ?: return
            
            // Mark Innings 1 complete
            val finishedInnings1 = innings1.copy(isCompleted = true)
            val index2Target = finishedInnings1.runs + 1

            // Setup Innings 2: role swapping
            val battingTeam = finishedInnings1.bowlingTeam
            val bowlingTeam = finishedInnings1.battingTeam

            // Re-initialize fresh stats map with reset states
            val playerStats = (battingTeam.players + bowlingTeam.players).associate { it.id to it.copy() }
            val striker = battingTeam.players.getOrNull(0)?.id
            val nonStriker = battingTeam.players.getOrNull(1)?.id
            val bowler = bowlingTeam.players.find { it.role == PlayerRole.BOWLER || it.role == PlayerRole.ALL_ROUNDER }?.id 
                ?: bowlingTeam.players.getOrNull(0)?.id

            val innings2 = InningsState(
                battingTeam = battingTeam,
                bowlingTeam = bowlingTeam,
                strikerId = striker,
                nonStrikerId = nonStriker,
                activeBowlerId = bowler,
                playersStats = playerStats
            )

            _uiState.update {
                it.copy(
                    innings1 = finishedInnings1,
                    innings2 = innings2,
                    currentInningsIndex = 2,
                    targetToWin = index2Target,
                    ballHistoryList = emptyList(),
                    liveAiCommentary = "🎙️ End of Innings 1! ${finishedInnings1.battingTeam.name} finishes with **${finishedInnings1.runs}/${finishedInnings1.wickets}** in ${finishedInnings1.oversString} overs. ${battingTeam.name} needs **$index2Target runs** to win the match!"
                )
            }
        } else {
            // Innings 2 is completed, wrap match!
            wrapMatch()
        }
    }

    private fun wrapMatch() {
        // Mark both completed and move screen to results
        val state = _uiState.value
        val in1 = state.innings1 ?: return
        val in2 = state.innings2 ?: return

        val finishedInnings1 = in1.copy(isCompleted = true)
        val finishedInnings2 = in2.copy(isCompleted = true)

        val team1Name = finishedInnings1.battingTeam.name
        val team2Name = finishedInnings2.battingTeam.name

        val t1Runs = finishedInnings1.runs
        val t2Runs = finishedInnings2.runs

        val matchWinnerResult: String
        val winnerTeamName: String

        if (t2Runs > t1Runs) {
            val wicketsLeft = finishedInnings2.battingTeam.players.size - 1 - finishedInnings2.wickets
            matchWinnerResult = "$team2Name won by $wicketsLeft wicket${if (wicketsLeft > 1) "s" else ""}!"
            winnerTeamName = team2Name
        } else if (t1Runs > t2Runs) {
            val runsDiff = t1Runs - t2Runs
            matchWinnerResult = "$team1Name won by $runsDiff run${if (runsDiff > 1) "s" else ""}!"
            winnerTeamName = team1Name
        } else {
            matchWinnerResult = "The match ended in an exciting TIE of $t1Runs runs each!"
            winnerTeamName = "Tie"
        }

        _uiState.update {
            it.copy(
                screen = GameScreen.SCORECARD,
                innings1 = finishedInnings1,
                innings2 = finishedInnings2,
                liveAiCommentary = "🎙️ Epic finish of the match! $matchWinnerResult Congratulations to all the players on an absolute cracker of a contest!"
            )
        }

        triggerMatchWrapUpCommentary(team1Name, team2Name, t1Runs, finishedInnings1.wickets, t2Runs, finishedInnings2.wickets, matchWinnerResult)
    }

    // --- AI Live Commentary Dispatchers ---

    private fun triggerAiCommentary(type: Int, batsmanName: String, bowlerName: String) {
        val state = _uiState.value
        val prompt = when (type) {
            4 -> {
                "Generate a quick 1-sentence live cricket commentary snippet (highly enthusiastic, TV-like) IN HINDI (using Devanagari script) for a BATSMAN hitting a magnificent FOUR. Batsman: $batsmanName, Bowler: $bowlerName. Keep it brief. FORMAT: Starts with \"🎙️ [AI हिन्दी कमेंट्री]\":"
            }
            6 -> {
                "Generate a quick 1-sentence live cricket commentary snippet (wildly ecstatic, TV-like) IN HINDI (using Devanagari script) for a BATSMAN hitting a massive SIX out of the stadium. Batsman: $batsmanName, Bowler: $bowlerName. Keep it brief. FORMAT: Starts with \"🎙️ [AI हिन्दी कमेंट्री]\":"
            }
            -1 -> {
                "Generate a quick 1-sentence live cricket commentary snippet (shocked, dramatic, TV-like) IN HINDI (using Devanagari script) for a BATSMAN getting OUT (caught or bowled by the bowler). Batsman: $batsmanName, Bowler: $bowlerName. Keep it brief. FORMAT: Starts with \"🎙️ [AI हिन्दी कमेंट्री]\":"
            }
            else -> return
        }

        _uiState.update { it.copy(isAiCommentaryLoading = true) }
        viewModelScope.launch {
            val fallback = when (type) {
                4 -> "🎙️ [हिन्दी कमेंट्री] \"शानदार शॉट! $batsmanName ने गेंदबाज $bowlerName की गेंद पर कवर्स के ऊपर से बेहतरीन चौका जड़ा!\""
                6 -> "🎙️ [हिन्दी कमेंट्री] \"गगनचुंबी छक्का! $batsmanName ने $bowlerName की गेंद को सीधे स्टेडियम के बाहर भेज दिया! छह रन!\""
                else -> "🎙️ [हिन्दी कमेंट्री] \"आउट! गेंदबाज $bowlerName की लाजवाब गेंद पर गिल्लियां बिखर गईं और $batsmanName को वापस जाना होगा!\""
            }
            val text = GeminiClient.getAiResponse(prompt, fallback)
            _uiState.update {
                it.copy(
                    isAiCommentaryLoading = false,
                    liveAiCommentary = text
                )
            }
        }
    }

    private fun triggerMatchWrapUpCommentary(
        t1Name: String, t2Name: String, t1Runs: Int, t1Wickets: Int,
        t2Runs: Int, t2Wickets: Int, result: String
    ) {
        val prompt = """
            You are a charismatic, legendary TV cricket commentator wrapping up a breathtaking final match.
            Summary of the match:
            - Team 1: $t1Name did $t1Runs/$t1Wickets.
            - Team 2: $t2Name did $t2Runs/$t2Wickets.
            - Result: $result
            
            Write an incredibly exciting, emotional post-match broadcast review of 4 sentences IN HINDI (using Devanagari script). Honor the champion team, describe the high-stakes pressure of the final overs, and conclude with a memorable closing quote about the beauty of cricket.
        """.trimIndent()

        _uiState.update { it.copy(isAiCommentaryLoading = true) }
        viewModelScope.launch {
            val fallback = "🎙️ [मैच रैप-अप] \"क्या रोमांचक मैच था! अंत तक दोनों टीमों ने बेहतरीन जज्बा दिखाया। $result यह शानदार मुकाबला इतिहास के पन्नों में दर्ज हो गया है! दोनों ही टीमों के खिलाड़ियों ने अद्भुत खेल का प्रदर्शन किया। हमारे साथ जुड़ने के लिए धन्यवाद!\""
            val text = GeminiClient.getAiResponse(prompt, fallback)
            _uiState.update {
                it.copy(
                    isAiCommentaryLoading = false,
                    liveAiCommentary = text
                )
            }
        }
    }

    // Reset whole game
    fun resetGame() {
        _uiState.update {
            MatchUiState(
                screen = GameScreen.TEAM_A_SETUP,
                teamAPlayers = it.teamAPlayers, // preserve existing player lists to prevent typing again
                teamBPlayers = it.teamBPlayers
            )
        }
    }
}
