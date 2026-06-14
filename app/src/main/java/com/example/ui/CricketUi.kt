package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.viewmodel.CricketViewModel
import com.example.viewmodel.InningsState
import com.example.viewmodel.MatchUiState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

// Professional Polish theme colors (creamy light warm background, sports cardinal red active color)
object CricketColors {
    val DeepSlate = Color(0xFFFDF8F7)       // Creamy off-white background #FDF8F7
    val CardBackground = Color(0xFFFFFFFF)  // Pure White cards
    val PrimaryRed = Color(0xFFB3261E)      // Deep Cricket Crimson Red #B3261E
    val DarkText = Color(0xFF0F172A)        // Charcoal Dark Slate #0F172A
    val MutedText = Color(0xFF64748B)       // Soft Slate #64748B
    val BorderRed = Color(0xFFFEE2E2)       // Light border tint #FEE2E2
    
    // Boundary 4s (Orange accent)
    val OrangeBg = Color(0xFFFFF7ED)
    val OrangeBorder = Color(0xFFFED7AA)
    val OrangeText = Color(0xFFC2410C)
    
    // Boundary 6s (Blue accent)
    val BlueBg = Color(0xFFEFF6FF)
    val BlueBorder = Color(0xFFBFDBFE)
    val BlueText = Color(0xFF1D4ED8)
    
    // Operational styling
    val LightGrayBg = Color(0xFFF8FAFC)
    val GrayBorder = Color(0xFFE2E8F0)
    val SoftWhite = Color(0xFFFAFAFA)       // Replaces original layout helper
    val BoundRed = Color(0xFFB3261E)
    val AccentBlue = Color(0xFF1D4ED8)
}

@Composable
fun CricketAppUi(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CricketColors.DeepSlate
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Elegant header logo bar matching Professional Polish
            HeaderBar(
                currentScreen = state.screen,
                titleText = when (state.screen) {
                    GameScreen.GAMEPLAY -> {
                        val innings = if (state.currentInningsIndex == 1) state.innings1 else state.innings2
                        if (innings != null) "${innings.battingTeam.name} vs ${innings.bowlingTeam.name}" else "LIVE SCORECARD"
                    }
                    GameScreen.SCORECARD -> "MATCH RESULTS WRAP-UP"
                    else -> "FANTASY CRICKET"
                },
                onResetClick = { viewModel.resetGame() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic screen loading based on state machine
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.screen) {
                    GameScreen.TEAM_A_SETUP -> TeamSetupScreen(
                        teamName = state.teamAName,
                        onTeamNameChange = { viewModel.updateTeamAName(it) },
                        newPlayerName = state.teamANewPlayerName,
                        onPlayerNameChange = { viewModel.updateTeamANewPlayerName(it) },
                        newPlayerRole = state.teamANewPlayerRole,
                        onRoleChange = { viewModel.updateTeamANewPlayerRole(it) },
                        isCaptain = state.teamANewPlayerIsCaptain,
                        onIsCaptainChange = { viewModel.updateTeamANewPlayerIsCaptain(it) },
                        isViceCaptain = state.teamANewPlayerIsViceCaptain,
                        onIsViceCaptainChange = { viewModel.updateTeamANewPlayerIsViceCaptain(it) },
                        addedPlayers = state.teamAPlayers,
                        onAddPlayer = { viewModel.addPlayerToTeamA() },
                        onRemovePlayer = { viewModel.removePlayerFromTeamA(it) },
                        onNextClick = { viewModel.setScreen(GameScreen.TEAM_B_SETUP) },
                        teamColorHex = "#FF3B30",
                        teamLabel = "RED TEAM (HOST)",
                        nextButtonTestTag = "confirm_team_a_button"
                    )

                    GameScreen.TEAM_B_SETUP -> TeamSetupScreen(
                        teamName = state.teamBName,
                        onTeamNameChange = { viewModel.updateTeamBName(it) },
                        newPlayerName = state.teamBNewPlayerName,
                        onPlayerNameChange = { viewModel.updateTeamBNewPlayerName(it) },
                        newPlayerRole = state.teamBNewPlayerRole,
                        onRoleChange = { viewModel.updateTeamBNewPlayerRole(it) },
                        isCaptain = state.teamBNewPlayerIsCaptain,
                        onIsCaptainChange = { viewModel.updateTeamBNewPlayerIsCaptain(it) },
                        isViceCaptain = state.teamBNewPlayerIsViceCaptain,
                        onIsViceCaptainChange = { viewModel.updateTeamBNewPlayerIsViceCaptain(it) },
                        addedPlayers = state.teamBPlayers,
                        onAddPlayer = { viewModel.addPlayerToTeamB() },
                        onRemovePlayer = { viewModel.removePlayerFromTeamB(it) },
                        onNextClick = { viewModel.setScreen(GameScreen.TOSS_SCREEN) },
                        teamColorHex = "#007AFF",
                        teamLabel = "BLUE TEAM (CHALLENGER)",
                        nextButtonTestTag = "confirm_team_b_button"
                    )

                    GameScreen.TOSS_SCREEN -> TossScreen(
                        state = state,
                        onTossClick = { viewModel.runTossWithAi() },
                        onMatchTypeChange = { viewModel.updateMatchType(it) },
                        onOversChange = { viewModel.updateMaxOvers(it) },
                        onStartMatch = { viewModel.startMatch() }
                    )

                    GameScreen.GAMEPLAY -> GameplayScreen(
                        state = state,
                        viewModel = viewModel
                    )

                    GameScreen.SCORECARD -> GrandScorecardScreen(
                        state = state,
                        onResetClick = { viewModel.resetGame() }
                    )
                }

                // Global Virtual DRS Overlay Dialog
                VirtualDrsOverlay(
                    state = state,
                    viewModel = viewModel
                )
            }
        }
    }
}

// --- Header Component ---
@Composable
fun HeaderBar(
    currentScreen: GameScreen,
    titleText: String,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CricketColors.PrimaryRed)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsCricket,
                    contentDescription = "Cricket Icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = titleText,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "IPL Scorer & AI Broadcaster",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f),
                letterSpacing = 0.5.sp
            )
        }

        if (currentScreen != GameScreen.TEAM_A_SETUP && currentScreen != GameScreen.TEAM_B_SETUP) {
            IconButton(
                onClick = onResetClick,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .size(36.dp)
                    .testTag("reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart scorer",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// --- Team Setup Screen ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamSetupScreen(
    teamName: String,
    onTeamNameChange: (String) -> Unit,
    newPlayerName: String,
    onPlayerNameChange: (String) -> Unit,
    newPlayerRole: PlayerRole,
    onRoleChange: (PlayerRole) -> Unit,
    isCaptain: Boolean,
    onIsCaptainChange: (Boolean) -> Unit,
    isViceCaptain: Boolean,
    onIsViceCaptainChange: (Boolean) -> Unit,
    addedPlayers: List<Player>,
    onAddPlayer: () -> Unit,
    onRemovePlayer: (String) -> Unit,
    onNextClick: () -> Unit,
    teamColorHex: String,
    teamLabel: String,
    nextButtonTestTag: String
) {
    var expandedRoleMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(teamColorHex)))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = teamLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CricketColors.MutedText,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = teamName,
                            onValueChange = onTeamNameChange,
                            label = { Text("Customize Team Name") },
                            textStyle = TextStyle(fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CricketColors.DarkText,
                                unfocusedTextColor = CricketColors.DarkText,
                                focusedBorderColor = CricketColors.PrimaryRed,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = CricketColors.PrimaryRed,
                                unfocusedLabelColor = CricketColors.MutedText
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("team_name_input")
                        )
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Add Players (Unlimited)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CricketColors.DarkText
                        )

                        OutlinedTextField(
                            value = newPlayerName,
                            onValueChange = onPlayerNameChange,
                            label = { Text("Player Name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CricketColors.DarkText,
                                unfocusedTextColor = CricketColors.DarkText,
                                focusedBorderColor = CricketColors.PrimaryRed,
                                unfocusedBorderColor = Color.LightGray,
                                focusedLabelColor = CricketColors.PrimaryRed,
                                unfocusedLabelColor = CricketColors.MutedText
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("player_name_input")
                        )

                        // Role selection dropdown replacement
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newPlayerRole.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Primary Speciality") },
                                trailingIcon = {
                                    IconButton(onClick = { expandedRoleMenu = true }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand role",
                                            tint = CricketColors.DarkText
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = CricketColors.DarkText,
                                    unfocusedTextColor = CricketColors.DarkText,
                                    focusedBorderColor = CricketColors.PrimaryRed,
                                    unfocusedBorderColor = Color.LightGray
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedRoleMenu = true }
                                    .testTag("role_dropdown_field")
                            )

                            DropdownMenu(
                                expanded = expandedRoleMenu,
                                onDismissRequest = { expandedRoleMenu = false },
                                modifier = Modifier.background(CricketColors.CardBackground)
                            ) {
                                PlayerRole.values().forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role.displayName, color = CricketColors.DarkText) },
                                        onClick = {
                                            onRoleChange(role)
                                            expandedRoleMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Captain & Vice Captain Selection check
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onIsCaptainChange(!isCaptain) }
                            ) {
                                Checkbox(
                                    checked = isCaptain,
                                    onCheckedChange = onIsCaptainChange,
                                    colors = CheckboxDefaults.colors(checkedColor = CricketColors.PrimaryRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Assign captain (C)", fontSize = 12.sp, color = CricketColors.DarkText)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onIsViceCaptainChange(!isViceCaptain) }
                            ) {
                                Checkbox(
                                    checked = isViceCaptain,
                                    onCheckedChange = onIsViceCaptainChange,
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Vice-captain (VC)", fontSize = 12.sp, color = CricketColors.DarkText)
                            }
                        }

                        Button(
                            onClick = onAddPlayer,
                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.PrimaryRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("add_player_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add player",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADD TO SQUAD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Squad list header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Squad (${addedPlayers.size} players)",
                        fontWeight = FontWeight.Bold,
                        color = CricketColors.DarkText,
                        fontSize = 14.sp
                    )
                }
            }

            if (addedPlayers.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No players added yet. Enter names above to proceed!",
                                color = CricketColors.MutedText,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(addedPlayers) { player ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                PlayerAvatar(
                                    role = player.role,
                                    isCaptain = player.isCaptain,
                                    isViceCaptain = player.isViceCaptain,
                                    size = 40.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = player.name,
                                            fontWeight = FontWeight.Bold,
                                            color = CricketColors.DarkText,
                                            fontSize = 14.sp
                                        )
                                        if (player.isCaptain) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "C",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(CricketColors.PrimaryRed, RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                        if (player.isViceCaptain) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "VC",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(Color(0xFFD97706), RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = player.role.displayName,
                                        fontSize = 11.sp,
                                        color = CricketColors.MutedText
                                    )
                                }
                            }

                            IconButton(onClick = { onRemovePlayer(player.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove player",
                                    tint = CricketColors.PrimaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNextClick,
            enabled = addedPlayers.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CricketColors.PrimaryRed,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(50.dp)
                .testTag(nextButtonTestTag)
        ) {
            Text(
                text = "NEXT SQUAD PROGRESS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Next screen",
                tint = Color.White
            )
        }
    }
}

// --- Toss and Match settings Screen ---
@Composable
fun TossScreen(
    state: MatchUiState,
    onTossClick: () -> Unit,
    onMatchTypeChange: (MatchType) -> Unit,
    onOversChange: (Int) -> Unit,
    onStartMatch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "AI Coins Toss Arena",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CricketColors.PrimaryRed,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFFFF3B30), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SportsCricket, "T1 Logo", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state.teamAName, fontWeight = FontWeight.Bold, color = CricketColors.DarkText, fontSize = 12.sp)
                            }

                            Text("VS", fontWeight = FontWeight.Bold, color = CricketColors.PrimaryRed, fontSize = 18.sp)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF007AFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SportsCricket, "T2 Logo", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state.teamBName, fontWeight = FontWeight.Bold, color = CricketColors.DarkText, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = onTossClick,
                            enabled = !state.isTossing,
                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.PrimaryRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("start_toss_button")
                        ) {
                            if (state.isTossing) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Casino, "Toss", tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SIMULATE AI COIN TOSS", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (state.isTossCompleted) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CricketColors.PrimaryRed.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, CricketColors.PrimaryRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "Speaker",
                                    tint = CricketColors.PrimaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "LIVE COIN TOSS UPDATE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = CricketColors.PrimaryRed
                                )
                            }
                            Text(
                                text = state.tossResult,
                                fontSize = 13.sp,
                                color = CricketColors.DarkText,
                                lineHeight = 18.sp,
                                style = LocalTextStyle.current
                            )
                        }
                    }
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Match Regulations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = CricketColors.DarkText
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AssistChip(
                                    onClick = { onMatchTypeChange(MatchType.STANDARD) },
                                    label = { Text("Standard Match") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = if (state.matchSettings.matchType == MatchType.STANDARD) CricketColors.PrimaryRed else CricketColors.DarkText,
                                        containerColor = if (state.matchSettings.matchType == MatchType.STANDARD) CricketColors.PrimaryRed.copy(alpha = 0.12f) else Color.Transparent
                                    )
                                )

                                AssistChip(
                                    onClick = { onMatchTypeChange(MatchType.UNLIMITED) },
                                    label = { Text("Unlimited Overs") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = if (state.matchSettings.matchType == MatchType.UNLIMITED) CricketColors.PrimaryRed else CricketColors.DarkText,
                                        containerColor = if (state.matchSettings.matchType == MatchType.UNLIMITED) CricketColors.PrimaryRed.copy(alpha = 0.12f) else Color.Transparent
                                    )
                                )
                            }

                            if (state.matchSettings.matchType == MatchType.STANDARD) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Maximum Overs to Bowl", fontSize = 12.sp, color = CricketColors.MutedText)
                                        Text("${state.matchSettings.maxOvers} Overs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CricketColors.PrimaryRed)
                                    }
                                    Slider(
                                        value = state.matchSettings.maxOvers.toFloat(),
                                        onValueChange = { onOversChange(it.toInt()) },
                                        valueRange = 1f..20f,
                                        steps = 19,
                                        colors = SliderDefaults.colors(
                                            thumbColor = CricketColors.PrimaryRed,
                                            activeTrackColor = CricketColors.PrimaryRed
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onStartMatch,
            enabled = state.isTossCompleted,
            colors = ButtonDefaults.buttonColors(
                containerColor = CricketColors.PrimaryRed,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(50.dp)
                .testTag("start_match_button")
        ) {
            Text("START MATCH GAMEPLAY", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.PlayArrow, "Play", tint = Color.White)
        }
    }
}

