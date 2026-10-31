@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.aviatoranalysis



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.security.MessageDigest
import java.util.Locale
import androidx.compose.foundation.text.selection.SelectionContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { AviatorApp() } }
    }
}

@Composable
fun AviatorApp() {
    var tab by remember { mutableStateOf(0) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Aviator Analysis Pro") })
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = tab) {
                listOf("Probability", "History", "SHA-256").forEachIndexed { i, s ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(s) })
                }
            }
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                when (tab) {
                    0 -> ProbabilityScreen()
                    1 -> HistoryScreen()
                    else -> HashScreen()
                }
                Spacer(Modifier.height(18.dp))
                                Text(
                    "Important: This app does not predict future crash points. Results are statistical estimates only.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(24.dp))

                HorizontalDivider()

                Spacer(Modifier.height(12.dp))

                Text(
                    "Developed by Wishva Dilshan",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
@Composable
fun ProbabilityScreen() {
    var multiplier by remember { mutableStateOf("2.0") }
    var stake by remember { mutableStateOf("100") }
    val m = multiplier.toDoubleOrNull()
    val s = stake.toDoubleOrNull()
    Text("Multiplier probability estimate", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(multiplier, { multiplier = it }, label = { Text("Target multiplier (e.g. 2.0)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
    OutlinedTextField(stake, { stake = it }, label = { Text("Bet amount (Rs.)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
    Spacer(Modifier.height(12.dp))
    if (m != null && s != null && m >= 1.0 && s >= 0) {
        val probability = (0.97 / m * 100.0).coerceAtMost(100.0)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Estimated chance of reaching $m×: ${fmt(probability)}%")
                Text("Cash-out return if successful: Rs. ${fmt(s * m)}")
                Text("Net profit if successful: Rs. ${fmt(s * (m - 1))}")
                Text("Loss if crash happens first: Rs. ${fmt(s)}")
            }
        }
        Text("This uses an illustrative 97% RTP model, not a confirmed live 1xBet round formula.",
            Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
    } else Text("Enter a multiplier of at least 1.0 and a valid stake.")
}

@Composable
fun HistoryScreen() {
    var input by remember { mutableStateOf("1.20, 2.15, 1.03, 5.40, 1.80") }
    Text("Analyze past round results", style = MaterialTheme.typography.titleLarge)
    Text("Enter multipliers separated by commas, spaces, or new lines.")
    OutlinedTextField(input, { input = it }, modifier = Modifier.fillMaxWidth().height(140.dp),
        label = { Text("Round multipliers") })
    val values = remember(input) {
        input.split(Regex("[,\\s]+")).mapNotNull { it.trim().toDoubleOrNull() }.filter { it >= 1.0 }
    }
    Spacer(Modifier.height(12.dp))
    if (values.isNotEmpty()) {
        val sorted = values.sorted()
        val avg = values.average()
        val low = values.count { it < 2.0 }
        val mid = values.count { it >= 2.0 && it < 10.0 }
        val high = values.count { it >= 10.0 }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Rounds analyzed: ${values.size}")
                Text("Average: ${fmt(avg)}×")
                Text("Lowest: ${fmt(sorted.first())}×")
                Text("Highest: ${fmt(sorted.last())}×")
                Text("Below 2×: $low (${fmt(low * 100.0 / values.size)}%)")
                Text("2× to below 10×: $mid (${fmt(mid * 100.0 / values.size)}%)")
                Text("10× or higher: $high (${fmt(high * 100.0 / values.size)}%)")
            }
        }
        Text("Past-round frequencies describe only the entered sample. They do not forecast the next round.",
            Modifier.padding(top = 8.dp), style = MaterialTheme.typography.bodySmall)
    } else Text("Enter valid multipliers (1.0 or higher).")
}

@Composable
fun HashScreen() {
    var source by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    Text("SHA-256 hash tool", style = MaterialTheme.typography.titleLarge)
    Text("Generate a SHA-256 digest from text you enter. This alone does not verify an Aviator round.")
    OutlinedTextField(source, { source = it; result = "" },
        modifier = Modifier.fillMaxWidth().height(120.dp), label = { Text("Text / seed") })
    Spacer(Modifier.height(8.dp))
    Button(onClick = {
        val bytes = MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8))
        result = bytes.joinToString("") { "%02x".format(it) }
    }, enabled = source.isNotEmpty()) { Text("Generate SHA-256") }
    if (result.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        Text("Digest:")
        SelectionText(result)
    }
    Text("A valid Provably Fair verification needs the game's exact published algorithm, seed rules, nonce and round data. A generic hash is not enough.",
        Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodySmall)
}

@Composable
fun SelectionText(value: String) {
    SelectionContainer { Text(value) }
}

fun fmt(value: Double): String = String.format(Locale.US, "%.2f", value)
