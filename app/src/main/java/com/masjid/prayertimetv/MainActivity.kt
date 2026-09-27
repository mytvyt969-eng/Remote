package com.masjid.prayertimetv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PrayerTime(val name: String, val time: String)

private val Bg = Color(0xFF061A36)
private val Panel = Color(0xFF102A4D)
private val Panel2 = Color(0xFF16365E)
private val Blue = Color(0xFF1688FF)
private val TextMain = Color.White
private val TextDim = Color(0xFFB9C9DE)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PrayerTimeApp() }
    }
}

@Composable
fun PrayerTimeApp() {
    val defaults = remember {
        listOf(
            PrayerTime("Fajr", "05:12 AM"),
            PrayerTime("Dhuhr", "12:28 PM"),
            PrayerTime("Asr", "04:03 PM"),
            PrayerTime("Maghrib", "06:09 PM"),
            PrayerTime("Isha", "07:45 PM")
        )
    }
    var prayers by remember { mutableStateOf(defaults) }
    var selected by remember { mutableIntStateOf(0) }
    var saved by remember { mutableStateOf(false) }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(Bg).padding(horizontal = 46.dp, vertical = 28.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Text("Set Prayer Times", color = TextMain, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Change the time for each of the 5 daily prayers",
                    color = TextDim, fontSize = 20.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(prayers) { index, prayer ->
                        PrayerRow(
                            prayer = prayer,
                            selected = selected == index,
                            onFocus = { selected = index },
                            onChange = { delta ->
                                prayers = prayers.toMutableList().also {
                                    it[index] = it[index].copy(time = changeTime(it[index].time, delta))
                                }
                                saved = false
                            }
                        )
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TvButton("Reset to Default", Modifier.weight(1f)) {
                        prayers = defaults
                        saved = false
                    }
                    TvButton("Save Changes", Modifier.weight(1.25f), primary = true) {
                        saved = true
                    }
                }

                Text(
                    if (saved) "✓ Changes saved" else "↑ ↓ Change time   •   ← → Select   •   OK Save",
                    color = if (saved) Color(0xFF7DFFB2) else TextDim,
                    fontSize = 17.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun PrayerRow(
    prayer: PrayerTime,
    selected: Boolean,
    onFocus: () -> Unit,
    onChange: (Int) -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected || focused) Panel2 else Panel)
            .border(3.dp, if (focused) Blue else Color(0x335C86B5), RoundedCornerShape(18.dp))
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocus()
            }
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionUp -> { onChange(+60); true }
                    Key.DirectionDown -> { onChange(-60); true }
                    Key.DirectionLeft -> { onChange(-1); true }
                    Key.DirectionRight -> { onChange(+1); true }
                    else -> false
                }
            }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prayer.name, color = TextMain, fontSize = 27.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text("−", color = TextDim, fontSize = 30.sp, modifier = Modifier.padding(horizontal = 20.dp))
        Text(prayer.time, color = TextMain, fontSize = 31.sp, fontWeight = FontWeight.Bold)
        Text("+", color = TextDim, fontSize = 30.sp, modifier = Modifier.padding(horizontal = 20.dp))
    }
}

@Composable
private fun TvButton(label: String, modifier: Modifier = Modifier, primary: Boolean = false, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp).onFocusChanged { focused = it.isFocused },
        shape = RoundedCornerShape(16.dp),
        color = if (primary) Blue else Panel,
        border = if (focused) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun changeTime(value: String, minutesDelta: Int): String {
    val clean = value.removeSuffix(" AM").removeSuffix(" PM")
    val parts = clean.split(":")
    var h = parts[0].toInt()
    val m = parts[1].toInt()
    val pm = value.endsWith("PM")
    var total = h % 12 * 60 + m + minutesDelta
    total = ((total % 720) + 720) % 720
    h = total / 60
    val newH = if (h == 0) 12 else h
    return String.format("%02d:%02d %s", newH, total % 60, if (pm) "PM" else "AM")
}
