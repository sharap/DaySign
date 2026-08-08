package calendar.maya.daysign.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.model.Person
import calendar.maya.daysign.ui.MainViewModel
import calendar.maya.daysign.ui.components.ImageSign
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val people by viewModel.people.collectAsState()
    val groups by viewModel.groups.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Люди", "Группы")
    
    var showAddPersonDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Люди и Группы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                if (selectedTab == 0) showAddPersonDialog = true 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> PeopleList(people, onDelete = { viewModel.deletePerson(it) })
                1 -> GroupList(groups)
            }
        }
    }
    
    if (showAddPersonDialog) {
        AddPersonDialog(
            onDismiss = { showAddPersonDialog = false },
            onConfirm = { name, date ->
                viewModel.addPerson(Person(name = name, birthDate = date, gender = "male", sunrise = "after"))
                showAddPersonDialog = false
            }
        )
    }
}

@Composable
fun PeopleList(people: List<Person>, onDelete: (Person) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(people) { person ->
            val mayaDate = MayaCalendar.maya(person.birthDate)
            ListItem(
                headlineContent = { Text(person.name) },
                supportingContent = { Text("${person.birthDate}, Кин ${mayaDate.kin}") },
                leadingContent = {
                    Row {
                        ImageSign(sign = mayaDate.daysign, size = 40.dp)
                        Spacer(modifier = Modifier.width(4.dp))
                        ImageSign(sign = mayaDate.trecena, size = 40.dp)
                    }
                },
                trailingContent = {
                    IconButton(onClick = { onDelete(person) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun GroupList(groups: List<calendar.maya.daysign.model.Group>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(groups) { group ->
            ListItem(
                headlineContent = { Text(group.name) },
                supportingContent = { Text("${group.memberIds.size} человек") }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun AddPersonDialog(onDismiss: () -> Unit, onConfirm: (String, LocalDate) -> Unit) {
    var name by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(LocalDate.now().toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить человека") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("Дата рождения (YYYY-MM-DD)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    val date = LocalDate.parse(dateString)
                    onConfirm(name, date)
                } catch (e: Exception) {
                    // Handle parse error
                }
            }) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
