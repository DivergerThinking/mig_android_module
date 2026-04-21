package com.diverger.mig_android_sdk.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// ═══════════════════════════════════════════════════════════════
// Colors
// ═══════════════════════════════════════════════════════════════

val CardGradientStart = Color(0xFFEC407A)
val CardGradientEnd = Color(0xFF9C27B0)

private val cardGradient = Brush.linearGradient(
    colors = listOf(CardGradientStart, CardGradientEnd),
    start = Offset.Zero,
    end = Offset.Infinite
)

private val cardShape = RoundedCornerShape(25.dp)

// ═══════════════════════════════════════════════════════════════
// Simple gradient card (no flip, no effects)
// ═══════════════════════════════════════════════════════════════

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(cardGradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════
// Holographic Flippable Card
// ═══════════════════════════════════════════════════════════════

@Composable
fun HolographicFlippableCard(
    targetAngle: Float,
    onAngleChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    front: @Composable ColumnScope.() -> Unit,
    back: @Composable ColumnScope.() -> Unit
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    // Single Animatable drives all rotation: drag snaps directly into it,
    // flip animation starts from wherever the finger left off — no snap/bounce.
    val rotation = remember { Animatable(targetAngle) }
    val scope = rememberCoroutineScope()
    val currentTargetAngle by rememberUpdatedState(targetAngle)
    val currentOnAngleChange by rememberUpdatedState(onAngleChange)

    val flipSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    // React to external targetAngle changes (e.g. button press)
    LaunchedEffect(targetAngle) {
        rotation.animateTo(targetAngle, flipSpec)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 12 * density
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val base = currentTargetAngle
                        val drag = rotation.value - base
                        scope.launch {
                            if (abs(drag) > 25f) {
                                val newTarget = if (drag > 0f) base + 180f else base - 180f
                                currentOnAngleChange(newTarget)
                                rotation.animateTo(newTarget, flipSpec)
                            } else {
                                rotation.animateTo(base, flipSpec)
                            }
                        }
                        dragOffsetX = 0f
                    },
                    onDragCancel = {
                        scope.launch { rotation.animateTo(currentTargetAngle, flipSpec) }
                        dragOffsetX = 0f
                    },
                    onHorizontalDrag = { _, amount ->
                        // On the back face the content is mirrored (rotationY = 180f),
                        // so we invert the drag direction to match the finger movement visually.
                        val normalized = ((currentTargetAngle % 360f) + 360f) % 360f
                        val faceFactor = if (normalized in 90f..270f) -1f else 1f
                        val newValue = (rotation.value + (amount * faceFactor) / 5f)
                            .coerceIn(currentTargetAngle - 45f, currentTargetAngle + 45f)
                        scope.launch { rotation.snapTo(newValue) }
                        dragOffsetX += amount
                    }
                )
            }
    ) {
        val normalized = ((rotation.value % 360f) + 360f) % 360f
        val showFront = normalized < 90f || normalized > 270f

        if (showFront) {
            FrontFace(dragOffsetX = dragOffsetX, content = front)
        } else {
            BackFace(content = back)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Front face with holographic effects
// ═══════════════════════════════════════════════════════════════

@Composable
private fun FrontFace(
    dragOffsetX: Float,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape)
                .background(cardGradient)
                .drawWithContent {
                    drawContent()

                    // Radial shine following drag
                    val cx = size.width * (0.5f + dragOffsetX / 800f)
                    val cy = size.height * 0.4f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = 300.dp.toPx()
                        ),
                        radius = 300.dp.toPx(),
                        center = Offset(cx, cy)
                    )

                    // Subtle border glow
                    val borderShift = dragOffsetX / 600f
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.25f)
                            ),
                            start = Offset(size.width * borderShift, 0f),
                            end = Offset(size.width * (1f + borderShift), size.height)
                        ),
                        size = size,
                        cornerRadius = CornerRadius(25.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )

        // Watermark — only visible during drag
        if (dragOffsetX != 0f) {
            val wmAlpha = (abs(dragOffsetX) / 400f).coerceIn(0f, 0.25f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 28.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "MADRID IN GAME",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = Color.White.copy(alpha = wmAlpha)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Back face (mirrored)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BackFace(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }
            .clip(cardShape)
            .background(cardGradient)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}
