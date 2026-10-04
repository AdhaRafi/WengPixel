package com.wengpixel.core.designsystem.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlin.math.roundToInt

@Composable
fun CheckerboardBackground(
    modifier: Modifier = Modifier,
    squareSizePx: Float = 24f,
    color1: Color = Color(0xFF1E293B),
    color2: Color = Color(0xFF334155)
) {
    Canvas(modifier = modifier) {
        val numCols = (size.width / squareSizePx).toInt() + 1
        val numRows = (size.height / squareSizePx).toInt() + 1
        for (i in 0 until numCols) {
            for (j in 0 until numRows) {
                val color = if ((i + j) % 2 == 0) color1 else color2
                drawRect(
                    color = color,
                    topLeft = Offset(i * squareSizePx, j * squareSizePx),
                    size = Size(squareSizePx, squareSizePx)
                )
            }
        }
    }
}

@Composable
fun BeforeAfterSlider(
    originalBitmap: Bitmap?,
    editedBitmap: Bitmap?,
    modifier: Modifier = Modifier,
    backgroundColor: Int = 0, // 0 = transparan dengan latar papan catur
    isShowingOriginalHold: Boolean = false
) {
    if (originalBitmap == null && editedBitmap == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Tidak ada gambar", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    // Jika belum ada hasil edit, tampilkan original saja
    if (editedBitmap == null || isShowingOriginalHold) {
        val activeBitmap = originalBitmap ?: editedBitmap!!
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            if (backgroundColor == 0) {
                CheckerboardBackground(modifier = Modifier.matchParentSize())
            } else {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(backgroundColor))
                )
            }
            AsyncImage(
                model = activeBitmap,
                contentDescription = "Gambar Asli",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "SEBELUM (ASLI)",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        return
    }

    // Perbandingan Sebelum dan Sesudah dengan Slider Geser
    var sliderFraction by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val splitX = widthPx * sliderFraction

        // Latar belakang transparan checkerboard atau warna terpilih
        if (backgroundColor == 0) {
            CheckerboardBackground(modifier = Modifier.matchParentSize())
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(backgroundColor))
            )
        }

        // Gambar Asli (Sebelum) di sisi kiri slider
        Canvas(modifier = Modifier.fillMaxSize()) {
            val imgBitmap = (originalBitmap ?: editedBitmap).asImageBitmap()
            val srcW = imgBitmap.width.toFloat()
            val srcH = imgBitmap.height.toFloat()
            val scale = minOf(size.width / srcW, size.height / srcH)
            val destW = srcW * scale
            val destH = srcH * scale
            val destX = (size.width - destW) / 2f
            val destY = (size.height - destH) / 2f

            clipRect(left = 0f, top = 0f, right = splitX, bottom = size.height) {
                drawImage(
                    image = imgBitmap,
                    dstOffset = IntOffset(destX.roundToInt(), destY.roundToInt()),
                    dstSize = IntSize(destW.roundToInt(), destH.roundToInt())
                )
            }
        }

        // Gambar Hasil Edit (Sesudah) di sisi kanan slider
        Canvas(modifier = Modifier.fillMaxSize()) {
            val imgBitmap = editedBitmap.asImageBitmap()
            val srcW = imgBitmap.width.toFloat()
            val srcH = imgBitmap.height.toFloat()
            val scale = minOf(size.width / srcW, size.height / srcH)
            val destW = srcW * scale
            val destH = srcH * scale
            val destX = (size.width - destW) / 2f
            val destY = (size.height - destH) / 2f

            clipRect(left = splitX, top = 0f, right = size.width, bottom = size.height) {
                drawImage(
                    image = imgBitmap,
                    dstOffset = IntOffset(destX.roundToInt(), destY.roundToInt()),
                    dstSize = IntSize(destW.roundToInt(), destH.roundToInt())
                )
            }
        }

        // Garis Pembatas Slider
        Box(
            modifier = Modifier
                .offset { IntOffset(splitX.roundToInt() - 1.dp.roundToPx(), 0) }
                .width(2.dp)
                .fillMaxSize()
                .background(Color.White)
        )

        // Tombol Drag Handle Slider
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (splitX - 18.dp.roundToPx()).roundToInt(),
                        (heightPx / 2f - 18.dp.roundToPx()).roundToInt()
                    )
                }
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        sliderFraction = (sliderFraction + (dragAmount.x / widthPx)).coerceIn(0.05f, 0.95f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = "Geser perbandingan",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Label Sebelum & Sesudah
        Surface(
            color = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(
                text = "SEBELUM",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Text(
                text = "SESUDAH",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}
