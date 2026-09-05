package com.example.ui.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.HarmonicAudioEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IntroLandscapeScreen(
    onIntroFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 3-Phase Cinematic Presentation matching the exact video sequence:
    // Phase 1: Atagul Games 3D Production Sequence (Crimson/Red background + 3D faceted typography)
    // Phase 2: Unity Engine Splash (Black background + 3D Unity logo)
    // Phase 3: Official ECHO Logo Reveal (Featuring the user's uploaded echo_logo.jpg)
    var phase by remember { mutableIntStateOf(1) }

    val atagulAlpha = remember { Animatable(0f) }
    val atagulScale = remember { Animatable(0.82f) }

    val unityAlpha = remember { Animatable(0f) }
    val unityScale = remember { Animatable(0.9f) }

    val echoAlpha = remember { Animatable(0f) }
    val echoScale = remember { Animatable(0.75f) }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val neonGlow by pulseTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonGlow"
    )

    LaunchedEffect(Unit) {
        HarmonicAudioEngine.playIntroJingle()

        // Phase 1: Atagul Games Video Animation
        launch { atagulAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch { atagulScale.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }
        delay(1400)
        atagulAlpha.animateTo(0f, tween(300))

        // Phase 2: Unity Engine Video Splash
        phase = 2
        launch { unityAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { unityScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        delay(1200)
        unityAlpha.animateTo(0f, tween(300))

        // Phase 3: Official ECHO Logo Reveal
        phase = 3
        launch { echoAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        launch { echoScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        delay(1500)

        // Smooth transition to main game
        onIntroFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when (phase) {
                    1 -> Color(0xFFB70000) // Atagul Games crimson red from video
                    2 -> Color(0xFF000000) // Unity pitch black from video
                    else -> Color(0xFF050711) // Echo dark cosmic
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onIntroFinished()
            }
            .testTag("intro_landscape_screen"),
        contentAlignment = Alignment.Center
    ) {
        if (phase == 1) {
            // === SCENE 1: ATAGUL GAMES 3D INTRO VIDEO SEQUENCE ===
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Center horizontal darker cinematic band from video
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color(0xFF6B0000))
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .scale(atagulScale.value)
                        .alpha(atagulAlpha.value)
                        .padding(horizontal = 24.dp)
                ) {
                    // 3D Stylized Studio Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0))
                                )
                            )
                    ) {
                        Text(
                            text = "A",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFB70000),
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3D White Block Letters from Video
                    Text(
                        text = "ATAGUL",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 8.sp,
                        color = Color.White,
                        modifier = Modifier.shadow(8.dp)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "G A M E S",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 10.sp,
                        color = Color(0xFFFFF1F2)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "P R E S E N T S",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 6.sp,
                        color = Color(0xFFFFD1D5)
                    )
                }
            }
        } else if (phase == 2) {
            // === SCENE 2: UNITY LOGO SPLASH ===
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(unityScale.value)
                    .alpha(unityAlpha.value)
            ) {
                // Iconic Unity 3D Cube Mark
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 3-blade interlocking geometric representation of the Unity cube
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp, 16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row {
                            Box(
                                modifier = Modifier
                                    .size(24.dp, 28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(24.dp, 28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "unity",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MADE WITH ENGINE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 4.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        } else {
            // === SCENE 3: OFFICIAL ECHO GAME LOGO REVEAL ===
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Glowing Neon Aura
                Box(
                    modifier = Modifier
                        .size(340.dp)
                        .scale(neonGlow)
                        .blur(65.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x7700E5FF),
                                    Color(0x337C4DFF),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .scale(echoScale.value)
                        .alpha(echoAlpha.value)
                ) {
                    // The exact user-provided official game logo
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(240.dp)
                            .shadow(24.dp, RoundedCornerShape(36.dp))
                            .clip(RoundedCornerShape(36.dp))
                            .background(Color(0xFF0F172A))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.echo_logo),
                            contentDescription = "ECHO Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "E C H O",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 10.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "TEK ÇİZGİ BULMACA",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        color = Color(0xFF00E5FF)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Başlamak için dokun",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
