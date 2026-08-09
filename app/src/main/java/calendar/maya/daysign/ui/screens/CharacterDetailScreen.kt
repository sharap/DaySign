package calendar.maya.daysign.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.R
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.ui.components.ImageSign
import calendar.maya.daysign.ui.components.MarkdownAsset
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(characterId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val lang = remember { Locale.getDefault().language }
    val daysignNames = stringArrayResource(id = R.array.daysign_names)
    val daysignNamesGenitive = stringArrayResource(id = R.array.daysign_names_genitive)
    val daysignNamesAccusative = stringArrayResource(id = R.array.daysign_names_accusative)
    val title = daysignNames.getOrElse(characterId) { "" }

    var expandedSection by remember { mutableStateOf<String?>("character") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$title ($characterId)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val scope = this
            val isWide = scope.maxWidth > 800.dp
            
            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Side: Character Description
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(R.string.character),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MarkdownAsset("md/character/$lang/character_$characterId.md")
                    }
                    
                    VerticalDivider()

                    // Right Side: Symbol and Connections
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.symbol_meaning),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ImageSign(sign = characterId, size = 180.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        MarkdownAsset("md/character/$lang/symbol_$characterId.md")
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = stringResource(R.string.connections_people),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ConnectionsList(
                            characterId = characterId,
                            names = daysignNames,
                            namesGenitive = daysignNamesGenitive,
                            namesAccusative = daysignNamesAccusative,
                            lang = lang
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Photo
                    val imageRes = context.resources.getIdentifier("photo$characterId", "drawable", context.packageName)
                    if (imageRes != 0) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ExpandableSection(
                        title = stringResource(R.string.character),
                        expanded = expandedSection == "character",
                        onToggle = { expandedSection = if (expandedSection == "character") null else "character" }
                    ) {
                        MarkdownAsset("md/character/$lang/character_$characterId.md")
                    }
                    
                    HorizontalDivider()

                    ExpandableSection(
                        title = stringResource(R.string.symbol_meaning),
                        expanded = expandedSection == "symbol",
                        onToggle = { expandedSection = if (expandedSection == "symbol") null else "symbol" }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ImageSign(sign = characterId, size = 200.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            MarkdownAsset("md/character/$lang/symbol_$characterId.md")
                        }
                    }

                    HorizontalDivider()

                    ExpandableSection(
                        title = stringResource(R.string.connections_people),
                        expanded = expandedSection == "connections",
                        onToggle = { expandedSection = if (expandedSection == "connections") null else "connections" }
                    ) {
                        ConnectionsList(
                            characterId = characterId,
                            names = daysignNames,
                            namesGenitive = daysignNamesGenitive,
                            namesAccusative = daysignNamesAccusative,
                            lang = lang
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
        }
        if (expanded) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ConnectionsList(
    characterId: Int,
    names: Array<String>,
    namesGenitive: Array<String>,
    namesAccusative: Array<String>,
    lang: String
) {
    val connections = MayaCalendar.getConnections(characterId)
    if (connections.isEmpty()) return

    val connectionTypes = listOf(
        "support_to", "support_from", "control_to", "control_from", "passion", "friend", "secret"
    )

    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        connections.forEachIndexed { index, connId ->
            val type = connectionTypes.getOrElse(index) { "" }
            
            val connTitle = if (lang == "ru") {
                when (index) {
                    0 -> "${names.getOrElse(characterId) { "" }} поддерживает ${namesAccusative.getOrElse(connId) { "" }}"
                    1 -> "${names.getOrElse(connId) { "" }} поддерживает ${namesAccusative.getOrElse(characterId) { "" }}"
                    2 -> "${names.getOrElse(characterId) { "" }} контролирует ${namesAccusative.getOrElse(connId) { "" }}"
                    3 -> "${names.getOrElse(connId) { "" }} контролирует ${namesAccusative.getOrElse(characterId) { "" }}"
                    4 -> "страсть/испытание ${namesGenitive.getOrElse(connId) { "" }}"
                    5 -> "партнёр/друг ${namesGenitive.getOrElse(connId) { "" }}"
                    6 -> "тайная сила ${namesGenitive.getOrElse(connId) { "" }}"
                    else -> ""
                }
            } else {
                when (index) {
                    0 -> "${names.getOrElse(characterId) { "" }} supports ${names.getOrElse(connId) { "" }}"
                    1 -> "${names.getOrElse(connId) { "" }} supports ${names.getOrElse(characterId) { "" }}"
                    2 -> "${names.getOrElse(characterId) { "" }} controls ${names.getOrElse(connId) { "" }}"
                    3 -> "${names.getOrElse(connId) { "" }} controls ${names.getOrElse(characterId) { "" }}"
                    4 -> "passion/challenge of ${names.getOrElse(connId) { "" }}"
                    5 -> "partner/friend ${names.getOrElse(connId) { "" }}"
                    6 -> "secret power of ${names.getOrElse(connId) { "" }}"
                    else -> ""
                }
            }

            ExpandableConnectionItem(
                title = connTitle,
                signId = connId,
                characterId = characterId,
                connId = connId,
                type = type,
                lang = lang,
                expanded = expandedIndex == index,
                onToggle = { expandedIndex = if (expandedIndex == index) -1 else index }
            )
        }
    }
}

@Composable
fun ExpandableConnectionItem(
    title: String,
    signId: Int,
    characterId: Int,
    connId: Int,
    type: String,
    lang: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageSign(sign = signId, size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, modifier = Modifier.weight(1f), fontSize = 14.sp)
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                    MarkdownAsset("md/connection/$lang/sign_${characterId}_${type}_${connId}.md")
                }
            }
        }
    }
}
