package calendar.maya.daysign.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calendar.maya.daysign.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DaySign") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.about_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.about_p1),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.about_p2),
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.about_p3),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.about_version),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
