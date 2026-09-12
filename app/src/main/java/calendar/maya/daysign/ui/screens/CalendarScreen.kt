package calendar.maya.daysign.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.R
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.model.MayaDate
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.ImageSign
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: MainViewModel, resetTrigger: Int = 0) {
    val currentDate by viewModel.currentDate.collectAsState()
    val peopleByKin by viewModel.peopleByKin.collectAsState()
    val locale = Locale.getDefault()
    
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    val daysignNamesTo = stringArrayResource(id = R.array.daysign_names_to)
    
    // Get localized week days
    val weekDays = remember(locale) {
        listOf(
            java.time.DayOfWeek.SUNDAY,
            java.time.DayOfWeek.MONDAY,
            java.time.DayOfWeek.TUESDAY,
            java.time.DayOfWeek.WEDNESDAY,
            java.time.DayOfWeek.THURSDAY,
            java.time.DayOfWeek.FRIDAY,
            java.time.DayOfWeek.SATURDAY
        ).map { it.getDisplayName(java.time.format.TextStyle.SHORT, locale).uppercase() }
    }

    // Created once per screen instead of once per visible list item
    val dayFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    // Use a fixed anchor date to keep indices stable
    val anchorDate = remember { LocalDate.of(2000, 1, 1) }
    val baseIndex = 100000 // Very large number to allow long scroll back
    
    val listState = rememberLazyGridState()

    // Calculate target index for current date
    val targetIndex = (baseIndex + ChronoUnit.DAYS.between(anchorDate, currentDate)).toInt()

    // Initial scroll and reset scroll
    LaunchedEffect(resetTrigger) {
        listState.scrollToItem(targetIndex - 2) // Show a bit of context above
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.calendar_title)) }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(
                count = baseIndex * 2,
                key = { index -> index }
            ) { index ->
                val date = remember(index) { anchorDate.plusDays((index - baseIndex).toLong()) }
                // Only kin/daysign/trecena are shown here, so the row takes the
                // shared cyclic values instead of allocating a full MayaDate.
                val maya = remember(date) { MayaCalendar.cycleInfo(date) }
                val isSelected = date == currentDate

                // Find people with this kin - OPTIMIZED: use pre-calculated map
                val birthdays = peopleByKin[maya.kin] ?: emptyList()

                Column {
                    ListItem(
                        modifier = Modifier.clickable { 
                            viewModel.setCurrentDate(date)
                            viewModel.navigateToPage(1) // Switch to Today tab
                        },
                        headlineContent = {
                            Text(
                                text = "${daysignNames.getOrElse(maya.daysign) { "" }} ${daysignNamesTo.getOrElse(maya.trecena) { "" }}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            Column {
                                Text("${date.format(dayFormatter)} ${weekDays[date.dayOfWeek.value % 7]}")
                                if (birthdays.isNotEmpty()) {
                                    Text(
                                        text = birthdays.joinToString(", ") { it.name },
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontSize = 12.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        },
                        leadingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isSame = maya.daysign == maya.trecena
                                val size = if (isSame) 60.dp else 40.dp
                                
                                ImageSign(sign = maya.daysign, size = size)
                                if (!isSame) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    ImageSign(sign = maya.trecena, size = 40.dp)
                                }
                            }
                        },
                        trailingContent = {
                            Text("${stringResource(R.string.kin_label)} ${maya.kin}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        },
                        colors = if (isSelected) 
                            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        else 
                            ListItemDefaults.colors()
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