// Any app must display a clean scorecard during active matches
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameplayScreen(
    state: MatchUiState,
    viewModel: CricketViewModel
) {
    val innings = if (state.currentInningsIndex == 1) state.innings1 else state.innings2
    if (innings == null) return

    val battingTeam = innings.battingTeam
    val bowlingTeam = innings.bowlingTeam

    val striker = innings.playersStats[innings.strikerId]
    val nonStriker = innings.playersStats[innings.nonStrikerId]
    val currentBowler = innings.playersStats[innings.activeBowlerId]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Pinned live broadcast scoreboard Card - Carmine Red dynamic sport visual
        Card(
            colors = CardDefaults.cardColors(containerColor = CricketColors.PrimaryRed),
            shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = battingTeam.name.uppercase(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BATTING",
                            fontSize = 9.sp,
                            color = CricketColors.PrimaryRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "INNINGS ${state.currentInningsIndex}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${innings.runs} / ${innings.wickets}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )

                    Text(
                        text = "${innings.oversString} Overs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Extras: ${innings.extras}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    val runsRate = if (innings.balls > 0) {
                        (innings.runs.toDouble() / (innings.balls.toDouble() / 6.0))
                    } else 0.0

                    Text(
                        "CRR: ${String.format("%.2f", runsRate)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (state.currentInningsIndex == 2 && state.targetToWin != null) {
                    val runsNeeded = state.targetToWin - innings.runs
                    val ballsRemaining = if (state.matchSettings.matchType == MatchType.STANDARD) {
                        (state.matchSettings.maxOvers * 6) - innings.balls
                    } else null

                    HorizontalDivider(color = Color.White.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 4.dp))
                    
                    val targetDesc = if (ballsRemaining != null) {
                        "Need $runsNeeded runs off $ballsRemaining balls to win!"
                    } else {
                        "Need $runsNeeded runs in unlimited overs to win!"
                    }
                    
                    Text(
                        text = "TARGET: ${state.targetToWin}  |  $targetDesc",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Tab selection Row for dividing content into 3 dedicated areas
        TabRow(
            selectedTabIndex = state.activeTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = CricketColors.PrimaryRed,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[state.activeTab.ordinal]),
                    color = CricketColors.PrimaryRed
                )
            },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Tab(
                selected = state.activeTab == GameplayTab.SCORING,
                onClick = { viewModel.setGameplayTab(GameplayTab.SCORING) },
                text = { Text("SCORING", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.SportsCricket, contentDescription = "Scoring", modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = state.activeTab == GameplayTab.DRS,
                onClick = { viewModel.setGameplayTab(GameplayTab.DRS) },
                text = { Text("VIRTUAL DRS", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Videocam, contentDescription = "Virtual DRS", modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = state.activeTab == GameplayTab.PLAYER_STATS,
                onClick = { viewModel.setGameplayTab(GameplayTab.PLAYER_STATS) },
                text = { Text("MEASUREMENTS", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Equalizer, contentDescription = "Player Stats", modifier = Modifier.size(18.dp)) }
            )
        }

        // Active layout presentation
        Box(modifier = Modifier.weight(1f)) {
            when (state.activeTab) {
                GameplayTab.SCORING -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // AI Commentary ticker
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.PrimaryRed.copy(alpha = 0.04f)),
                                border = BorderStroke(1.dp, CricketColors.PrimaryRed.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(CricketColors.PrimaryRed, CircleShape)
                                            .size(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (state.isAiCommentaryLoading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.Campaign, "Live commentary", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = state.liveAiCommentary,
                                        color = CricketColors.DarkText,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Batting Summary Table
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Batsman", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, modifier = Modifier.weight(2f))
                                        Text("R", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                        Text("B", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                        Text("4s", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                        Text("6s", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                                        Text("SR", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                                    }

                                    HorizontalDivider(color = Color(0xFFF1F5F9))

                                    if (striker != null) {
                                        BatsmanRow(player = striker, isActive = true)
                                    }
                                    if (nonStriker != null) {
                                        BatsmanRow(player = nonStriker, isActive = false)
                                    }
                                }
                            }
                        }

                        // Bowling block
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            if (currentBowler != null) {
                                                PlayerAvatar(
                                                    role = currentBowler.role,
                                                    isCaptain = currentBowler.isCaptain,
                                                    isViceCaptain = currentBowler.isViceCaptain,
                                                    size = 36.dp,
                                                    borderWidth = 1.dp
                                                )
                                            }
                                            Column {
                                                Text("Bowler In Action", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 10.sp)
                                                Text(currentBowler?.name ?: "Select Bowler", fontWeight = FontWeight.Bold, color = CricketColors.AccentBlue, fontSize = 14.sp)
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("O", fontSize = 10.sp, color = CricketColors.MutedText)
                                                Text(currentBowler?.oversBowledString ?: "0.0", color = CricketColors.DarkText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("R", fontSize = 10.sp, color = CricketColors.MutedText)
                                                Text("${currentBowler?.runsConceded ?: 0}", color = CricketColors.DarkText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("W", fontSize = 10.sp, color = CricketColors.MutedText)
                                                Text("${currentBowler?.wicketsTaken ?: 0}", color = CricketColors.PrimaryRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Econ", fontSize = 10.sp, color = CricketColors.MutedText)
                                                Text(String.format("%.1f", currentBowler?.economyRate ?: 0.0), color = CricketColors.DarkText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFFF1F5F9))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("This Over:", fontSize = 11.sp, color = CricketColors.MutedText)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (innings.currentOverSymbols.isEmpty()) {
                                                Text("(Fresh Over)", color = CricketColors.MutedText, fontSize = 11.sp)
                                            } else {
                                                innings.currentOverSymbols.forEach { sym ->
                                                    Box(
                                                        modifier = Modifier
                                                            .background(
                                                                color = when (sym) {
                                                                    "W" -> CricketColors.PrimaryRed
                                                                    "6" -> CricketColors.BlueBg
                                                                    "4" -> CricketColors.OrangeBg
                                                                    "Wd", "Nb" -> Color(0xFFFEF08A)
                                                                    else -> Color(0xFFF1F5F9)
                                                                },
                                                                shape = RoundedCornerShape(6.dp)
                                                            )
                                                            .border(
                                                                width = 1.dp,
                                                                color = when (sym) {
                                                                    "W" -> CricketColors.PrimaryRed
                                                                    "6" -> CricketColors.BlueBorder
                                                                    "4" -> CricketColors.OrangeBorder
                                                                    "Wd", "Nb" -> Color(0xFFFACC15)
                                                                    else -> Color(0xFFE2E8F0)
                                                                },
                                                                shape = RoundedCornerShape(6.dp)
                                                            )
                                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = sym,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (sym) {
                                                                "W" -> Color.White
                                                                "6" -> CricketColors.BlueText
                                                                "4" -> CricketColors.OrangeText
                                                                "Wd", "Nb" -> Color(0xFF854D0E)
                                                                else -> CricketColors.DarkText
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Scoring Operational Buttons Deck
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Cricket Operations Deck",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = CricketColors.PrimaryRed
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.recordNormalRuns(0) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.LightGrayBg),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.weight(1f).height(42.dp).testTag("run_0_button")
                                        ) {
                                            Text("0", fontWeight = FontWeight.Bold, color = CricketColors.DarkText)
                                        }

                                        Button(
                                            onClick = { viewModel.recordNormalRuns(1) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.LightGrayBg),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.weight(1f).height(42.dp).testTag("run_1_button")
                                        ) {
                                            Text("1", fontWeight = FontWeight.Bold, color = CricketColors.DarkText)
                                        }

                                        Button(
                                            onClick = { viewModel.recordNormalRuns(2) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.LightGrayBg),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.weight(1f).height(42.dp).testTag("run_2_button")
                                        ) {
                                            Text("2", fontWeight = FontWeight.Bold, color = CricketColors.DarkText)
                                        }

                                        Button(
                                            onClick = { viewModel.recordNormalRuns(3) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.LightGrayBg),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.weight(1f).height(42.dp).testTag("run_3_button")
                                        ) {
                                            Text("3", fontWeight = FontWeight.Bold, color = CricketColors.DarkText)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.recordNormalRuns(4) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.OrangeBg),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, CricketColors.OrangeBorder),
                                            modifier = Modifier.weight(1f).height(42.dp).testTag("run_4_button")
                                        ) {
                                            Text("4 runs", fontWeight = FontWeight.Bold, color = CricketColors.OrangeText)
                                        }

                                        Button(
                                            onClick = { viewModel.recordNormalRuns(6) },
                                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.BlueBg),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, CricketColors.BlueBorder),
                                            modifier = Modifier.weight(1f).height(42.dp).testTag("run_6_button")
                                        ) {
                                            Text("6 runs", fontWeight = FontWeight.Bold, color = CricketColors.BlueText)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.recordExtraBall(isWide = true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF08A).copy(alpha = 0.15f), contentColor = Color(0xFF854D0E)),
                                            border = BorderStroke(1.dp, Color(0xFFFDE047)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(42.dp).testTag("wide_button")
                                        ) {
                                            Text("Wide (+0)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.recordExtraBall(isWide = false) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF08A).copy(alpha = 0.15f), contentColor = Color(0xFF854D0E)),
                                            border = BorderStroke(1.dp, Color(0xFFFDE047)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f).height(42.dp).testTag("no_ball_button")
                                        ) {
                                            Text("No-Ball (+1)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { viewModel.openOutDialog() },
                                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.PrimaryRed),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1.2f).height(42.dp).testTag("out_button")
                                        ) {
                                            Icon(Icons.Default.Dangerous, "Wicket", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("OUT WICKET", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // History logs
                        item {
                            Text(
                                text = "Running Ball Log",
                                fontWeight = FontWeight.Bold,
                                color = CricketColors.DarkText,
                                fontSize = 13.sp
                            )
                        }

                        if (state.ballHistoryList.isEmpty()) {
                            item {
                                Text(
                                    "No events yet. Record balls using the operations deck above!",
                                    color = CricketColors.MutedText,
                                    fontSize = 11.sp
                                )
                            }
                        } else {
                            items(state.ballHistoryList.take(15)) { entry ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    shape = RoundedCornerShape(10.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    entry.bowlerName,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CricketColors.AccentBlue,
                                                    fontSize = 11.sp
                                                )
                                                Text(" to ", color = CricketColors.MutedText, fontSize = 10.sp)
                                                Text(
                                                    entry.batsmanName,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CricketColors.DarkText,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(entry.description, color = CricketColors.MutedText, fontSize = 11.sp)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (entry.isWicket) CricketColors.PrimaryRed 
                                                            else if (entry.isExtra) Color(0xFFFEF08A) 
                                                            else if (entry.description.contains("6")) CricketColors.BlueBg
                                                            else if (entry.description.contains("4")) CricketColors.OrangeBg
                                                            else Color(0xFFF1F5F9),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (entry.isWicket) CricketColors.PrimaryRed
                                                            else if (entry.isExtra) Color(0xFFFACC15)
                                                            else if (entry.description.contains("6")) CricketColors.BlueBorder
                                                            else if (entry.description.contains("4")) CricketColors.OrangeBorder
                                                            else Color(0xFFE2E8F0),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (entry.isWicket) "W" 
                                                       else if (entry.description.contains("6")) "6" 
                                                       else if (entry.description.contains("4")) "4" 
                                                       else if (entry.isExtra) "Ex" 
                                                       else "•",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (entry.isWicket) Color.White 
                                                       else if (entry.description.contains("6")) CricketColors.BlueText
                                                       else if (entry.description.contains("4")) CricketColors.OrangeText
                                                       else if (entry.isExtra) Color(0xFF854D0E)
                                                       else CricketColors.DarkText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                GameplayTab.DRS -> {
                    // Incredible integrated Virtual DRS Replay Engine!
                    val drsBall = state.lastBallDrsSimulation
                    var replayStage by remember { mutableStateOf(1) }
                    var cameraAngleHoriz by remember { mutableStateOf(45f) }
                    var cameraAngleVert by remember { mutableStateOf(25f) }
                    var isOrbiting by remember { mutableStateOf(true) }
                    var animTick by remember { mutableStateOf(0f) }

                    // Sequential playback through stages
                    LaunchedEffect(drsBall) {
                        replayStage = 1
                        animTick = 0f
                        delay(2400)
                        replayStage = 2
                        animTick = 0f
                        delay(2400)
                        replayStage = 3
                    }

                    // Animation ticking loop for Stage 1 and Stage 2
                    LaunchedEffect(replayStage, drsBall) {
                        if (replayStage == 1 || replayStage == 2) {
                            animTick = 0f
                            val totalSteps = 100
                            for (step in 1..totalSteps) {
                                delay(16)
                                animTick = step / totalSteps.toFloat()
                            }
                        }
                    }

                    // Auto orbit animation coroutine for Stage 3
                    LaunchedEffect(isOrbiting) {
                        if (isOrbiting) {
                            while (true) {
                                delay(30)
                                cameraAngleHoriz = (cameraAngleHoriz + 1.2f) % 360f
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title header Card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "IPL-STYLE VIRTUAL DRS CENTER",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            "Active camera: ULTRA SLOW-MO STREAMS OK",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF4ADE80)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            replayStage = 1
                                            animTick = 0f
                                        },
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                            .size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            "Replay",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Playback stage ribbon indicators
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val stagesInfo = listOf(
                                    Triple(1, "STAGE 1", "गेंदबाज़ी और रन-अप"),
                                    Triple(2, "STAGE 2", "पिचिंग और टप्पा"),
                                    Triple(3, "STAGE 3", "3D रोटेशन और मैदान")
                                )
                                stagesInfo.forEach { (stgNum, label, hLabel) ->
                                    val isCurrent = replayStage == stgNum
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrent) Color(0xFF1E3A8A) else Color(0xFFF1F5F9)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { replayStage = stgNum },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(label, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) Color.White else Color(0xFF64748B))
                                            Text(hLabel, fontSize = 8.sp, color = if (isCurrent) Color.White.copy(alpha = 0.8f) else Color(0xFF475569))
                                        }
                                    }
                                }
                            }
                        }

                        // The Interactive Player Theater (2D of Batsman & Pitching -> 3D Hawkeye)
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(290.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Custom visual visualizer Canvas
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1.3f)
                                            .background(Color.Black)
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val w = size.width
                                            val h = size.height

                                            if (replayStage == 1) {
                                                // --- STAGE 1: BOWLER END RUN-UP & DEPARTURE FROM DISCHARGE CREASE ---
                                                // Grass turf base
                                                drawRect(color = Color(0xFF14532D), size = size)

                                                // Bowling end line crease at y = 82%
                                                drawLine(
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    start = Offset(0f, h * 0.82f),
                                                    end = Offset(w, h * 0.82f),
                                                    strokeWidth = 2f
                                                )

                                                // Batting end line crease at y = 30%
                                                drawLine(
                                                    color = Color.White.copy(alpha = 0.4f),
                                                    start = Offset(0f, h * 0.30f),
                                                    end = Offset(w, h * 0.30f),
                                                    strokeWidth = 1.5f
                                                )

                                                // Tiny distant stumps at batting end (y = 30%, centered)
                                                val midBatX = w * 0.5f
                                                for (i in -1..1) {
                                                    drawRect(
                                                        color = Color(0xFFFFD700),
                                                        topLeft = Offset(midBatX + (i * 7f) - 1.5f, h * 0.22f),
                                                        size = androidx.compose.ui.geometry.Size(3f, h * 0.08f)
                                                    )
                                                }

                                                // Stylized bowler starting or running in
                                                // Bowler starts from deep behind (run-up) and moves up towards discharge line
                                                val bowlerY = h * (0.96f - animTick * 0.14f)
                                                val bowlerX = w * 0.52f

                                                // Draw Bowler body representation
                                                drawLine(
                                                    color = Color(0xFF991B1B), // Bowl team Cardinal Jersey
                                                    start = Offset(bowlerX, bowlerY),
                                                    end = Offset(bowlerX, bowlerY - 25f),
                                                    strokeWidth = 4f
                                                )
                                                drawCircle(
                                                    color = Color(0xFFFCA5A5),
                                                    center = Offset(bowlerX, bowlerY - 29f),
                                                    radius = 4f
                                                )

                                                // Draw fielding members stationary positions ("where they sat")
                                                val fielders = listOf(
                                                    Triple(w * 0.18f, h * 0.48f, "Slips"),
                                                    Triple(w * 0.85f, h * 0.42f, "Gully"),
                                                    Triple(w * 0.48f, h * 0.16f, "Keeper")
                                                )
                                                fielders.forEach { (fx, fy, name) ->
                                                    drawCircle(color = Color(0xFF1E3A8A).copy(alpha = 0.6f), center = Offset(fx, fy), radius = 6f)
                                                    drawCircle(color = Color.White.copy(alpha = 0.8f), center = Offset(fx, fy), radius = 2.5f)
                                                }

                                                // Animating the ball leaving the bowler's hand towards batting end
                                                if (animTick > 0.6f) {
                                                    val ballProgress = (animTick - 0.6f) / 0.4f
                                                    val ballX = bowlerX + (midBatX - bowlerX) * ballProgress
                                                    val ballY = (bowlerY - 25f) + ((h * 0.38f) - (bowlerY - 25f)) * ballProgress
                                                    // Red cricket ball pulsing
                                                    drawCircle(color = Color(0xFFEF4444), center = Offset(ballX, ballY), radius = 4f)
                                                    drawCircle(color = Color.White.copy(alpha = 0.3f), center = Offset(ballX, ballY), radius = 7f)
                                                }

                                                // Boundary fence surroundings
                                                drawLine(
                                                    color = Color(0xFFFFCC00).copy(alpha = 0.2f),
                                                    start = Offset(0f, h * 0.08f),
                                                    end = Offset(w, h * 0.08f),
                                                    strokeWidth = 8f
                                                )
                                            } else if (replayStage == 2) {
                                                // --- STAGE 2: CLOSE-UP BATSMENT STANCE, IMPACT AND LANDING ---
                                                drawRect(color = Color(0xFF14532D), size = size)

                                                // Pitch line crease at y = 80% (close-up batting)
                                                drawLine(
                                                    color = Color.White.copy(alpha = 0.6f),
                                                    start = Offset(0f, h * 0.8f),
                                                    end = Offset(w, h * 0.8f),
                                                    strokeWidth = 4f
                                                )

                                                // Stumps centered
                                                val stumpsX = w * 0.5f
                                                val stumpTopY = h * 0.35f
                                                val stumpHeight = h * 0.45f
                                                for (i in -1..1) {
                                                    drawRoundRect(
                                                        color = Color(0xFFFFD700),
                                                        topLeft = Offset(stumpsX + (i * 15f) - 3f, stumpTopY),
                                                        size = androidx.compose.ui.geometry.Size(6f, stumpHeight),
                                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
                                                    )
                                                }
                                                // Bails
                                                drawRoundRect(
                                                    color = Color(0xFFFFCC00),
                                                    topLeft = Offset(stumpsX - 20f, stumpTopY - 5f),
                                                    size = androidx.compose.ui.geometry.Size(40f, 5f),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f)
                                                )

                                                // Batsman standing stance
                                                val batsmanX = stumpsX - 48f
                                                val batHeadY = h * 0.32f
                                                // Team Blue jersey batsman
                                                drawRect(color = Color(0xFF1D4ED8), topLeft = Offset(batsmanX - 10f, h * 0.45f), size = androidx.compose.ui.geometry.Size(20f, 40f))
                                                drawCircle(color = Color(0xFF1E3A8A), center = Offset(batsmanX, batHeadY), radius = 11f)
                                                // Leg pads
                                                drawRoundRect(color = Color.LightGray, topLeft = Offset(batsmanX - 8f, h * 0.65f), size = androidx.compose.ui.geometry.Size(7f, 32f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
                                                drawRoundRect(color = Color.LightGray, topLeft = Offset(batsmanX + 1f, h * 0.65f), size = androidx.compose.ui.geometry.Size(7f, 32f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))

                                                // Ball landing spot and movement
                                                val ballEndX = w * drsBall.pitchX
                                                val ballEndY = h * drsBall.pitchY
                                                val startX = w * 0.75f
                                                val startY = -40f

                                                val currentBallX = startX + (ballEndX - startX) * animTick
                                                val currentBallY = startY + (ballEndY - startY) * animTick

                                                // If ball has already pitched, show visual bounce footprint
                                                if (animTick > 0.6f) {
                                                    drawCircle(
                                                        color = Color(0xFFFBBF24).copy(alpha = 0.4f),
                                                        center = Offset(ballEndX, ballEndY),
                                                        radius = 16f * (animTick - 0.6f) / 0.4f
                                                    )
                                                    drawCircle(
                                                        color = Color.Red,
                                                        center = Offset(ballEndX, ballEndY),
                                                        radius = 4f
                                                    )
                                                }

                                                // Draw ball shadow & trace
                                                drawCircle(color = Color.Black.copy(alpha = 0.4f), center = Offset(currentBallX, currentBallY + 5f), radius = 4f)
                                                drawCircle(color = Color(0xFFEF4444), center = Offset(currentBallX, currentBallY), radius = 7f)
                                            } else {
                                                // --- STAGE 3: INTERACTIVE 3D HAWKEYE MODEL ENVIRONMENT ---
                                                // Dark galaxy star points background
                                                drawRect(color = Color(0xFF020617), size = size)

                                                // Math 3D helper projection logic
                                                val radH = (cameraAngleHoriz * kotlin.math.PI / 180f).toFloat()
                                                val radV = (cameraAngleVert * kotlin.math.PI / 180f).toFloat()

                                                fun project(x: Float, y: Float, z: Float): Offset {
                                                    // Rotation Y
                                                    val cosH = kotlin.math.cos(radH)
                                                    val sinH = kotlin.math.sin(radH)
                                                    val rx = x * cosH - z * sinH
                                                    val rz = x * sinH + z * cosH

                                                    // Rotation X (vertical tilt)
                                                    val cosV = kotlin.math.cos(radV)
                                                    val sinV = kotlin.math.sin(radV)
                                                    val ry = y * cosV - rz * sinV
                                                    val rzOutput = y * sinV + rz * cosV

                                                    // Screen project
                                                    val dist = 360f
                                                    val scale = dist / (dist + rzOutput)
                                                    return Offset(
                                                        x = (w / 2f) + rx * scale * 1.55f,
                                                        y = (h * 0.46f) - ry * scale * 1.45f
                                                    )
                                                }

                                                // 1. Draw boundary circle (outfield stadium surroundings)
                                                var prevRing: Offset? = null
                                                val segments = 16
                                                for (i in 0..segments) {
                                                    val radAngle = (i * 2 * kotlin.math.PI / segments).toFloat()
                                                    val sx = 230f * kotlin.math.cos(radAngle)
                                                    val sz = 230f * kotlin.math.sin(radAngle)
                                                    val projPt = project(sx, -35f, sz)
                                                    if (prevRing != null) {
                                                        drawLine(
                                                            color = Color(0xFF1E293B),
                                                            start = prevRing,
                                                            end = projPt,
                                                            strokeWidth = 2.5f
                                                        )
                                                    }
                                                    prevRing = projPt
                                                }

                                                // 2. Draw 4 towering floodlights around stadium
                                                val lights = listOf(
                                                    Pair(-190f, -190f), Pair(190f, -190f),
                                                    Pair(190f, 190f), Pair(-190f, 190f)
                                                )
                                                lights.forEach { light ->
                                                    val base3d = project(light.first, -35f, light.second)
                                                    val top3d = project(light.first, 85f, light.second)
                                                    drawLine(color = Color(0xFF334155), start = base3d, end = top3d, strokeWidth = 2.5f)
                                                    // Yellow led glow
                                                    drawCircle(color = Color(0xFFFBBF24).copy(alpha = 0.3f), center = top3d, radius = 9f)
                                                    drawCircle(color = Color.White, center = top3d, radius = 3.5f)
                                                }

                                                // 3. Draw Grass Pitch polygon bounds
                                                val gp1 = project(-32f, -35f, -135f)
                                                val gp2 = project(32f, -35f, -135f)
                                                val gp3 = project(32f, -35f, 135f)
                                                val gp4 = project(-32f, -35f, 135f)

                                                val gpPath = androidx.compose.ui.graphics.Path().apply {
                                                    moveTo(gp1.x, gp1.y)
                                                    lineTo(gp2.x, gp2.y)
                                                    lineTo(gp3.x, gp3.y)
                                                    lineTo(gp4.x, gp4.y)
                                                    close()
                                                }
                                                // Turf green pitch center fill
                                                drawPath(path = gpPath, color = Color(0xFF166534))
                                                drawLine(color = Color.White.copy(alpha = 0.4f), start = gp1, end = gp2, strokeWidth = 1.5f)
                                                drawLine(color = Color.White.copy(alpha = 0.4f), start = gp2, end = gp3, strokeWidth = 1f)
                                                drawLine(color = Color.White.copy(alpha = 0.4f), start = gp3, end = gp4, strokeWidth = 1.5f)
                                                drawLine(color = Color.White.copy(alpha = 0.4f), start = gp4, end = gp1, strokeWidth = 1f)

                                                // 4. White crease lines in 3D
                                                val creB1 = project(-32f, -35f, -108f)
                                                val creB2 = project(32f, -35f, -108f)
                                                drawLine(color = Color.White.copy(alpha = 0.6f), start = creB1, end = creB2, strokeWidth = 2f)

                                                val creF1 = project(-32f, -35f, 108f)
                                                val creF2 = project(32f, -35f, 108f)
                                                drawLine(color = Color.White.copy(alpha = 0.6f), start = creF1, end = creF2, strokeWidth = 2f)

                                                // 5. Bowler standing at bowling end ("जहां गेंदबाज़ी हुई")
                                                val bowB = project(10f, -35f, -120f)
                                                val bowT = project(10f, 12f, -120f)
                                                drawLine(color = Color(0xFFDC2626), start = bowB, end = bowT, strokeWidth = 6f)
                                                drawCircle(color = Color(0xFFFECACA), center = bowT, radius = 5f)

                                                // 6. Stumps at batting end (glowing neon hit/not hit status indicators)
                                                val isBallHitting = drsBall.wicketHitStatus == "Hitting"
                                                val stumpColor = if (isBallHitting) Color(0xFFEF4444) else Color(0xFF22C55E)
                                                val bailColor = if (isBallHitting) Color(0xFFFCA5A5) else Color(0xFFFFD700)

                                                val stumpsXList = listOf(-6.5f, 0f, 6.5f)
                                                stumpsXList.forEach { sx ->
                                                    val stB = project(sx, -35f, 118f)
                                                    val stT = project(sx, 7f, 118f)
                                                    drawLine(color = stumpColor, start = stB, end = stT, strokeWidth = 3f)
                                                }
                                                // Bails
                                                val bailPt1 = project(-7.5f, 8.5f, 118f)
                                                val bailPt2 = project(7.5f, 8.5f, 118f)
                                                drawLine(color = bailColor, start = bailPt1, end = bailPt2, strokeWidth = 2f)

                                                // 7. Batsman standing next to crease
                                                val bmB = project(-18f, -35f, 109f)
                                                val bmT = project(-18f, 12f, 109f)
                                                drawLine(color = Color(0xFF1D4ED8), start = bmB, end = bmT, strokeWidth = 7f)
                                                drawCircle(color = Color(0xFF93C5FD), center = bmT, radius = 5.5f)

                                                // 8. 3D BALL TRAJECTORY HAWKEYE PATH CURVE
                                                val pX = 32f * (2f * drsBall.pitchX - 1f)
                                                val pZ = 108f * (2f * drsBall.pitchY - 1f)

                                                val endBallX = if (drsBall.isWide) (if (pX < 0) -42f else 42f) else (pX * 0.4f)
                                                val endBallY = when (drsBall.wicketHitStatus) {
                                                    "Over" -> 22f
                                                    "Hitting" -> -14f
                                                    else -> -24f
                                                }
                                                val endBallZ = 120f

                                                // Pre-bounce curve
                                                var lastPt = project(10f, 10f, -120f)
                                                val stepsCount = 6
                                                for (step in 1..stepsCount) {
                                                    val ratio = step / stepsCount.toFloat()
                                                    val curX = 10f + (pX - 10f) * ratio
                                                    val curZ = -120f + (pZ - (-120f)) * ratio
                                                    val curY = -35f + (10f - (-35f)) * (1f - ratio) + 20f * (ratio * (1f - ratio))
                                                    val pPt = project(curX, curY, curZ)
                                                    drawLine(color = Color(0xFF22C55E).copy(alpha = 0.7f), start = lastPt, end = pPt, strokeWidth = 1.5f)
                                                    lastPt = pPt
                                                }

                                                // Footprint mark at pitch bounce landing
                                                val landingPt = project(pX, -35f, pZ)
                                                drawCircle(color = Color(0xFFFACC15).copy(alpha = 0.3f), center = landingPt, radius = 10f)
                                                drawCircle(color = Color(0xFFFBBF24), center = landingPt, radius = 3.5f)

                                                // Post-bounce predictive tracker curve
                                                var lastPostPt = landingPt
                                                val stepsPost = 8
                                                for (step in 1..stepsPost) {
                                                    val ratio = step / stepsPost.toFloat()
                                                    val curX = pX + (endBallX - pX) * ratio
                                                    val curZ = pZ + (endBallZ - pZ) * ratio
                                                    val curY = -35f + (endBallY - (-35f)) * ratio + 10f * (ratio * (1f - ratio))
                                                    val pPt = project(curX, curY, curZ)

                                                    drawLine(
                                                        color = if (isBallHitting) Color(0xFFEF4444) else Color(0xFF10B981),
                                                        start = lastPostPt,
                                                        end = pPt,
                                                        strokeWidth = 3f
                                                    )
                                                    if (step % 2 == 0) {
                                                        drawCircle(
                                                            color = if (isBallHitting) Color(0xFFEF4444) else Color(0xFF10B981),
                                                            center = pPt,
                                                            radius = 4.5f
                                                        )
                                                    }
                                                    lastPostPt = pPt
                                                }
                                            }
                                        }

                                        // Overlay camera label and info
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = when (replayStage) {
                                                    1 -> "🎥 STAGE 1: BOWLER END (क्रीज)"
                                                    2 -> "🎥 STAGE 2: BATSMAN INTERCEPT (टप्पा)"
                                                    else -> "🖥️ STAGE 3: LIVE 3D RECONSTRUCTION"
                                                },
                                                color = Color.White,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (replayStage == 3) Color(0xFF22C55E) else Color(0xFFFFD700))
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "3D STATUS: ACTIVE",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }

                                        // Verdict overlay on active render
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 8.dp)
                                        ) {
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (drsBall.finalDecisionOut) Color(0xFF991B1B).copy(alpha = 0.85f) else Color(0xFF166534).copy(alpha = 0.85f)
                                                ),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (drsBall.finalDecisionOut) "OUT" else {
                                                        if (drsBall.isWide) "WIDE BALL" else "NOT OUT"
                                                    },
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 16.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }
                                    }

                                    // Interactive 3D Orbit Deck for Stage 3 camera rotation
                                    if (replayStage == 3) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF0F172A))
                                                .padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "ROTATE 3D STADIUM CAMERA (कैमरा घुमाएं)",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF38BDF8)
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        "AUTO-ROTATE",
                                                        fontSize = 8.5.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Switch(
                                                        checked = isOrbiting,
                                                        onCheckedChange = { isOrbiting = it },
                                                        modifier = Modifier.scale(0.65f),
                                                        colors = SwitchDefaults.colors(
                                                            checkedThumbColor = Color(0xFF38BDF8),
                                                            checkedTrackColor = Color(0xFF0284C7)
                                                        )
                                                    )
                                                }
                                            }

                                            Slider(
                                                value = cameraAngleHoriz,
                                                onValueChange = {
                                                    cameraAngleHoriz = it
                                                    isOrbiting = false
                                                },
                                                valueRange = 0f..360f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = Color(0xFF38BDF8),
                                                    activeTrackColor = Color(0xFF0284C7),
                                                    inactiveTrackColor = Color(0xFF334155)
                                                ),
                                                modifier = Modifier.fillMaxWidth().height(24.dp)
                                            )

                                            // Quick Perspective Shortcuts
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                val buttons = listOf(
                                                    Pair("FRONT (सामने)", 0f),
                                                    Pair("SIDES (बगल)", 90f),
                                                    Pair("BACK (पीछे)", 180f),
                                                    Pair("BIRD'S EYE (ऊपर)", 135f)
                                                )
                                                buttons.forEach { (lbl, ang) ->
                                                    Button(
                                                        onClick = {
                                                            cameraAngleHoriz = ang
                                                            isOrbiting = false
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                                        modifier = Modifier.weight(1f).height(24.dp),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(lbl, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Stage 1 or 2 telemetry stats line
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF0F172A))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("PITCH", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = if (drsBall.isWide) "Outside Legs/Crease" else "In Line",
                                                    color = if (drsBall.isWide) Color(0xFFD97706) else Color(0xFF4ADE80),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("IMPACT", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = if (drsBall.isWide) "Wide of Bat" else "On Pads/Stumps",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("WICKETS", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = when (drsBall.wicketHitStatus) {
                                                        "Hitting" -> "Hitting Stumps"
                                                        "Over" -> "Missing Over"
                                                        else -> "Missing Wide"
                                                    }.uppercase(),
                                                    color = if (drsBall.wicketHitStatus == "Hitting") Color(0xFFEF4444) else Color(0xFF38BDF8),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Real-time status disclosure check
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("3D SIMULATION LIVE & INDEPENDENT DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                    Text(
                                        text = "घटना का 3D सिमुलेशन पूर्णत: सक्रिय और स्वतंत्र है। यह वास्तविक लाइव 3D पर्यावरण में लगातार सिमुलेट हो रहा है। आप किसी भी समय कैमरे को घुमाकर चारों दिशाओं - सामने (Front), पीछे (Back), और दोनों बगल (Sides/Off/Leg) और ऊपर से सुंदर स्टेडियम दृश्य देख सकते हैं।",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = CricketColors.DarkText
                                    )
                                }
                            }
                        }

                        // Hindi Subtitled live commentary text (prominent commentator card box)
                        item {
                            Card(
                                colors = CardColors(
                                    containerColor = CricketColors.PrimaryRed.copy(alpha = 0.05f),
                                    contentColor = CricketColors.DarkText,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = Color.Transparent
                                ),
                                border = BorderStroke(1.dp, CricketColors.PrimaryRed.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = "Hindi commentary voiceover",
                                        tint = CricketColors.PrimaryRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "🎙️ [हिन्दी कमेंट्री]",
                                            fontWeight = FontWeight.Bold,
                                            color = CricketColors.PrimaryRed,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = drsBall.hindiCommentary,
                                            color = CricketColors.DarkText,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // Last ball details info alert
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.LightGrayBg),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("LAST BALL DELIVERY DATA PROFILE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CricketColors.MutedText)
                                    Text(
                                        "Bowler ${drsBall.bowlerName} bowled to Striker ${drsBall.batsmanName}. Resulting outcome registered is: ${drsBall.eventDescription}.",
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        color = CricketColors.DarkText
                                    )
                                }
                            }
                        }

                        // Appeal manual challenges trigger
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.initiateDrsReview(isWicketReview = false, reviewType = "LBW") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.LiveTv, "LBW Challenge", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("LBW CHALLENGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = { viewModel.initiateDrsReview(isWicketReview = false, reviewType = "CATCH") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, "Catch Challenge", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CATCH REFEREE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                GameplayTab.PLAYER_STATS -> {
                    // Rich table layout showing all team members, role measurements, and metrics!
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title card
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.PrimaryRed.copy(alpha = 0.05f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Text("PLAYER MEASUREMENTS & LIVE DATA BOARDS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CricketColors.PrimaryRed)
                                    Text("Live statistics and metrics comparison of all registered cricketers in the match.", fontSize = 10.sp, color = CricketColors.MutedText)
                                }
                            }
                        }

                        // Batting Team scorecard list
                        item {
                            Text(
                                text = "BATTING TEAM: ${battingTeam.name.uppercase()} SCORES",
                                fontWeight = FontWeight.Bold,
                                color = CricketColors.DarkText,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        items(battingTeam.players) { player ->
                            val stats = innings.playersStats[player.id] ?: player
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        PlayerAvatar(
                                            role = player.role,
                                            isCaptain = player.isCaptain,
                                            isViceCaptain = player.isViceCaptain,
                                            size = 32.dp,
                                            borderWidth = 1.dp
                                        )
                                        Column {
                                            Text(player.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CricketColors.DarkText)
                                            Text(
                                                text = if (stats.isOut) "Dismissed: ${stats.dismissalInfo}" else "NOT OUT (BAT)",
                                                fontSize = 9.sp,
                                                color = if (stats.isOut) CricketColors.PrimaryRed else Color(0xFF22C55E),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("RUNS", fontSize = 8.sp, color = CricketColors.MutedText)
                                            Text("${stats.runsScored}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CricketColors.DarkText)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("BALLS", fontSize = 8.sp, color = CricketColors.MutedText)
                                            Text("${stats.ballsFaced}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CricketColors.MutedText)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.3f)) {
                                            Text("SR", fontSize = 8.sp, color = CricketColors.MutedText)
                                            val sr = if (stats.ballsFaced > 0) (stats.runsScored.toDouble() / stats.ballsFaced.toDouble()) * 100.0 else 0.0
                                            Text(String.format("%.1f", sr), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CricketColors.AccentBlue)
                                        }
                                    }
                                }
                            }
                        }

                        // Bowling Team measurements list
                        item {
                            Text(
                                text = "BOWLING TEAM: ${bowlingTeam.name.uppercase()} SPELLS",
                                fontWeight = FontWeight.Bold,
                                color = CricketColors.DarkText,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        items(bowlingTeam.players) { player ->
                            val stats = innings.playersStats[player.id] ?: player
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        PlayerAvatar(
                                            role = player.role,
                                            isCaptain = player.isCaptain,
                                            isViceCaptain = player.isViceCaptain,
                                            size = 32.dp,
                                            borderWidth = 1.dp
                                        )
                                        Column {
                                            Text(player.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CricketColors.DarkText)
                                            Text(player.role.displayName.uppercase(), fontSize = 9.sp, color = CricketColors.MutedText)
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1.1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("O", fontSize = 8.sp, color = CricketColors.MutedText)
                                            Text(stats.oversBowledString, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CricketColors.DarkText)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("CONCED", fontSize = 8.sp, color = CricketColors.MutedText)
                                            Text("${stats.runsConceded}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CricketColors.MutedText)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                            Text("WICKETS", fontSize = 8.sp, color = CricketColors.MutedText)
                                            Text("${stats.wicketsTaken}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CricketColors.PrimaryRed)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                                            Text("ECON", fontSize = 8.sp, color = CricketColors.MutedText)
                                            Text(String.format("%.1f", stats.economyRate), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CricketColors.AccentBlue)
                                        }
                                    }
                                }
                            }
                        }

                        // Comparative visual measurement metrics panel
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        "MATCH AGGREGATE RUN DISTRIBUTIONS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = CricketColors.DarkText
                                    )

                                    val totalBatsmenRuns = battingTeam.players.sumOf { (innings.playersStats[it.id] ?: it).runsScored }
                                    val totalBowlerRunsGiven = bowlingTeam.players.sumOf { (innings.playersStats[it.id] ?: it).runsConceded }

                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Batting runs produced", fontSize = 10.sp, color = CricketColors.MutedText)
                                                Text("$totalBatsmenRuns Runs", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            LinearProgressIndicator(
                                                progress = { if (totalBatsmenRuns + totalBowlerRunsGiven > 0) totalBatsmenRuns.toFloat() / (totalBatsmenRuns + totalBowlerRunsGiven).toFloat() else 0.5f },
                                                color = Color(0xFF10B981),
                                                trackColor = Color(0xFFE2E8F0),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                            )
                                        }

                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Bowling runs conceded", fontSize = 10.sp, color = CricketColors.MutedText)
                                                Text("$totalBowlerRunsGiven Runs", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                            LinearProgressIndicator(
                                                progress = { if (totalBatsmenRuns + totalBowlerRunsGiven > 0) totalBowlerRunsGiven.toFloat() / (totalBatsmenRuns + totalBowlerRunsGiven).toFloat() else 0.5f },
                                                color = CricketColors.PrimaryRed,
                                                trackColor = Color(0xFFE2E8F0),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Out Selector Dialog ---
    if (state.showOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelOutDialog() },
            containerColor = CricketColors.CardBackground,
            title = {
                Text("Confirm Player Wicket", color = CricketColors.PrimaryRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Batsman selection
                    Text("Select out Batsman:", fontSize = 12.sp, color = CricketColors.MutedText)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        striker?.let { player ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.outBatsmanId == player.id) CricketColors.PrimaryRed.copy(alpha = 0.1f) else CricketColors.LightGrayBg
                                ),
                                border = if (state.outBatsmanId == player.id) BorderStroke(1.dp, CricketColors.PrimaryRed) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectOutBatsman(player.id) }
                                    .testTag("out_striker_option")
                            ) {
                                Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        player.name + " (Striker)",
                                        color = if (state.outBatsmanId == player.id) CricketColors.PrimaryRed else CricketColors.DarkText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        nonStriker?.let { player ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.outBatsmanId == player.id) CricketColors.PrimaryRed.copy(alpha = 0.1f) else CricketColors.LightGrayBg
                                ),
                                border = if (state.outBatsmanId == player.id) BorderStroke(1.dp, CricketColors.PrimaryRed) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectOutBatsman(player.id) }
                                    .testTag("out_non_striker_option")
                            ) {
                                Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        player.name + " (Non-Striker)",
                                        color = if (state.outBatsmanId == player.id) CricketColors.PrimaryRed else CricketColors.DarkText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Bowl taker selection
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Select Wicket Taking Bowler:", fontSize = 12.sp, color = CricketColors.MutedText)
                    LazyColumn(
                        modifier = Modifier
                            .height(120.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(bowlingTeam.players) { player ->
                            val isSelected = state.outBowlerId == player.id
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CricketColors.AccentBlue.copy(alpha = 0.1f) else CricketColors.LightGrayBg
                                ),
                                border = if (isSelected) BorderStroke(1.dp, CricketColors.AccentBlue) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectOutBowler(player.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(player.name, color = if (isSelected) CricketColors.AccentBlue else CricketColors.DarkText, fontSize = 12.sp)
                                    Text(player.role.displayName, color = CricketColors.MutedText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val isTeamABatting = innings.battingTeam.name == state.teamAName
                val battingDrsLeft = if (isTeamABatting) state.teamADrsReviewsLeft else state.teamBDrsReviewsLeft
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.submitWicket() },
                            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.PrimaryRed),
                            modifier = Modifier.testTag("confirm_wicket_button")
                        ) {
                            Text("CONFIRM OUT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.initiateDrsReview(isWicketReview = true, reviewType = "EDGE") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)), // dark slate
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1f).testTag("batting_drs_edge_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GraphicEq, "Edge Check", tint = Color.White, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("EDGE (Snicko)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 7.5.sp)
                            }
                        }
                        
                        Button(
                            onClick = { viewModel.initiateDrsReview(isWicketReview = true, reviewType = "LBW") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)), // dark blue
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1f).testTag("batting_drs_lbw_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Radar, "LBW", tint = Color.White, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("LBW (Hawkeye)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 7.5.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.initiateDrsReview(isWicketReview = true, reviewType = "CATCH") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)), // rich green
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            modifier = Modifier.weight(1.1f).testTag("batting_drs_catch_button")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CameraAlt, "Catch Referee", tint = Color.White, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("CATCH (Ref)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 7.5.sp)
                            }
                        }
                    }
                    Text(
                        "Batting Team vDRS reviews: Unlimited", 
                        fontSize = 9.sp, 
                        color = CricketColors.MutedText, 
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelOutDialog() }) {
                    Text("Cancel", color = CricketColors.MutedText)
                }
            }
        )
    }

    // --- Next Batsman Selector Dialog ---
    if (state.showNewBatsmanSelection) {
        val remainingBatsmen = battingTeam.players.filter { player ->
            val stat = innings.playersStats[player.id] ?: player
            player.id != innings.strikerId && player.id != innings.nonStrikerId && !stat.isOut
        }

        AlertDialog(
            onDismissRequest = {}, // Force selection! Can't dismiss
            containerColor = CricketColors.CardBackground,
            title = {
                Text("Select Next Batsman", color = CricketColors.PrimaryRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select player to replace the dismissed batsman on the crease:", fontSize = 12.sp, color = CricketColors.MutedText)
                    LazyColumn(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(remainingBatsmen) { player ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.LightGrayBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectNewBatsman(player.id) }
                                    .testTag("new_batsman_option_${player.name.replace(" ", "_").lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        PlayerAvatar(
                                            role = player.role,
                                            isCaptain = player.isCaptain,
                                            isViceCaptain = player.isViceCaptain,
                                            size = 28.dp,
                                            borderWidth = 1.dp
                                        )
                                        Text(player.name, color = CricketColors.DarkText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Text(player.role.displayName, color = CricketColors.PrimaryRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {} // Handled via item click selection directly!
        )
    }

    // --- Next Bowler Selection Dialog ---
    if (state.showBowlerSelection) {
        val potentialBowlers = bowlingTeam.players.filter { it.id != innings.activeBowlerId }

        AlertDialog(
            onDismissRequest = {}, // Force bowler selection at over end!
            containerColor = CricketColors.CardBackground,
            title = {
                Text("Select Bowler for Next Over", color = CricketColors.PrimaryRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Over completed! Select bowler (rules prohibit consecutive overs from same bowler):", fontSize = 12.sp, color = CricketColors.MutedText)
                    LazyColumn(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(potentialBowlers) { player ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CricketColors.LightGrayBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectActiveBowler(player.id) }
                                    .testTag("new_bowler_option_${player.name.replace(" ", "_").lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        PlayerAvatar(
                                            role = player.role,
                                            isCaptain = player.isCaptain,
                                            isViceCaptain = player.isViceCaptain,
                                            size = 28.dp,
                                            borderWidth = 1.dp
                                        )
                                        Text(player.name, color = CricketColors.DarkText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Text(player.role.displayName, color = CricketColors.AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {} // item click logic
        )
    }
}

@Composable
fun PlayerAvatar(
    role: PlayerRole,
    isCaptain: Boolean = false,
    isViceCaptain: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 36.dp,
    borderWidth: androidx.compose.ui.unit.Dp = 1.5.dp
) {
    val drawableRes = when (role) {
        PlayerRole.BATSMAN -> R.drawable.img_avatar_batsman
        PlayerRole.BOWLER -> R.drawable.img_avatar_bowler
        PlayerRole.WICKETKEEPER -> R.drawable.img_avatar_wicketkeeper
        PlayerRole.ALL_ROUNDER -> R.drawable.img_avatar_allrounder
        PlayerRole.FIELDER -> R.drawable.img_avatar_generic
    }
    
    val borderColor = if (isCaptain) {
        CricketColors.PrimaryRed
    } else if (isViceCaptain) {
        Color(0xFFD97706)
    } else {
        Color(0xFFCBD5E1)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFF1F5F9))
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "Player Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun BatsmanRow(player: Player, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(2.2f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PlayerAvatar(
                role = player.role,
                isCaptain = player.isCaptain,
                isViceCaptain = player.isViceCaptain,
                size = 22.dp,
                borderWidth = 1.dp
            )
            Text(
                text = player.name + if (isActive) " *" else "",
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) CricketColors.PrimaryRed else CricketColors.DarkText,
                fontSize = 13.sp
            )
        }

        Text(
            text = "${player.runsScored}",
            fontWeight = FontWeight.Bold,
            color = CricketColors.DarkText,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.5f)
        )

        Text(
            text = "${player.ballsFaced}",
            color = CricketColors.MutedText,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.5f)
        )

        Text(
            text = "${player.fours}",
            color = CricketColors.MutedText,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.5f)
        )

        Text(
            text = "${player.sixes}",
            color = CricketColors.MutedText,
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            modifier = Modifier.weight(0.5f)
        )

        Text(
            text = String.format("%.1f", player.strikeRate),
            color = if (isActive) CricketColors.PrimaryRed else CricketColors.MutedText,
            textAlign = TextAlign.End,
            fontSize = 12.sp,
            modifier = Modifier.weight(0.8f)
        )
    }
}

// --- Grand Final Scorecard Page ---
@Composable
fun GrandScorecardScreen(
    state: MatchUiState,
    onResetClick: () -> Unit
) {
    val in1 = state.innings1 ?: return
    val in2 = state.innings2 ?: return

    var selectedInningsLog by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI commentator final broadcast summary
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CricketColors.PrimaryRed.copy(alpha = 0.04f)),
                    border = BorderStroke(1.dp, CricketColors.PrimaryRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = "Speaker",
                                tint = CricketColors.PrimaryRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI GAME BROADCAST WRAP-UP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = CricketColors.PrimaryRed
                            )
                        }
                        Text(
                            text = state.liveAiCommentary,
                            fontSize = 13.sp,
                            color = CricketColors.DarkText,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Quick totals Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Match Summary Box", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(in1.battingTeam.name, fontWeight = FontWeight.Bold, color = CricketColors.DarkText)
                            Text("${in1.runs} / ${in1.wickets} (${in1.oversString} Ov)", fontWeight = FontWeight.Bold, color = CricketColors.DarkText)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(in2.battingTeam.name, fontWeight = FontWeight.Bold, color = CricketColors.PrimaryRed)
                            Text("${in2.runs} / ${in2.wickets} (${in2.oversString} Ov)", fontWeight = FontWeight.Bold, color = CricketColors.PrimaryRed)
                        }
                    }
                }
            }

            // Toggle innings breakdown buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { selectedInningsLog = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedInningsLog == 1) CricketColors.PrimaryRed else CricketColors.LightGrayBg
                        ),
                        border = if (selectedInningsLog == 1) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Innings 1",
                            color = if (selectedInningsLog == 1) Color.White else CricketColors.DarkText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { selectedInningsLog = 2 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedInningsLog == 2) CricketColors.PrimaryRed else CricketColors.LightGrayBg
                        ),
                        border = if (selectedInningsLog == 2) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Innings 2",
                            color = if (selectedInningsLog == 2) Color.White else CricketColors.DarkText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            val currentLog = if (selectedInningsLog == 1) in1 else in2

            // Detailed Scorecard batting table
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "BATTER STATS (${currentLog.battingTeam.name.uppercase()})",
                                fontWeight = FontWeight.Bold,
                                color = CricketColors.PrimaryRed,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Batsman", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, modifier = Modifier.weight(2f))
                            Text("R", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                            Text("B", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                            Text("4s", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                            Text("6s", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.5f))
                            Text("SR", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        currentLog.battingTeam.players.forEach { origPlayer ->
                            val player = currentLog.playersStats[origPlayer.id] ?: origPlayer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(2f)
                                ) {
                                    PlayerAvatar(
                                        role = player.role,
                                        isCaptain = player.isCaptain,
                                        isViceCaptain = player.isViceCaptain,
                                        size = 24.dp,
                                        borderWidth = 1.dp
                                    )
                                    Column {
                                        Text(player.name, fontWeight = FontWeight.Bold, color = CricketColors.DarkText, fontSize = 12.sp)
                                        Text(
                                            text = if (player.isOut) player.dismissalInfo ?: "out" else "not out",
                                            fontSize = 10.sp,
                                            color = if (player.isOut) CricketColors.MutedText else CricketColors.AccentBlue
                                        )
                                    }
                                }

                                Text("${player.runsScored}", fontWeight = FontWeight.Bold, color = CricketColors.DarkText, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.weight(0.5f))
                                Text("${player.ballsFaced}", color = CricketColors.MutedText, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.weight(0.5f))
                                Text("${player.fours}", color = CricketColors.MutedText, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.weight(0.5f))
                                Text("${player.sixes}", color = CricketColors.MutedText, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.weight(0.5f))
                                Text(String.format("%.1f", player.strikeRate), color = CricketColors.MutedText, textAlign = TextAlign.End, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                            }
                        }
                    }
                }
            }

            // Detailed Scorecard bowling table
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CricketColors.CardBackground),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "BOWLER STATS (${currentLog.bowlingTeam.name.uppercase()})",
                                fontWeight = FontWeight.Bold,
                                color = CricketColors.AccentBlue,
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bowler", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, modifier = Modifier.weight(2f))
                            Text("O", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
                            Text("R", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
                            Text("W", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
                            Text("Econ", fontWeight = FontWeight.Bold, color = CricketColors.MutedText, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        currentLog.bowlingTeam.players.forEach { origPlayer ->
                            val player = currentLog.playersStats[origPlayer.id] ?: origPlayer
                            // Only show if bowler actually bowled at least 1 ball (to keep it clean under unlimited players)
                            if (player.ballsBowled > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(2f)
                                    ) {
                                        PlayerAvatar(
                                            role = player.role,
                                            isCaptain = player.isCaptain,
                                            isViceCaptain = player.isViceCaptain,
                                            size = 24.dp,
                                            borderWidth = 1.dp
                                        )
                                        Text(player.name, fontWeight = FontWeight.Bold, color = CricketColors.DarkText, fontSize = 12.sp)
                                    }
                                    Text(player.oversBowledString, color = CricketColors.DarkText, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                                    Text("${player.runsConceded}", color = CricketColors.DarkText, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                                    Text("${player.wicketsTaken}", fontWeight = FontWeight.Bold, color = CricketColors.PrimaryRed, textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.weight(0.6f))
                                    Text(String.format("%.2f", player.economyRate), color = CricketColors.MutedText, textAlign = TextAlign.End, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onResetClick,
            colors = ButtonDefaults.buttonColors(containerColor = CricketColors.PrimaryRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(50.dp)
                .testTag("scorecard_restart_button")
        ) {
            Icon(Icons.Default.Refresh, "Restart", tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("START BRAND NEW TOURNAMENT MATCH", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// --- Virtual Decision Review System (vDRS) Control Room Composable Overlay ---
@Composable
fun VirtualDrsOverlay(
    state: MatchUiState,
    viewModel: CricketViewModel
) {
    if (!state.showDrsDialog) return

    var drsActiveView3D by remember { mutableStateOf(false) }
    var drsCameraYaw by remember { mutableStateOf(45f) } // default dynamic isometric-like perspective
    var drsCameraPitch by remember { mutableStateOf(20f) }
    var isOrbiting by remember { mutableStateOf(false) }
    var ballAnimTime by remember { mutableStateOf(0f) }

    // Live-running ball path animation ticker (runs while BALL_TRACKING stage remains active)
    LaunchedEffect(state.drsCurrentStage) {
        if (state.drsCurrentStage == "BALL_TRACKING") {
            ballAnimTime = 0f
            while (true) {
                delay(25)
                ballAnimTime += 0.012f
                if (ballAnimTime > 1.25f) {
                    ballAnimTime = 0f
                }
            }
        }
    }

    // Camera 360-degree sweep dynamic sweep ticker
    LaunchedEffect(isOrbiting) {
        if (isOrbiting) {
            while (isOrbiting) {
                delay(30)
                drsCameraYaw = (drsCameraYaw + 1.2f) % 360f
            }
        }
    }

    Dialog(onDismissRequest = {}) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Deep Cyber Navy Slate
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, if (state.drsIsCompleted) {
                if (state.drsFinalDecisionOut) Color(0xFFF87171) else Color(0xFF4ADE80)
            } else Color(0xFF38BDF8)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar LED Flash
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (state.drsIsCompleted) Color(0xFFE11D48) else Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "vDRS CONTROL ROOM",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "LIVE FEED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier
                            .background(Color(0xFF0284C7).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                // Review info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "CHALLENGED BY: ${state.drsReviewerTeamName.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "DECISION UNDER REVIEW: ${state.drsReviewType} APPEAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFBBF24)
                    )
                }

                // Workflow steps status bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stepsList = if (state.drsReviewType == "EDGE") {
                        listOf("INIT", "ULTRAEDGE", "DECISION")
                    } else if (state.drsReviewType == "CATCH") {
                        listOf("INIT", "REFCAM", "DECISION")
                    } else {
                        listOf("INIT", "ULTRAEDGE", "HAWKEYE", "DECISION")
                    }
                    
                    stepsList.forEachIndexed { _, step ->
                        val isActive = when (state.drsCurrentStage) {
                            "INITIATING" -> step == "INIT"
                            "ULTRA_EDGE" -> step == "ULTRAEDGE"
                            "CAMERA_CATCH_CHECK" -> step == "REFCAM"
                            "BALL_TRACKING" -> step == "HAWKEYE"
                            "DECISION" -> step == "DECISION"
                            else -> false
                        }
                        
                        val isFinished = when (state.drsCurrentStage) {
                            "ULTRA_EDGE" -> step == "INIT"
                            "CAMERA_CATCH_CHECK" -> step == "INIT"
                            "BALL_TRACKING" -> step == "INIT" || step == "ULTRAEDGE"
                            "DECISION" -> step != "DECISION"
                            else -> false
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) Color(0xFF0284C7) 
                                                 else if (isFinished) Color(0xFF1E293B) 
                                                 else Color(0xFF0F172A)
                            ),
                            border = BorderStroke(
                                1.dp, 
                                if (isActive) Color(0xFF38BDF8) 
                                else if (isFinished) Color(0xFF334155) 
                                else Color.White.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = step,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.White 
                                            else if (isFinished) Color(0xFF22C55E) 
                                            else Color(0xFF64748B),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Central interactive calibration animation card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Slate Dark
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when (state.drsCurrentStage) {
                            "INITIATING" -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(11.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF38BDF8),
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        "SYNCHRONIZING FEED CLOCKS...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        "Calibrating micro-audio and telemetry channels",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            "ULTRA_EDGE" -> {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = "Snicko Feed",
                                                tint = Color(0xFFE11D48),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "SNICKOMETER ULTRAEDGE FEED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = if (state.drsUltraEdgeHasSpike) "SPIKE DETECTED" else "FLAT LINE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (state.drsUltraEdgeHasSpike) Color(0xFFF87171) else Color(0xFF4ADE80),
                                            modifier = Modifier
                                                .background(
                                                    (if (state.drsUltraEdgeHasSpike) Color(0xFFEF4444) else Color(0xFF22C55E)).copy(alpha = 0.15f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Render Waveform Canvas!
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .background(Color(0xFF0F172A).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    ) {
                                        val width = size.width
                                        val height = size.height
                                        val centerY = height / 2f
                                        val points = state.drsUltraEdgeWaveform
                                        val step = width / (points.size.coerceAtLeast(2) - 1)

                                        // Draw horizontal zero line
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.15f),
                                            start = Offset(0f, centerY),
                                            end = Offset(width, centerY),
                                            strokeWidth = 1f
                                        )

                                        // Draw snicko wave
                                        if (points.isNotEmpty()) {
                                            for (i in 0 until points.size - 1) {
                                                val startX = i * step
                                                val startY = centerY - (points[i] * (height / 2.5f))
                                                val endX = (i + 1) * step
                                                val endY = centerY - (points[i + 1] * (height / 2.5f))

                                                drawLine(
                                                    color = if (state.drsUltraEdgeHasSpike && i in 20..30) Color(0xFFF87171) else Color(0xFF2DD4BF),
                                                    start = Offset(startX, startY),
                                                    end = Offset(endX, endY),
                                                    strokeWidth = if (state.drsUltraEdgeHasSpike && i in 20..30) 2.5f else 1.5f
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            "CAMERA_CATCH_CHECK" -> {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Camera Ref",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "CAMERA REF LIVE ZOOM (ANGLE #4)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = if (state.drsUltraCleanCatchCheck) "CLEAN CATCH" else "GROUNDED / NOT CLEAN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (state.drsUltraCleanCatchCheck) Color(0xFF4ADE80) else Color(0xFFF87171),
                                            modifier = Modifier
                                                .background(
                                                    (if (state.drsUltraCleanCatchCheck) Color(0xFF22C55E) else Color(0xFFEF4444)).copy(alpha = 0.15f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    // Display Simulated Live video playback grid!
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .background(Color.Black, RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                            .padding(6.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("🔴 REPLAY LIVE [500FPS]", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                                                Text("CATCH CHECK: ${state.drsCatchFielderName.uppercase()}", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Draw stylized schematics for catcher
                                            Box(
                                                modifier = Modifier.fillMaxWidth().weight(1f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = if (state.drsUltraCleanCatchCheck) Icons.Default.ThumbUp else Icons.Default.ThumbDown,
                                                        contentDescription = "Check Status",
                                                        tint = if (state.drsUltraCleanCatchCheck) Color(0xFF4ADE80) else Color(0xFFF87171),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = if (state.drsUltraCleanCatchCheck) "FINGERS UNDER THE BALL" else "BALL CONTACTED TURF FIRST",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White
                                                    )
                                                    Text(
                                                        text = "ZOOM EXTRA 400% CALIBRATION - APPROVED",
                                                        fontSize = 8.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("CALIBRATION OK", color = Color(0xFF4ADE80), fontSize = 8.sp)
                                                Text("SYSTEM ACTIVE", color = Color(0xFF4ADE80), fontSize = 8.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            "BALL_TRACKING" -> {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Header controls (With option to retire from default bowling maps and look in 3D orbit)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Radar,
                                                contentDescription = "Hawkeye",
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (drsActiveView3D) "HAWKEYE 3D SPACE ORBIT" else "HAWKEYE TELEMETRY PITCH MAP",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        // Operational Switcher (Retire from Bowled Zone Screen into 3D Simulation view)
                                        Row(
                                            modifier = Modifier
                                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                .padding(2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (!drsActiveView3D) Color(0xFF1E293B) else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        drsActiveView3D = false
                                                        isOrbiting = false
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    "2D Wicket Spot",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (!drsActiveView3D) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (drsActiveView3D) Color(0xFF1E293B) else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        drsActiveView3D = true
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    "Immersive 3D Space",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (drsActiveView3D) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                                )
                                            }
                                        }
                                    }

                                    // Display depending on whether we active-look in 3D or are looking at the traditional flat pitch tracking
                                    if (drsActiveView3D) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Left: Immersive 3D Canvas
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                                modifier = Modifier.weight(1.3f).fillMaxHeight()
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                                        val w = size.width
                                                        val h = size.height

                                                        // Helper to project 3D to 2D
                                                        fun project3D(x: Float, y: Float, z: Float, yaw: Float, pitch: Float): Offset {
                                                            val yawRad = Math.toRadians(yaw.toDouble()).toFloat()
                                                            val pitchRad = Math.toRadians(pitch.toDouble()).toFloat()

                                                            // 1. Yaw rotation (Y-axis)
                                                            val x1 = x * kotlin.math.cos(yawRad) - z * kotlin.math.sin(yawRad)
                                                            val z1 = x * kotlin.math.sin(yawRad) + z * kotlin.math.cos(yawRad)
                                                            val y1 = y

                                                            // 2. Pitch rotation (X-axis)
                                                            val x2 = x1
                                                            val y2 = y1 * kotlin.math.cos(pitchRad) - z1 * kotlin.math.sin(pitchRad)
                                                            val z2 = y1 * kotlin.math.sin(pitchRad) + z1 * kotlin.math.cos(pitchRad)

                                                            // 3. Perspective Projection details
                                                            val cameraDistance = 14f
                                                            val perspectiveFactor = 280f
                                                            val safeDenom = if (cameraDistance + z2 < 1.0f) 1.0f else (cameraDistance + z2)
                                                            val scale = perspectiveFactor / safeDenom

                                                            return Offset((w / 2f) + x2 * scale, (h / 2f) - y2 * scale)
                                                        }

                                                        // Draw Outfield boundary circle (stadium surroundings)
                                                        val boundaryPoints = 20
                                                        val boundaryRadius = 7.8f
                                                        val boundaryPath = Path()
                                                        for (i in 0..boundaryPoints) {
                                                            val angle = (i * 2 * Math.PI / boundaryPoints).toFloat()
                                                            val bx = boundaryRadius * kotlin.math.cos(angle)
                                                            val bz = boundaryRadius * kotlin.math.sin(angle)
                                                            val proj = project3D(bx, -0.05f, bz, drsCameraYaw, drsCameraPitch)
                                                            if (i == 0) {
                                                                boundaryPath.moveTo(proj.x, proj.y)
                                                            } else {
                                                                boundaryPath.lineTo(proj.x, proj.y)
                                                            }
                                                        }
                                                        boundaryPath.close()
                                                        drawPath(boundaryPath, color = Color(0xFF0F3E1E)) // Dark grass outfield field
                                                        drawPath(boundaryPath, color = Color(0xFF22C55E).copy(alpha = 0.4f), style = Stroke(width = 2.5f)) // Boundary rope

                                                        // Draw the central Clay Wicket Pitch Strip
                                                        val pitchCorners = listOf(
                                                            Offset(-1.1f, -10f),
                                                            Offset(1.1f, -10f),
                                                            Offset(1.1f, 10f),
                                                            Offset(-1.1f, 10f)
                                                        )
                                                        val pitchPath = Path()
                                                        pitchCorners.forEachIndexed { i, pt ->
                                                            val proj = project3D(pt.x, -0.01f, pt.y, drsCameraYaw, drsCameraPitch)
                                                            if (i == 0) pitchPath.moveTo(proj.x, proj.y) else pitchPath.lineTo(proj.x, proj.y)
                                                        }
                                                        pitchPath.close()
                                                        drawPath(pitchPath, color = Color(0xFFD9B980)) // Sand pitch clay body
                                                        drawPath(pitchPath, color = Color(0xFFE5C590).copy(alpha = 0.5f), style = Stroke(width = 1f)) // Crease outline

                                                        // Crease Lines
                                                        val creaseLeft = project3D(-1.1f, 0f, 9.0f, drsCameraYaw, drsCameraPitch)
                                                        val creaseRight = project3D(1.1f, 0f, 9.0f, drsCameraYaw, drsCameraPitch)
                                                        drawLine(color = Color.White.copy(alpha = 0.8f), start = creaseLeft, end = creaseRight, strokeWidth = 1.5f)

                                                        val bCreaseLeft = project3D(-1.1f, 0f, -9.0f, drsCameraYaw, drsCameraPitch)
                                                        val bCreaseRight = project3D(1.1f, 0f, -9.0f, drsCameraYaw, drsCameraPitch)
                                                        drawLine(color = Color.White.copy(alpha = 0.8f), start = bCreaseLeft, end = bCreaseRight, strokeWidth = 1.5f)

                                                        // Draw Sight Screen panel at the back surroundings (Bowler view boundary)
                                                        val ss1 = project3D(-1.8f, 0f, 11.5f, drsCameraYaw, drsCameraPitch)
                                                        val ss2 = project3D(1.8f, 0f, 11.5f, drsCameraYaw, drsCameraPitch)
                                                        val ss3 = project3D(1.8f, 1.8f, 11.5f, drsCameraYaw, drsCameraPitch)
                                                        val ss4 = project3D(-1.8f, 1.8f, 11.5f, drsCameraYaw, drsCameraPitch)
                                                        val ssPath = Path()
                                                        ssPath.moveTo(ss1.x, ss1.y)
                                                        ssPath.lineTo(ss2.x, ss2.y)
                                                        ssPath.lineTo(ss3.x, ss3.y)
                                                        ssPath.lineTo(ss4.x, ss4.y)
                                                        ssPath.close()
                                                        drawPath(ssPath, color = Color(0xFF334155).copy(alpha = 0.7f)) // Slate Sight Screen backdrop

                                                        // Draw Batsman Wickets Stumps (Target) at z = 10.0f
                                                        val sxPos = listOf(-0.15f, 0f, 0.15f)
                                                        sxPos.forEach { sx ->
                                                            val sBase = project3D(sx, 0f, 10.0f, drsCameraYaw, drsCameraPitch)
                                                            val sTip = project3D(sx, 0.73f, 10.0f, drsCameraYaw, drsCameraPitch)
                                                            drawLine(
                                                                color = Color(0xFFFF3366), // Glowing neon pink modern wickets
                                                                start = sBase,
                                                                end = sTip,
                                                                strokeWidth = 3f
                                                            )
                                                        }
                                                        val bailL = project3D(-0.16f, 0.73f, 10.0f, drsCameraYaw, drsCameraPitch)
                                                        val bailR = project3D(0.16f, 0.73f, 10.0f, drsCameraYaw, drsCameraPitch)
                                                        drawLine(color = Color(0xFFFFEE55), start = bailL, end = bailR, strokeWidth = 2f)

                                                        // Draw Bowler Wickets Stumps on the other end
                                                        sxPos.forEach { sx ->
                                                            val sBase = project3D(sx, 0f, -10.0f, drsCameraYaw, drsCameraPitch)
                                                            val sTip = project3D(sx, 0.73f, -10.0f, drsCameraYaw, drsCameraPitch)
                                                            drawLine(
                                                                color = Color(0xFF64748B),
                                                                start = sBase,
                                                                end = sTip,
                                                                strokeWidth = 2f
                                                            )
                                                        }

                                                        // Mapped Ball-Tracking live parabolic path
                                                        val x_rel = -0.4f
                                                        val y_rel = 2.1f
                                                        val z_rel = -8.5f
                                                        val t_bounce = 0.65f

                                                        val x_bounce = (state.drsBallTrackingPitchX - 0.5f) * 1.8f
                                                        val y_bounce = 0.03f
                                                        val z_bounce = 10.0f - state.drsBallTrackingPitchY * 11.0f

                                                        // Wicket hit height mapped
                                                        val y_target = if (state.drsBallTrackingWickets == "Hitting") {
                                                            0.32f
                                                        } else if (state.drsBallTrackingWickets == "Missing" && state.drsBallTrackingLength == "Short Ball") {
                                                            1.2f
                                                        } else if (state.drsBallTrackingWickets == "Missing") {
                                                            0.9f
                                                        } else {
                                                            0.58f
                                                        }
                                                        val x_target = if (state.drsBallTrackingWickets == "Hitting") {
                                                            (state.drsBallTrackingPitchX - 0.5f) * 0.45f
                                                        } else if (state.drsBallTrackingWickets.contains("Outside") || state.drsBallTrackingPitching.contains("Outside")) {
                                                            0.7f
                                                        } else {
                                                            0.10f
                                                        }
                                                        val z_target = 10.0f

                                                        // Draw fading neon laser route trace up to current anim progress
                                                        val laserTrailColor = if (state.drsFinalDecisionOut) Color(0xFFEF4444) else Color(0xFF4ADE80)
                                                        val trailSteps = 30
                                                        val trailOffsetList = mutableListOf<Offset>()
                                                        val isPostBounceSeq = mutableListOf<Boolean>()

                                                        for (step in 0..trailSteps) {
                                                            val currT = (step.toFloat() / trailSteps.toFloat()) * ballAnimTime
                                                            val px: Float
                                                            val py: Float
                                                            val pz: Float
                                                            
                                                            if (currT <= t_bounce) {
                                                                val tau = currT / t_bounce
                                                                px = x_rel + (x_bounce - x_rel) * tau
                                                                pz = z_rel + (z_bounce - z_rel) * tau
                                                                py = y_rel * (1.0f - tau) + y_bounce * tau + 2.5f * tau * (1.0f - tau)
                                                                isPostBounceSeq.add(false)
                                                            } else {
                                                                val tau2 = (currT - t_bounce) / (1.2f - t_bounce)
                                                                px = x_bounce + (x_target - x_bounce) * tau2
                                                                pz = z_bounce + (z_target - z_bounce) * tau2
                                                                py = y_bounce * (1.0f - tau2) + y_target * tau2 + 1.1f * tau2 * (1.0f - tau2)
                                                                isPostBounceSeq.add(true)
                                                            }
                                                            
                                                            trailOffsetList.add(project3D(px, py, pz, drsCameraYaw, drsCameraPitch))
                                                        }

                                                        // Plot Trail Segment Connectors
                                                        if (trailOffsetList.size > 1) {
                                                            for (idx in 0 until trailOffsetList.size - 1) {
                                                                val isPost = isPostBounceSeq[idx]
                                                                drawLine(
                                                                    color = if (isPost) laserTrailColor else Color(0xFF38BDF8),
                                                                    start = trailOffsetList[idx],
                                                                    end = trailOffsetList[idx+1],
                                                                    strokeWidth = 2.5f,
                                                                    cap = StrokeCap.Round
                                                                )
                                                            }
                                                        }

                                                        // Draw current live moving ball sphere & physical shadow casting
                                                        val bx: Float
                                                        val by: Float
                                                        val bz: Float
                                                        if (ballAnimTime <= t_bounce) {
                                                            val tau = ballAnimTime / t_bounce
                                                            bx = x_rel + (x_bounce - x_rel) * tau
                                                            bz = z_rel + (z_bounce - z_rel) * tau
                                                            by = y_rel * (1.0f - tau) + y_bounce * tau + 2.5f * tau * (1.0f - tau)
                                                        } else {
                                                            val tau2 = (ballAnimTime - t_bounce) / (1.2f - t_bounce)
                                                            bx = x_bounce + (x_target - x_bounce) * tau2
                                                            bz = z_bounce + (z_target - z_bounce) * tau2
                                                            by = y_bounce * (1.0f - tau2) + y_target * tau2 + 1.1f * tau2 * (1.0f - tau2)
                                                        }

                                                        val ballScreen = project3D(bx, by, bz, drsCameraYaw, drsCameraPitch)
                                                        val shadowScreen = project3D(bx, 0.01f, bz, drsCameraYaw, drsCameraPitch)

                                                        // Casting shadow (creates real 3D depth illusion!)
                                                        drawCircle(
                                                            color = Color.Black.copy(alpha = 0.45f),
                                                            radius = 4.5f,
                                                            center = shadowScreen
                                                        )

                                                        // Ball Cherry sphere
                                                        drawCircle(
                                                            color = Color(0xFFDC2626),
                                                            radius = 6.5f,
                                                            center = ballScreen
                                                        )
                                                        // Soft neon flare around the ball to see easily
                                                        drawCircle(
                                                            color = Color(0xFFFCA5A5).copy(alpha = 0.35f),
                                                            radius = 10f,
                                                            center = ballScreen
                                                        )
                                                    }

                                                    // Overlay camera view label
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .padding(6.dp)
                                                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = "YAW: ${drsCameraYaw.toInt()}° | PITCH: ${drsCameraPitch.toInt()}°",
                                                            color = Color(0xFF38BDF8),
                                                            fontSize = 7.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }

                                            // Right: Immersive 3D Space Controllers
                                            Column(
                                                modifier = Modifier
                                                    .weight(0.9f)
                                                    .fillMaxHeight()
                                                    .background(Color(0xFF0F172A).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                                    .padding(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    "3D CAMERA RIG",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF38BDF8),
                                                    letterSpacing = 0.5.sp
                                                )

                                                Text("PRESET ANGLES:", fontSize = 7.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Button(
                                                        onClick = { drsCameraYaw = 0f; drsCameraPitch = 25f; isOrbiting = false },
                                                        colors = ButtonDefaults.buttonColors(containerColor = if (drsCameraYaw == 0f && drsCameraPitch == 25f && !isOrbiting) Color(0xFF0284C7) else Color(0xFF1E293B)),
                                                        contentPadding = PaddingValues(0.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1f).height(21.dp)
                                                    ) {
                                                        Text("FRONT", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                    Button(
                                                        onClick = { drsCameraYaw = 180f; drsCameraPitch = 25f; isOrbiting = false },
                                                        colors = ButtonDefaults.buttonColors(containerColor = if (drsCameraYaw == 180f && drsCameraPitch == 25f && !isOrbiting) Color(0xFF0284C7) else Color(0xFF1E293B)),
                                                        contentPadding = PaddingValues(0.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1f).height(21.dp)
                                                    ) {
                                                        Text("BACK", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Button(
                                                        onClick = { drsCameraYaw = 90f; drsCameraPitch = 15f; isOrbiting = false },
                                                        colors = ButtonDefaults.buttonColors(containerColor = if (drsCameraYaw == 90f && drsCameraPitch == 15f && !isOrbiting) Color(0xFF0284C7) else Color(0xFF1E293B)),
                                                        contentPadding = PaddingValues(0.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1f).height(21.dp)
                                                    ) {
                                                        Text("SIDE", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                    Button(
                                                        onClick = { drsCameraYaw = 0f; drsCameraPitch = 65f; isOrbiting = false },
                                                        colors = ButtonDefaults.buttonColors(containerColor = if (drsCameraYaw == 0f && drsCameraPitch == 65f && !isOrbiting) Color(0xFF0284C7) else Color(0xFF1E293B)),
                                                        contentPadding = PaddingValues(0.dp),
                                                        shape = RoundedCornerShape(4.dp),
                                                        modifier = Modifier.weight(1f).height(21.dp)
                                                    ) {
                                                        Text("TOP", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }

                                                Button(
                                                    onClick = { isOrbiting = !isOrbiting },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isOrbiting) Color(0xFF10B981) else Color(0xFFFF2E63).copy(alpha = 0.15f)
                                                    ),
                                                    border = BorderStroke(0.6.dp, if (isOrbiting) Color(0xFF34D399) else Color(0xFFFF2E63).copy(alpha = 0.3f)),
                                                    contentPadding = PaddingValues(0.dp),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.fillMaxWidth().height(23.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(Icons.Default.Autorenew, null, modifier = Modifier.size(9.dp), tint = if (isOrbiting) Color.White else Color(0xFFFF8FA2))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(if (isOrbiting) "STOP ORBIT 360" else "AUTO ROTATE ORBIT", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = if (isOrbiting) Color.White else Color(0xFFFF8FA2))
                                                    }
                                                }

                                                Spacer(modifier = Modifier.weight(1f))

                                                // Rotate slider
                                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("Rotate Camera (Yaw)", fontSize = 6.5.sp, color = Color(0xFF94A3B8))
                                                        Text("${drsCameraYaw.toInt()}°", fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                                    }
                                                    Slider(
                                                        value = drsCameraYaw,
                                                        onValueChange = { drsCameraYaw = it; isOrbiting = false },
                                                        valueRange = 0f..360f,
                                                        colors = SliderDefaults.colors(
                                                            thumbColor = Color(0xFF38BDF8),
                                                            activeTrackColor = Color(0xFF0284C7),
                                                            inactiveTrackColor = Color(0xFF334155)
                                                        ),
                                                        modifier = Modifier.height(11.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // ORIGINAL 2D Layout (Pitch landing radar & stats)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Left: Visual Pitch Zone Canvas
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                                modifier = Modifier.weight(1.1f).fillMaxHeight()
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text("PITCH LANDING RADAR", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                                    
                                                    Box(
                                                        modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 2.dp)
                                                    ) {
                                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                                            val w = size.width
                                                            val h = size.height
                                                            
                                                            // Draw turf Grass green base
                                                            drawRect(
                                                                color = Color(0xFF14532D), 
                                                                size = size
                                                            )
                                                            
                                                            // Draw side white bowling crease limits
                                                            drawLine(
                                                                color = Color.White.copy(alpha = 0.4f),
                                                                start = Offset(0.15f * w, 0f),
                                                                end = Offset(0.15f * w, h),
                                                                strokeWidth = 2f
                                                            )
                                                            drawLine(
                                                                color = Color.White.copy(alpha = 0.4f),
                                                                start = Offset(0.85f * w, 0f),
                                                                end = Offset(0.85f * w, h),
                                                                strokeWidth = 2f
                                                            )
                                                            
                                                            // Draw horizontal pitch length zone partitions
                                                            val yFull = 0.22f * h
                                                            val yGood = 0.45f * h
                                                            val yShort = 0.72f * h
                                                            
                                                            drawLine(color = Color.White.copy(alpha = 0.15f), start = Offset(0f, yFull), end = Offset(w, yFull), strokeWidth = 1f)
                                                            drawLine(color = Color.White.copy(alpha = 0.15f), start = Offset(0f, yGood), end = Offset(w, yGood), strokeWidth = 1f)
                                                            drawLine(color = Color.White.copy(alpha = 0.15f), start = Offset(0f, yShort), end = Offset(w, yShort), strokeWidth = 1f)
                                                            
                                                            // Map predicted pitching spot from viewmodel calculation state
                                                            val px = state.drsBallTrackingPitchX * w
                                                            val py = state.drsBallTrackingPitchY * h
                                                            
                                                            // Outer pulsing signal rings
                                                            drawCircle(
                                                                color = Color(0xFFFBBF24).copy(alpha = 0.35f),
                                                                radius = 11f,
                                                                center = Offset(px, py)
                                                            )
                                                            // Red high-visibility core impact dot
                                                            drawCircle(
                                                                color = Color(0xFFEF4444), 
                                                                radius = 5.5f,
                                                                center = Offset(px, py)
                                                            )
                                                        }
                                                        
                                                        // Floating zone guides
                                                        Column(
                                                            modifier = Modifier.fillMaxSize(),
                                                            verticalArrangement = Arrangement.SpaceBetween,
                                                            horizontalAlignment = Alignment.Start
                                                        ) {
                                                            Text(" YORKER ZONE", color = Color.White.copy(alpha = 0.4f), fontSize = 6.sp, fontWeight = FontWeight.SemiBold)
                                                            Text(" FULL PITCH", color = Color.White.copy(alpha = 0.4f), fontSize = 6.sp, fontWeight = FontWeight.SemiBold)
                                                            Text(" GOOD LENGTH", color = Color.White.copy(alpha = 0.4f), fontSize = 6.sp, fontWeight = FontWeight.SemiBold)
                                                            Text(" SHORT BALL", color = Color.White.copy(alpha = 0.4f), fontSize = 6.sp, fontWeight = FontWeight.SemiBold)
                                                        }
                                                    }
                                                    
                                                    Text(
                                                        text = "ZONE: ${state.drsBallTrackingLength.uppercase()}",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFFBBF24)
                                                    )
                                                }
                                            }

                                            // Right: 3 telemetry lines
                                            Column(
                                                modifier = Modifier.weight(1.3f).fillMaxHeight(),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                // Pitching Card
                                                val isPitchingOk = state.drsBallTrackingPitching == "In Line" || state.drsBallTrackingPitching == "Outside Off"
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f)
                                                        .background(Color(0xFF0F172A).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                        .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("PITCHLINE", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                                    Text(
                                                        text = state.drsBallTrackingPitching,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isPitchingOk) Color(0xFF4ADE80) else Color(0xFFF87171)
                                                    )
                                                }

                                                // Impact Card
                                                val isImpactOk = state.drsBallTrackingImpact == "In Line" || state.drsBallTrackingImpact == "Umpire's Call"
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f)
                                                        .background(Color(0xFF0F172A).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                        .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("IMPACT", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                                    Text(
                                                        text = state.drsBallTrackingImpact,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isImpactOk) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                                                    )
                                                }

                                                // Wicket Hit Card
                                                val isWicketsOk = state.drsBallTrackingWickets == "Hitting" || state.drsBallTrackingWickets == "Umpire's Call"
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f)
                                                        .background(Color(0xFF0F172A).copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                                        .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("WICKETS", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                                                    Text(
                                                        text = state.drsBallTrackingWickets,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (isWicketsOk) Color(0xFF4ADE80) else Color(0xFFF87171)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> { // DECISION or NONE
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Text(
                                        text = "VIRTUAL THIRD UMPIRE VERDICT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 1.sp
                                    )

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (state.drsFinalDecisionOut) Color(0xFF7F1D1D) else Color(0xFF064E3B)
                                        ),
                                        border = BorderStroke(2.dp, if (state.drsFinalDecisionOut) Color(0xFFEF4444) else Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (state.drsFinalDecisionOut) "OUT" else "NOT OUT",
                                                fontSize = 32.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                letterSpacing = 4.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Commentary text box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎙️",
                            fontSize = 18.sp
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "DRS Broadcast Analyst",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = state.drsLiveCommentary,
                                fontSize = 11.sp,
                                color = Color.White,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Action Conclude Button
                Button(
                    onClick = { viewModel.concludeDrsReview() },
                    enabled = state.drsIsCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.drsFinalDecisionOut) Color(0xFFDC2626) else Color(0xFF16A34A),
                        disabledContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("conclude_drs_button")
                ) {
                    if (state.drsIsCompleted) {
                        Text("CONCLUDE REVIEW & APPLY VERDICT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(color = Color(0xFF64748B), strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SIMULATING vDRS COMPILATION...", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
