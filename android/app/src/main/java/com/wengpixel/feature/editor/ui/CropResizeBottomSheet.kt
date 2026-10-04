package com.wengpixel.feature.editor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wengpixel.core.model.CropRatio

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CropResizeBottomSheet(
    currentWidth: Int,
    currentHeight: Int,
    onDismiss: () -> Unit,
    onApplyCrop: (CropRatio) -> Unit,
    onApplyResize: (Int, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedRatio by remember { mutableStateOf(CropRatio.FREE) }
    var widthInput by remember { mutableStateOf(currentWidth.toString()) }
    var heightInput by remember { mutableStateOf(currentHeight.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Potong & Ubah Ukuran Gambar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ukuran saat ini: $currentWidth × $currentHeight px",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pilih Rasio Aspek (Crop):",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CropRatio.entries.forEach { ratio ->
                    FilterChip(
                        selected = selectedRatio == ratio,
                        onClick = { selectedRatio = ratio },
                        label = { Text(ratio.title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Ubah Resolusi Piksel (Resize Manual):",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = widthInput,
                    onValueChange = { widthInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Lebar (px)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("×", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it.filter { char -> char.isDigit() } },
                    label = { Text("Tinggi (px)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val w = widthInput.toIntOrNull() ?: currentWidth
                        val h = heightInput.toIntOrNull() ?: currentHeight
                        if (selectedRatio != CropRatio.FREE) {
                            onApplyCrop(selectedRatio)
                        } else if (w != currentWidth || h != currentHeight) {
                            onApplyResize(w, h)
                        }
                        onDismiss()
                    }
                ) {
                    Text("Terapkan")
                }
            }
        }
    }
}
