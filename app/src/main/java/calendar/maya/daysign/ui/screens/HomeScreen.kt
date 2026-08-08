package calendar.maya.daysign.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.R
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.ImageSign
import calendar.maya.daysign.ui.components.KinDescription
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToPeople: () -> Unit) {
    val mayaDate by viewModel.currentMayaDate.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    val daysignNamesTo = stringArrayResource(id = R.array.daysign_names_to)
    
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("ru"))
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        viewModel.setCurrentDate(date)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "${mayaDate.longCount.baktun}.${mayaDate.longCount.katun}.${mayaDate.longCount.tun}.${mayaDate.longCount.uinal}.${mayaDate.longCount.day}",
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToPeople) {
                        Icon(Icons.Default.Person, contentDescription = "People")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.backDay() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Button(onClick = { viewModel.setCurrentDate(LocalDate.now()) }) {
                        Text("Сегодня")
                    }
                    IconButton(onClick = { viewModel.nextDay() }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Кин ${mayaDate.kin}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ImageSign(sign = mayaDate.daysign, size = 120.dp)
                    Text(daysignNames.getOrElse(mayaDate.daysign) { "" })
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ImageSign(sign = mayaDate.trecena, size = 120.dp)
                    Text(daysignNamesTo.getOrElse(mayaDate.trecena) { "" })
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = currentDate.format(dateFormatter),
                modifier = Modifier.clickable { 
                    showDatePicker = true
                },
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Accordion-like sections (simplified)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Описание дня", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    KinDescription(kin = mayaDate.kin, lang = "ru")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (mayaDate.fantoms.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Фантомы", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            mayaDate.fantoms.forEach { phantom ->
                                ImageSign(sign = phantom, size = 40.dp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Связи", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val connections = MayaCalendar.getConnections(mayaDate.daysign)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        connections.forEach { conn ->
                            ImageSign(sign = conn, size = 40.dp)
                        }
                    }
                }
            }
        }
    }
}
