package calendar.maya.daysign.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import calendar.maya.daysign.R
import calendar.maya.daysign.logic.MayaCalendar
import calendar.maya.daysign.model.Person

@Composable
fun InfluenceList(
    targetKinDaysign: Int,
    targetKinTrecena: Int,
    people: List<Person>,
    onPersonClick: (Person) -> Unit,
    modifier: Modifier = Modifier
) {
    var processedPeople by remember { mutableStateOf<List<Pair<Person, List<Int>>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(people, targetKinDaysign, targetKinTrecena) {
        isLoading = true
        // Process in background to avoid UI lag
        val results = people.map { person ->
            val personMaya = if (person.sunrise == "before") {
                MayaCalendar.maya(person.birthDate.minusDays(1))
            } else {
                MayaCalendar.maya(person.birthDate)
            }
            val connections = MayaCalendar.getKinConnections(
                targetKinDaysign, targetKinTrecena,
                personMaya.daysign, personMaya.trecena
            )
            person to connections
        }.filter { it.second.any { conn -> conn > 0 } }
         .sortedWith(
             compareByDescending<Pair<Person, List<Int>>> { it.second.count { conn -> conn > 0 } }
             .thenBy { it.first.name }
         )
        
        processedPeople = results
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else if (processedPeople.isEmpty()) {
        Text(stringResource(R.string.no_connections), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.secondary)
    } else {
        Column(modifier = modifier) {
            processedPeople.forEach { (person, connections) ->
                val personMaya = if (person.sunrise == "before") {
                    MayaCalendar.maya(person.birthDate.minusDays(1))
                } else {
                    MayaCalendar.maya(person.birthDate)
                }

                ListItem(
                    modifier = Modifier.clickable { onPersonClick(person) },
                    headlineContent = { Text(person.name) },
                    supportingContent = {
                        Text(formatConnections(connections, person.gender))
                    },
                    leadingContent = {
                        Row {
                            ImageSign(sign = personMaya.daysign, size = 32.dp)
                            ImageSign(sign = personMaya.trecena, size = 32.dp)
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun formatConnections(connections: List<Int>, gender: String): String {
    val sb = StringBuilder()
    val isMale = gender == "male"
    
    if (connections[1] > 0) sb.append(stringResource(R.string.support_in)).append(" ")
    if (connections[0] > 0) sb.append(stringResource(R.string.support_out)).append(" ")
    if (connections[3] > 0) sb.append(stringResource(R.string.control_in)).append(" ")
    if (connections[2] > 0) sb.append(stringResource(if (isMale) R.string.control_out_male else R.string.control_out_female)).append(" ")
    if (connections[4] > 0) sb.append(stringResource(R.string.passion)).append(" ")
    if (connections[5] > 0) sb.append(stringResource(R.string.friend)).append(" ")
    if (connections[6] > 0) sb.append(stringResource(R.string.secret)).append(" ")
    if (connections[7] > 0) sb.append(stringResource(R.string.aspirations)).append(" ")
    if (connections[8] > 0) sb.append(stringResource(R.string.equal_sign)).append(" ")
    
    return sb.toString().trim()
}
