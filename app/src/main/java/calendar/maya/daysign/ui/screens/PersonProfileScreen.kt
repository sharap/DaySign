package calendar.maya.daysign.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.R
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.AccordionItem
import calendar.maya.daysign.ui.components.ConnectionCircle
import calendar.maya.daysign.ui.components.ImageSign
import calendar.maya.daysign.ui.components.InfluenceList
import calendar.maya.daysign.ui.components.MarkdownAsset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonProfileScreen(
    personId: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToPerson: (Int) -> Unit,
    onNavigateToGroup: (Int) -> Unit
) {
    val allPeople by viewModel.allPeople.collectAsState()
    val allGroups by viewModel.groups.collectAsState()
    val defaultGroupId by viewModel.defaultGroupId.collectAsState()
    val favoritesIds by viewModel.favoritesIds.collectAsState()
    val effectiveMembers by viewModel.effectiveDefaultGroupMembers.collectAsState()
    val lang = remember { Locale.getDefault().language }
    
    val person = allPeople.find { it.id == personId }
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    val daysignNamesTo = stringArrayResource(id = R.array.daysign_names_to)
    val currentDate by viewModel.currentDate.collectAsState()

    if (person == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.person_not_found))
        }
        return
    }

    val mayaDate = if (person.sunrise == "before") {
        MayaCalendar.maya(person.birthDate.minusDays(1))
    } else {
        MayaCalendar.maya(person.birthDate)
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) }
    val personGroups = allGroups.filter { it.memberIds.contains(person.id) }
    
    val birthDate = if (person.sunrise == "before") person.birthDate.minusDays(1) else person.birthDate
    val diffDays = ChronoUnit.DAYS.between(birthDate, currentDate)
    val isBorn = birthDate.isBefore(currentDate) || birthDate.isEqual(currentDate)
    val ageKin = if (isBorn) (Math.ceil((260.0 + diffDays) / 260.0)).toInt() else 1
    val ageMaya = if (isBorn) {
        val tone = if (ageKin % 13 == 0) 13 else ageKin % 13
        val ds = if (ageKin % 20 == 0) 20 else ageKin % 20
        val tr = if ((ds - tone + 1) < 1) (ds - tone + 21) else (ds - tone + 1)
        ds to tr
    } else 1 to 1

    val toNextBirth = 260 - (diffDays % 260)
    val nextMayaBirth = currentDate.plusDays(toNextBirth)

    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${person.name} (${mayaDate.kin})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    val isFavorite = favoritesIds.contains(personId)
                    IconButton(onClick = { viewModel.toggleFavorite(personId) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                    }
                    IconButton(onClick = { 
                        viewModel.deletePerson(person)
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val scope = this
            val isWide = scope.maxWidth > 800.dp
            var expandedSection by remember { mutableStateOf<String?>(null) }

            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Column: Signs, Phantoms, Info, Map, Groups, Connections
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.navigateToCharacter(mayaDate.daysign) }) {
                                ImageSign(sign = mayaDate.daysign, size = 120.dp)
                                Text(text = daysignNames.getOrElse(mayaDate.daysign) { "" }, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.navigateToCharacter(mayaDate.trecena) }) {
                                ImageSign(sign = mayaDate.trecena, size = 120.dp)
                                Text(text = daysignNamesTo.getOrElse(mayaDate.trecena) { "" }, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AccordionItem(title = stringResource(R.string.phantoms), isExpanded = expandedSection == "phantoms", onToggle = { expandedSection = if (expandedSection == "phantoms") null else "phantoms" }, visible = mayaDate.fantoms.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                mayaDate.fantoms.forEach { phantom ->
                                    ImageSign(sign = phantom, size = 48.dp, modifier = Modifier.clickable { viewModel.navigateToCharacter(phantom) })
                                }
                            }
                        }

                        AccordionItem(title = stringResource(R.string.info), isExpanded = expandedSection == "info", onToggle = { expandedSection = if (expandedSection == "info") null else "info" }) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                InfoRow(
                                    stringResource(R.string.birth_gregorian), 
                                    "${person.birthDate.format(dateFormatter)} (${if (person.sunrise == "before") stringResource(R.string.before_sunrise) else stringResource(R.string.after_sunrise)})",
                                    onClick = { viewModel.navigateToPage(1, person.birthDate) }
                                )
                                InfoRow(stringResource(R.string.kin_label), mayaDate.kin.toString())
                                InfoRow(stringResource(R.string.count_days), diffDays.toString())
                                if (isBorn) {
                                    InfoRow(stringResource(R.string.age_sign), "${daysignNames.getOrElse(ageMaya.first) { "" }} ${daysignNamesTo.getOrElse(ageMaya.second) { "" }}")
                                    InfoRow(stringResource(R.string.age_kin), ageKin.toString())
                                    InfoRow(stringResource(R.string.age_gregorian), (diffDays / 365.25).toInt().toString())
                                    InfoRow(
                                        stringResource(R.string.next_maya_day), 
                                        "${nextMayaBirth.format(dateFormatter)}, ${stringResource(R.string.in_days, toNextBirth)}",
                                        onClick = { viewModel.navigateToPage(1, nextMayaBirth) }
                                    )
                                }
                            }
                        }

                        AccordionItem(title = stringResource(R.string.map_connections), isExpanded = expandedSection == "map", onToggle = { expandedSection = if (expandedSection == "map") null else "map" }) {
                            ConnectionCircle(daysign = mayaDate.daysign, trecena = mayaDate.trecena)
                        }

                        AccordionItem(title = stringResource(R.string.groups), isExpanded = expandedSection == "groups", onToggle = { expandedSection = if (expandedSection == "groups") null else "groups" }) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                personGroups.forEach { group ->
                                    SuggestionChip(onClick = { onNavigateToGroup(group.id) }, label = { Text(group.name) }, modifier = Modifier.padding(bottom = 4.dp))
                                }
                                val availableGroups = allGroups.filter { personId !in it.memberIds }
                                if (availableGroups.isNotEmpty()) {
                                    var showSelectGroupDialog by remember { mutableStateOf(false) }
                                    AssistChip(onClick = { showSelectGroupDialog = true }, label = { Text(stringResource(R.string.add_to_group)) }, leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(AssistChipDefaults.IconSize)) })
                                    if (showSelectGroupDialog) {
                                        SelectGroupDialog(groups = availableGroups, onDismiss = { showSelectGroupDialog = false }, onConfirm = { viewModel.addPersonToGroup(personId, it); showSelectGroupDialog = false })
                                    }
                                }
                            }
                        }

                        val connTitle = if (defaultGroupId != null) stringResource(R.string.connections_people) + " (${allGroups.find { it.id == defaultGroupId }?.name})" else if (favoritesIds.isNotEmpty()) stringResource(R.string.favorites) else stringResource(R.string.connections_people)
                        AccordionItem(title = connTitle, isExpanded = expandedSection == "connections", onToggle = { expandedSection = if (expandedSection == "connections") null else "connections" }) {
                            val filterIds = allGroups.find { it.id == defaultGroupId }?.memberIds
                            // Kept in a keyed remember so the list is not rebuilt on every
                            // recomposition and InfluenceList can actually skip.
                            val filteredPeople = remember(allPeople, filterIds, favoritesIds, person.id) {
                                val ids = filterIds?.toHashSet()
                                if (ids != null) allPeople.filter { it.id in ids && it.id != person.id }
                                else if (favoritesIds.isNotEmpty()) allPeople.filter { it.id in favoritesIds && it.id != person.id }
                                else allPeople.filter { it.id != person.id }
                            }
                            InfluenceList(targetKinDaysign = mayaDate.daysign, targetKinTrecena = mayaDate.trecena, people = filteredPeople, onPersonClick = { onNavigateToPerson(it.id) })
                        }
                    }

                    VerticalDivider()

                    // Right Column: Character ONLY
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(text = stringResource(R.string.character), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        MarkdownAsset("md/character/$lang/character_${person.gender}_kin${mayaDate.kin}.md")
                    }
                }
            } else {
                // Mobile Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.navigateToCharacter(mayaDate.daysign) }) {
                            ImageSign(sign = mayaDate.daysign, size = 120.dp)
                            Text(text = daysignNames.getOrElse(mayaDate.daysign) { "" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.navigateToCharacter(mayaDate.trecena) }) {
                            ImageSign(sign = mayaDate.trecena, size = 120.dp)
                            Text(text = daysignNamesTo.getOrElse(mayaDate.trecena) { "" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    AccordionItem(title = stringResource(R.string.phantoms), isExpanded = expandedSection == "phantoms", onToggle = { expandedSection = if (expandedSection == "phantoms") null else "phantoms" }, visible = mayaDate.fantoms.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            mayaDate.fantoms.forEach { phantom ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.navigateToCharacter(phantom) }) {
                                    ImageSign(sign = phantom, size = 48.dp)
                                    Text(daysignNames.getOrElse(phantom) { "" }, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    AccordionItem(title = stringResource(R.string.character), isExpanded = expandedSection == "character", onToggle = { expandedSection = if (expandedSection == "character") null else "character" }) {
                        Box(modifier = Modifier.padding(16.dp)) { MarkdownAsset("md/character/$lang/character_${person.gender}_kin${mayaDate.kin}.md") }
                    }
                    AccordionItem(title = stringResource(R.string.info), isExpanded = expandedSection == "info", onToggle = { expandedSection = if (expandedSection == "info") null else "info" }) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            InfoRow(stringResource(R.string.birth_gregorian), "${person.birthDate.format(dateFormatter)} (${if (person.sunrise == "before") stringResource(R.string.before_sunrise) else stringResource(R.string.after_sunrise)})", onClick = { viewModel.navigateToPage(1, person.birthDate) })
                            InfoRow(stringResource(R.string.birth_longcount), "${mayaDate.longCount.baktun}.${mayaDate.longCount.katun}.${mayaDate.longCount.tun}.${mayaDate.longCount.uinal}.${mayaDate.longCount.day}")
                            InfoRow(stringResource(R.string.kin_label), mayaDate.kin.toString())
                            InfoRow(stringResource(R.string.count_days), diffDays.toString())
                            if (isBorn) {
                                InfoRow(stringResource(R.string.age_sign), "${daysignNames.getOrElse(ageMaya.first) { "" }} ${daysignNamesTo.getOrElse(ageMaya.second) { "" }}")
                                InfoRow(stringResource(R.string.age_kin), ageKin.toString())
                                InfoRow(stringResource(R.string.age_gregorian), (diffDays / 365.25).toInt().toString())
                                InfoRow(stringResource(R.string.next_maya_day), "${nextMayaBirth.format(dateFormatter)}, ${stringResource(R.string.in_days, toNextBirth)}", onClick = { viewModel.navigateToPage(1, nextMayaBirth) })
                            }
                        }
                    }
                    AccordionItem(title = stringResource(R.string.map_connections), isExpanded = expandedSection == "map", onToggle = { expandedSection = if (expandedSection == "map") null else "map" }) { ConnectionCircle(daysign = mayaDate.daysign, trecena = mayaDate.trecena) }
                    AccordionItem(title = stringResource(R.string.groups), isExpanded = expandedSection == "groups", onToggle = { expandedSection = if (expandedSection == "groups") null else "groups" }) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            personGroups.forEach { group -> SuggestionChip(onClick = { onNavigateToGroup(group.id) }, label = { Text(group.name) }, modifier = Modifier.padding(bottom = 4.dp)) }
                            val availableGroups = allGroups.filter { personId !in it.memberIds }
                            if (availableGroups.isNotEmpty()) {
                                var showSelectGroupDialog by remember { mutableStateOf(false) }
                                AssistChip(onClick = { showSelectGroupDialog = true }, label = { Text(stringResource(R.string.add_to_group)) }, leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(AssistChipDefaults.IconSize)) }, modifier = Modifier.padding(top = 8.dp))
                                if (showSelectGroupDialog) { SelectGroupDialog(groups = availableGroups, onDismiss = { showSelectGroupDialog = false }, onConfirm = { viewModel.addPersonToGroup(personId, it); showSelectGroupDialog = false }) }
                            }
                        }
                    }
                    AccordionItem(title = (if (defaultGroupId != null) stringResource(R.string.connections_people) + " (${allGroups.find { it.id == defaultGroupId }?.name})" else if (favoritesIds.isNotEmpty()) stringResource(R.string.favorites) else stringResource(R.string.connections_people)), isExpanded = expandedSection == "connections", onToggle = { expandedSection = if (expandedSection == "connections") null else "connections" }) {
                        val filterIds = allGroups.find { it.id == defaultGroupId }?.memberIds
                        // Kept in a keyed remember so the list is not rebuilt on every
                        // recomposition and InfluenceList can actually skip.
                        val filteredPeople = remember(allPeople, filterIds, favoritesIds, person.id) {
                            val ids = filterIds?.toHashSet()
                            if (ids != null) allPeople.filter { it.id in ids && it.id != person.id }
                            else if (favoritesIds.isNotEmpty()) allPeople.filter { it.id in favoritesIds && it.id != person.id }
                            else allPeople.filter { it.id != person.id }
                        }
                        InfluenceList(targetKinDaysign = mayaDate.daysign, targetKinTrecena = mayaDate.trecena, people = filteredPeople, onPersonClick = { onNavigateToPerson(it.id) }, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AddPersonDialog(
            person = person,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, date, gender, sunrise ->
                viewModel.addPerson(person.copy(name = name, birthDate = date, gender = gender, sunrise = sunrise))
                showEditDialog = false
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
        Text(
            text = value, 
            textAlign = TextAlign.End, 
            modifier = Modifier.weight(1f),
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SelectGroupDialog(
    groups: List<calendar.maya.daysign.model.Group>,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_to_group)) },
        text = {
            LazyColumn {
                items(groups) { group ->
                    ListItem(
                        modifier = Modifier.clickable { onConfirm(group.id) },
                        headlineContent = { Text(group.name) }
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
