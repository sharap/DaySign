package calendar.maya.daysign.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import calendar.maya.daysign.ui.components.getSignColor
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
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
import calendar.maya.daysign.model.Person
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.AccordionItem
import calendar.maya.daysign.ui.components.ImageSign
import calendar.maya.daysign.ui.components.InfluenceList
import calendar.maya.daysign.ui.components.rememberProgressiveCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Int,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToPerson: (Int) -> Unit
) {
    val groups by viewModel.groups.collectAsState()
    val allPeople by viewModel.allPeople.collectAsState()
    val defaultGroupId by viewModel.defaultGroupId.collectAsState()
    val favoritesIds by viewModel.favoritesIds.collectAsState()
    
    val isFavorites = groupId == -1
    val group = if (isFavorites) {
        calendar.maya.daysign.model.Group(
            id = -1,
            name = stringResource(R.string.favorites),
            description = "",
            memberIds = favoritesIds.toList()
        )
    } else {
        groups.find { it.id == groupId }
    }
    
    val isDefault = if (isFavorites) defaultGroupId == null else defaultGroupId == groupId
    // memberIds is a List, so "in" was a linear scan per person on every
    // recomposition; keyed remember + Set makes this O(n) and stable.
    val memberIds = group?.memberIds ?: emptyList()
    val members = remember(allPeople, memberIds) {
        val ids = memberIds.toHashSet()
        allPeople.filter { it.id in ids }
    }

    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showEditGroupDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isFavorites) {
                            if (!isDefault) viewModel.setDefaultGroup(null)
                        } else {
                            if (isDefault) viewModel.setDefaultGroup(null)
                            else viewModel.setDefaultGroup(groupId)
                        }
                    }) {
                        Icon(
                            imageVector = if (isDefault) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.set_as_default),
                            tint = if (isDefault) Color.Red else LocalContentColor.current
                        )
                    }
                    if (!isFavorites) {
                        IconButton(onClick = { showEditGroupDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit))
                        }
                        IconButton(onClick = { 
                            group?.let { viewModel.deleteGroup(it) }
                            onBack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val scope = this
            val isWide = scope.maxWidth > 800.dp
            var expandedSection by remember { mutableStateOf<String?>("people") }
            var includeFantoms by remember { mutableStateOf(false) }

            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Column: Members
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.people), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (!isFavorites) {
                                item {
                                    ListItem(
                                        modifier = Modifier.clickable { showAddPersonDialog = true },
                                        headlineContent = { Text(stringResource(R.string.add_person), color = MaterialTheme.colorScheme.primary) },
                                        leadingContent = { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) }
                                    )
                                    HorizontalDivider()
                                }
                            }
                            items(members) { person ->
                                var personExpanded by remember { mutableStateOf(false) }
                                val personMaya = if (person.sunrise == "before") MayaCalendar.maya(person.birthDate.minusDays(1)) else MayaCalendar.maya(person.birthDate)
                                ListItem(
                                    modifier = Modifier.clickable { personExpanded = !personExpanded },
                                    headlineContent = { Text(person.name) },
                                    leadingContent = { Row { ImageSign(sign = personMaya.daysign, size = 32.dp); ImageSign(sign = personMaya.trecena, size = 32.dp) } },
                                    trailingContent = {
                                        IconButton(onClick = { if (isFavorites) viewModel.toggleFavorite(person.id) else { val updatedIds = group!!.memberIds.filter { it != person.id }; viewModel.addGroup(group.copy(memberIds = updatedIds)) } }) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove))
                                        }
                                    }
                                )
                                if (personExpanded) {
                                    InfluenceList(targetKinDaysign = personMaya.daysign, targetKinTrecena = personMaya.trecena, people = members.filter { it.id != person.id }, onPersonClick = { onNavigateToPerson(it.id) }, modifier = Modifier.padding(start = 16.dp))
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                    VerticalDivider()
                    // Right Column: Result
                    Column(
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stringResource(R.string.result_group), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeFantoms = !includeFantoms }) {
                                Checkbox(checked = includeFantoms, onCheckedChange = { includeFantoms = it })
                                Text(stringResource(R.string.phantoms), fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        ResultGroupSection(members, includeFantoms)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                    if (group?.description?.isNotEmpty() == true) {
                        Text(group.description, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(16.dp))
                    }
                    AccordionItem(title = stringResource(R.string.people), isExpanded = expandedSection == "people", onToggle = { expandedSection = if (expandedSection == "people") null else "people" }) {
                        Column {
                            if (!isFavorites) {
                                ListItem(modifier = Modifier.clickable { showAddPersonDialog = true }, headlineContent = { Text(stringResource(R.string.add_person), color = MaterialTheme.colorScheme.primary) }, leadingContent = { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) })
                                HorizontalDivider()
                            }
                            val visibleMembers = rememberProgressiveCount(members.size)
                            for (i in 0 until visibleMembers) {
                                val person = members[i]
                                var personExpanded by remember { mutableStateOf(false) }
                                val personMaya = if (person.sunrise == "before") MayaCalendar.maya(person.birthDate.minusDays(1)) else MayaCalendar.maya(person.birthDate)
                                ListItem(modifier = Modifier.clickable { personExpanded = !personExpanded }, headlineContent = { Text(person.name) }, leadingContent = { Row { ImageSign(sign = personMaya.daysign, size = 32.dp); ImageSign(sign = personMaya.trecena, size = 32.dp) } }, trailingContent = { IconButton(onClick = { if (isFavorites) viewModel.toggleFavorite(person.id) else { val updatedIds = group!!.memberIds.filter { it != person.id }; viewModel.addGroup(group.copy(memberIds = updatedIds)) } }) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove)) } })
                                if (personExpanded) { InfluenceList(targetKinDaysign = personMaya.daysign, targetKinTrecena = personMaya.trecena, people = members.filter { it.id != person.id }, onPersonClick = { onNavigateToPerson(it.id) }, modifier = Modifier.padding(start = 16.dp)) }
                                HorizontalDivider()
                            }
                        }
                    }
                    AccordionItem(title = stringResource(R.string.result_group), isExpanded = expandedSection == "result", onToggle = { expandedSection = if (expandedSection == "result") null else "result" }) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeFantoms = !includeFantoms }) {
                                    Checkbox(checked = includeFantoms, onCheckedChange = { includeFantoms = it })
                                    Text(stringResource(R.string.phantoms), fontSize = 12.sp)
                                }
                            }
                            ResultGroupSection(members, includeFantoms)
                        }
                    }
                }
            }
        }
    }

    if (showAddPersonDialog) {
        group?.let { g ->
            SelectPeopleDialog(
                allPeople = allPeople,
                alreadyInGroup = g.memberIds,
                onDismiss = { showAddPersonDialog = false },
                onConfirm = { selectedIds ->
                    val updatedIds = (g.memberIds + selectedIds).distinct()
                    viewModel.addGroup(g.copy(memberIds = updatedIds))
                    showAddPersonDialog = false
                }
            )
        }
    }

    if (showEditGroupDialog) {
        group?.let { g ->
            EditGroupDialog(
                group = g,
                onDismiss = { showEditGroupDialog = false },
                onConfirm = { name, desc ->
                    viewModel.addGroup(g.copy(name = name, description = desc))
                    showEditGroupDialog = false
                }
            )
        }
    }
}

