package com.example.model

import java.util.UUID

enum class PlayerRole(val displayName: String) {
    BATSMAN("Batsman"),
    BOWLER("Bowler"),
    WICKETKEEPER("Wicketkeeper"),
    ALL_ROUNDER("All-rounder"),
    FIELDER("Fielder")
}

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val role: PlayerRole,
    val isCaptain: Boolean = false,
    val isViceCaptain: Boolean = false,
    
    // Batting stats
    val isOut: Boolean = false,
    val runsScored: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val dismissalInfo: String? = null, // e.g. "b BowlerName" or "c FielderName b BowlerName"
    
    // Bowling stats
    val ballsBowled: Int = 0,
    val runsConceded: Int = 0,
    val wicketsTaken: Int = 0,
    val runsConcededThisOver: Int = 0
) {
    val oversBowledString: String
        get() {
            val overs = ballsBowled / 6
            val remainingBalls = ballsBowled % 6
            return "$overs.$remainingBalls"
        }
    
    val economyRate: Double
        get() {
            if (ballsBowled == 0) return 0.0
            return (runsConceded.toDouble() / (ballsBowled.toDouble() / 6.0))
        }

    val strikeRate: Double
        get() {
            if (ballsFaced == 0) return 0.0
            return (runsScored.toDouble() / ballsFaced.toDouble()) * 100.0
        }
}

data class Team(
    val name: String,
    val accentColorHex: String, // e.g. Red, Blue, etc.
    val players: List<Player> = emptyList()
)

enum class MatchType {
    STANDARD,
    UNLIMITED
}

data class MatchSettings(
    val matchType: MatchType = MatchType.STANDARD,
    val maxOvers: Int = 5
)

enum class GameScreen {
    TEAM_A_SETUP, // Red Team
    TEAM_B_SETUP, // Second Team
    TOSS_SCREEN,
    GAMEPLAY,
    SCORECARD
}

data class BallHistoryEntry(
    val bowlerName: String,
    val batsmanName: String,
    val description: String, // e.g., "6 runs", "Wicket!", "Wide"
    val isExtra: Boolean,
    val isWicket: Boolean
)

enum class GameplayTab {
    SCORING,
    DRS,
    PLAYER_STATS
}

data class DrsSimulationData(
    val batsmanName: String = "Batsman",
    val bowlerName: String = "Bowler",
    val eventDescription: String = "Dot ball",
    val pitchX: Float = 0.5f,
    val pitchY: Float = 0.6f,
    val isExtra: Boolean = false,
    val isWide: Boolean = false,
    val wicketHitStatus: String = "Hitting", // "Hitting", "Over", "Wide"
    val hindiCommentary: String = "🎙️ [कमेंट्री] गेंद रक्षात्मक रूप से खेली गई, कोई रन नहीं।",
    val finalDecisionOut: Boolean = false
)

