package calendar.maya.daysign.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import calendar.maya.daysign.R
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.model.Group
import calendar.maya.daysign.model.Person
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.ImageSign
import calendar.maya.daysign.ui.components.SignChips
import java.time.LocalDate

@Composable
fun PeopleScreen(viewModel: MainViewModel, resetTrigger: Int = 0) {
    val navController = rememberNavController()
    val pendingPersonId by viewModel.pendingPersonId.collectAsState()

    LaunchedEffect(resetTrigger) {
        if (resetTrigger > 0) {
            navController.popBackStack(navController.graph.startDestinationId, false)
        }
    }

    LaunchedEffect(pendingPersonId) {
        pendingPersonId?.let { personId ->
            navController.navigate("person/$personId") {
                launchSingleTop = true
            }
            viewModel.consumePendingPersonId()
        }
    }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            PeopleListScreen(
                viewModel = viewModel,
                onNavigateToPerson = { navController.navigate("person/$it") },
                onNavigateToGroup = { navController.navigate("group/$it") },
                onNavigateToImport = { navController.navigate("import") }
            )
        }
        composable("import") {
            ImportDataScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "person/{personId}",
            arguments = listOf(navArgument("personId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getInt("personId") ?: 0
            PersonProfileScreen(
                personId = personId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPerson = { navController.navigate("person/$it") },
                onNavigateToGroup = { navController.navigate("group/$it") }
            )
        }
        composable(
            "group/{groupId}",
            arguments = listOf(navArgument("groupId") { type = NavType.IntType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            GroupDetailScreen(
                groupId = groupId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToPerson = { navController.navigate("person/$it") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleListScreen(
    viewModel: MainViewModel,
    onNavigateToPerson: (Int) -> Unit,
    onNavigateToGroup: (Int) -> Unit,
    onNavigateToImport: () -> Unit
) {
    val people by viewModel.people.collectAsState()
    val allPeople by viewModel.allPeople.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.people), stringResource(R.string.groups))
    
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.people)) },
                actions = {
                    IconButton(onClick = onNavigateToImport) {
                        Icon(Icons.AutoMirrored.Filled.Input, contentDescription = "Import")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                if (selectedTab == 0) showAddPersonDialog = true 
                else showAddGroupDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(if (index == 0) "$title (${allPeople.size})" else "$title (${groups.size})") 
                        }
                    )
                }
            }
            
            if (selectedTab == 0) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    placeholder = { Text(stringResource(R.string.search)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }
            
            when (selectedTab) {
                0 -> PeopleList(people, onClick = onNavigateToPerson)
                1 -> GroupList(groups, viewModel = viewModel, onClick = onNavigateToGroup)
            }
        }
    }
    
    if (showAddPersonDialog) {
        AddPersonDialog(
            onDismiss = { showAddPersonDialog = false },
            onConfirm = { name, date, gender, sunrise ->
                viewModel.addPerson(Person(name = name, birthDate = date, gender = gender, sunrise = sunrise))
                showAddPersonDialog = false
            }
        )
    }
    
    if (showAddGroupDialog) {
        AddGroupDialog(
            onDismiss = { showAddGroupDialog = false },
            onConfirm = { name ->
                viewModel.addGroup(Group(name = name, description = "", memberIds = emptyList())) { id ->
                    onNavigateToGroup(id)
                }
                showAddGroupDialog = false
            }
        )
    }
}

