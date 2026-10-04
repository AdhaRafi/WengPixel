package com.wengpixel.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wengpixel.core.designsystem.PresetEditorColors

@Composable
fun ColorPickerRow(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(PresetEditorColors) { (colorInt, name) ->
            val isSelected = selectedColor == colorInt

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        } else {
                            Modifier.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        }
                    )
                    .clickable { onColorSelected(colorInt) }
                    .padding(3.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (colorInt == 0) {
                    // Transparan: tampilkan pola checkerboard
                    CheckerboardBackground(
                        modifier = Modifier.fillMaxSize(),
                        squareSizePx = 10f,
                        color1 = Color(0xFF64748B),
                        color2 = Color(0xFF94A3B8)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(colorInt))
                    )
                }

                if (isSelected) {
                    val iconTint = if (colorInt == 0xFFFFFFFF.toInt() || colorInt == 0xFFFEF08A.toInt()) {
                        Color.Black
                    } else {
                        Color.White
                    }
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Terpilih",
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
