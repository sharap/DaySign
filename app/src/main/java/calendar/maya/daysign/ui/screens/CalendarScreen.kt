package calendar.maya.daysign.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
    val allPeople by viewModel.allPeople.collectAsState()
    
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    val daysignNamesTo = stringArrayResource(id = R.array.daysign_names_to)
    val weekDays = listOf("ВС", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ")

    // Use a fixed anchor date to keep indices stable
    val anchorDate = remember { LocalDate.of(2000, 1, 1) }
    val baseIndex = 100000 // Very large number to allow long scroll back
    
    val listState = rememberLazyListState()

    // Calculate target index for current date
    val targetIndex = (baseIndex + ChronoUnit.DAYS.between(anchorDate, currentDate)).toInt()

    // Initial scroll and reset scroll
    LaunchedEffect(resetTrigger) {
        listState.scrollToItem(targetIndex - 2) // Show a bit of context above
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Календарь") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            itemsIndexed(
                items = List(baseIndex * 2) { it },
                key = { _, index -> index }
            ) { index, _ ->
                val date = anchorDate.plusDays((index - baseIndex).toLong())
                val maya = MayaCalendar.maya(date)
                val isSelected = date == currentDate
                
                // Find people with this kin
                val birthdays = allPeople.filter { person ->
                    val personMaya = if (person.sunrise == "before") {
                        MayaCalendar.maya(person.birthDate.minusDays(1))
                    } else {
                        MayaCalendar.maya(person.birthDate)
                    }
                    personMaya.kin == maya.kin
                }

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
                                Text("${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))} ${weekDays[date.dayOfWeek.value % 7]}")
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
                            Text("Кин ${maya.kin}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