@Composable
fun EditGroupDialog(
    group: calendar.maya.daysign.model.Group,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(group.name) }
    var description by remember { mutableStateOf(group.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_group)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_group)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.description_placeholder)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, description) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SelectPeopleDialog(
    allPeople: List<Person>,
    alreadyInGroup: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(setOf<Int>()) }

    val filteredPeople = allPeople.filter { person ->
        person.id !in alreadyInGroup && person.name.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_to_group)) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredPeople) { person ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (selectedIds.contains(person.id)) {
                                        selectedIds - person.id
                                    } else {
                                        selectedIds + person.id
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedIds.contains(person.id),
                                onCheckedChange = null
                            )
                            Text(person.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty()
            ) {
                Text(stringResource(R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ResultGroupSection(members: List<Person>, includeFantoms: Boolean) {
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    var sortedIntersections by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(members, includeFantoms) {
        isLoading = true
        val counts = IntArray(21)
        members.forEach { person ->
            val maya = if (person.sunrise == "before") {
                MayaCalendar.maya(person.birthDate.minusDays(1))
            } else {
                MayaCalendar.maya(person.birthDate)
            }
            
            val c1 = MayaCalendar.getConnections(maya.daysign)
            val c2 = MayaCalendar.getConnections(maya.trecena)
            
            c1.forEach { if (it > 0) counts[it]++ }
            c2.forEach { if (it > 0) counts[it]++ }

            if (includeFantoms) {
                maya.fantoms.forEach { phantom ->
                    if (phantom > 0) {
                        counts[phantom]++
                        MayaCalendar.getConnections(phantom).forEach { if (it > 0) counts[it]++ }
                    }
                }
            }
        }
        
        sortedIntersections = counts.indices.filter { it > 0 }
            .map { it to counts[it] }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
        
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else {
        Column {
            sortedIntersections.forEach { (sign, count) ->
                ListItem(
                    headlineContent = { Text("${daysignNames.getOrElse(sign) { "" }} ($sign)") },
                    trailingContent = { Text(count.toString()) },
                    leadingContent = { ImageSign(sign = sign, size = 32.dp) },
                    colors = ListItemDefaults.colors(
                        containerColor = getSignColor(sign).copy(alpha = 0.1f)
                    )
                )
                HorizontalDivider()
            }
        }
    }
}
