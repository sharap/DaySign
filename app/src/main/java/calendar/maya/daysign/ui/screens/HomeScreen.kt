package calendar.maya.daysign.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.R
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.AccordionItem
import calendar.maya.daysign.ui.components.ImageSign
import calendar.maya.daysign.ui.components.InfluenceList
import calendar.maya.daysign.ui.components.KinDescription
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lang = remember { Locale.getDefault().language }
    
    val mayaDate by viewModel.currentMayaDate.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val allPeople by viewModel.allPeople.collectAsState()
    val allGroups by viewModel.groups.collectAsState()
    val defaultGroupId by viewModel.defaultGroupId.collectAsState()
    val effectiveMembers by viewModel.effectiveDefaultGroupMembers.collectAsState()
    
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    val daysignNamesTo = stringArrayResource(id = R.array.daysign_names_to)
    
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
    
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
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
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
                }
            )
        },
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallFloatingActionButton(
                    onClick = { viewModel.backDay() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }

                ExtendedFloatingActionButton(
                    onClick = { viewModel.setCurrentDate(LocalDate.now()) },
                    text = { Text(stringResource(R.string.today)) },
                    icon = { },
                    containerColor = if (currentDate == LocalDate.now()) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.secondaryContainer
                )

                SmallFloatingActionButton(
                    onClick = { viewModel.nextDay() },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.forward))
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        var expandedSection by remember { mutableStateOf<String?>("kin") }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Кин ${mayaDate.kin}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.navigateToCharacter(mayaDate.daysign) }
                    ) {
                        ImageSign(sign = mayaDate.daysign, size = 120.dp)
                        Text(daysignNames.getOrElse(mayaDate.daysign) { "" }, fontWeight = FontWeight.Bold)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.navigateToCharacter(mayaDate.trecena) }
                    ) {
                        ImageSign(sign = mayaDate.trecena, size = 120.dp)
                        Text(daysignNamesTo.getOrElse(mayaDate.trecena) { "" }, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = currentDate.format(dateFormatter),
                    modifier = Modifier.clickable { 
                        showDatePicker = true
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                AccordionItem(
                    title = stringResource(R.string.phantoms),
                    isExpanded = expandedSection == "phantoms",
                    onToggle = { expandedSection = if (expandedSection == "phantoms") null else "phantoms" },
                    visible = mayaDate.fantoms.isNotEmpty()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mayaDate.fantoms.forEach { phantom ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.navigateToCharacter(phantom) }
                            ) {
                                ImageSign(sign = phantom, size = 48.dp)
                                Text(daysignNames.getOrElse(phantom) { "" }, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            item {
                AccordionItem(
                    title = stringResource(R.string.kin_description),
                    isExpanded = expandedSection == "kin",
                    onToggle = { expandedSection = if (expandedSection == "kin") null else "kin" }
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        KinDescription(kin = mayaDate.kin, lang = lang)
                    }
                }
            }

            val equalPeople = allPeople.filter { person ->
                val personMaya = if (person.sunrise == "before") {
                    MayaCalendar.maya(person.birthDate.minusDays(1))
                } else {
                    MayaCalendar.maya(person.birthDate)
                }
                personMaya.kin == mayaDate.kin
            }

            item {
                AccordionItem(
                    title = stringResource(R.string.equal_people),
                    isExpanded = expandedSection == "equal",
                    onToggle = { expandedSection = if (expandedSection == "equal") null else "equal" },
                    visible = equalPeople.isNotEmpty()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        equalPeople.forEach { person ->
                            ListItem(
                                modifier = Modifier.clickable { viewModel.navigateToPerson(person.id) },
                                headlineContent = { Text(person.name) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            val defaultGroup = allGroups.find { it.id == defaultGroupId }
            item {
                AccordionItem(
                    title = if (defaultGroup != null) {
                        "${stringResource(R.string.group_label)} ${defaultGroup.name}"
                    } else if (!effectiveMembers.isNullOrEmpty()) {
                        stringResource(R.string.favorites)
                    } else {
                        stringResource(R.string.influence_people)
                    },
                    isExpanded = expandedSection == "influence",
                    onToggle = { expandedSection = if (expandedSection == "influence") null else "influence" }
                ) {
                    val filteredPeople = if (effectiveMembers != null) {
                        allPeople.filter { it.id in effectiveMembers!! }
                    } else {
                        allPeople
                    }

                    InfluenceList(
                        targetKinDaysign = mayaDate.daysign,
                        targetKinTrecena = mayaDate.trecena,
                        people = filteredPeople,
                        onPersonClick = { viewModel.navigateToPerson(it.id) },
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