@Composable
fun PeopleList(people: List<Person>, onClick: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(people) { person ->
            val mayaDate = if (person.sunrise == "before") {
                MayaCalendar.maya(person.birthDate.minusDays(1))
            } else {
                MayaCalendar.maya(person.birthDate)
            }
            ListItem(
                modifier = Modifier.clickable { onClick(person.id) },
                headlineContent = { Text(person.name) },
                supportingContent = { Text("${person.birthDate}, ${stringResource(R.string.kin_label)} ${mayaDate.kin}") },
                leadingContent = {
                    Row {
                        ImageSign(sign = mayaDate.daysign, size = 40.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        ImageSign(sign = mayaDate.trecena, size = 40.dp)
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun GroupList(groups: List<Group>, viewModel: MainViewModel, onClick: (Int) -> Unit) {
    val defaultGroupId by viewModel.defaultGroupId.collectAsState()
    val favoritesIds by viewModel.favoritesIds.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (favoritesIds.isNotEmpty()) {
            item {
                val isDefault = defaultGroupId == null
                ListItem(
                    modifier = Modifier.clickable { onClick(-1) },
                    headlineContent = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.favorites))
                            if (isDefault) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Favorite, 
                                    contentDescription = null, 
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    supportingContent = { Text(stringResource(R.string.people_count, favoritesIds.size)) },
                    trailingContent = {
                        IconButton(onClick = {
                            if (!isDefault) viewModel.setDefaultGroup(null)
                        }) {
                            Icon(
                                imageVector = if (isDefault) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = stringResource(R.string.set_as_default),
                                tint = if (isDefault) Color.Red else LocalContentColor.current
                            )
                        }
                    }
                )
                HorizontalDivider()
            }
        }
        
        items(groups) { group ->
            val isDefault = defaultGroupId == group.id
            ListItem(
                modifier = Modifier.clickable { onClick(group.id) },
                headlineContent = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(group.name)
                        if (isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Favorite, 
                                contentDescription = null, 
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                supportingContent = { Text(stringResource(R.string.people_count, group.memberIds.size)) },
                trailingContent = {
                    IconButton(onClick = {
                        if (isDefault) viewModel.setDefaultGroup(null)
                        else viewModel.setDefaultGroup(group.id)
                    }) {
                        Icon(
                            imageVector = if (isDefault) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.set_as_default),
                            tint = if (isDefault) Color.Red else LocalContentColor.current
                        )
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_group)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name_group)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonDialog(
    person: Person? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, String, String) -> Unit
) {
    var name by remember { mutableStateOf(person?.name ?: "") }
    var selectedDate by remember { mutableStateOf(person?.birthDate ?: LocalDate.now()) }
    var gender by remember { mutableStateOf(person?.gender ?: "male") }
    var sunrise by remember { mutableStateOf(person?.sunrise ?: "after") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    val mayaDate = if (sunrise == "before") MayaCalendar.maya(selectedDate.minusDays(1)) else MayaCalendar.maya(selectedDate)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (person == null) stringResource(R.string.add_person) else stringResource(R.string.edit)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name)) }, modifier = Modifier.fillMaxWidth())
                
                OutlinedTextField(
                    value = selectedDate.toString(),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.birth_date)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                
                Text(stringResource(R.string.gender), modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { gender = "male" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = gender == "male", onClick = { gender = "male" })
                    Text(stringResource(R.string.male), modifier = Modifier.padding(start = 8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { gender = "female" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = gender == "female", onClick = { gender = "female" })
                    Text(stringResource(R.string.female), modifier = Modifier.padding(start = 8.dp))
                }

                Text(stringResource(R.string.time_birth), modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { sunrise = "after" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = sunrise == "after", onClick = { sunrise = "after" })
                    Text(stringResource(R.string.after_sunrise), modifier = Modifier.padding(start = 8.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { sunrise = "before" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = sunrise == "before", onClick = { sunrise = "before" })
                    Text(stringResource(R.string.before_sunrise), modifier = Modifier.padding(start = 8.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                SignChips(daysign = mayaDate.daysign, trecena = mayaDate.trecena)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, selectedDate, gender, sunrise) }) { Text(if (person == null) stringResource(R.string.add) else stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var jsonText by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            OutlinedTextField(
                value = jsonText,
                onValueChange = { jsonText = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                label = { Text(stringResource(R.string.json_data_label)) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val exported = viewModel.generateExportJson()
                        jsonText = exported
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Daysign Export", exported)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.export_label))
                }
                Button(
                    onClick = {
                        viewModel.importData(jsonText)
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = jsonText.isNotEmpty()
                ) {
                    Text(stringResource(R.string.import_label))
                }
            }
        }
    }
}
