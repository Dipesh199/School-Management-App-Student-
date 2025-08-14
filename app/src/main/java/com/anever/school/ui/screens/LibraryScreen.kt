package com.anever.school.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anever.school.data.Repository
import com.anever.school.data.model.LoanStatus

@Composable
fun LibraryScreen() {
    val repo = remember { Repository() }
    var tab by remember { mutableIntStateOf(0) } // 0=Browse, 1=My Loans
    var snack by remember { mutableStateOf<String?>(null) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(remember { SnackbarHostState() }) { data -> Snackbar(snackbarData = data) }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Browse") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("My Loans") })
            }
            when (tab) {
                0 -> BrowseTab(repo) { msg -> snack = msg }
                1 -> LoansTab(repo) { msg -> snack = msg }
            }
            if (snack != null) {
                Snackbar(Modifier.padding(16.dp)) { Text(snack!!) }
            }
        }
    }
}

/* ---------------- Browse ---------------- */

@Composable
private fun BrowseTab(repo: Repository, onSnack: (String) -> Unit) {
    var q by remember { mutableStateOf("") }
    var items by remember(q) { mutableStateOf(repo.browseBooks(q)) }

    LaunchedEffect(q) { items = repo.browseBooks(q) }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            label = { Text("Search by title or author") },
            singleLine = true
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (items.isEmpty()) {
                item { Text("No books found") }
            } else {
                items(items, key = { it.book.id }) { row ->
                    BookRow(
                        title = row.book.title,
                        author = row.book.author,
                        isbn = row.book.isbn,
                        copies = row.book.copies,
                        available = row.book.available,
                        isBorrowed = row.isBorrowed,
                        isReservedByMe = row.reservedLoanId != null,
                        onReserve = {
                            val res = repo.reserveBook(row.book.id)
                            onSnack(res.getOrElse { it.message ?: "Error" })
                            items = repo.browseBooks(q)
                        },
                        onCancelReservation = {
                            val id = row.reservedLoanId!!
                            val res = repo.cancelReservation(id)
                            onSnack(res.getOrElse { it.message ?: "Error" })
                            items = repo.browseBooks(q)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookRow(
    title: String,
    author: String,
    isbn: String,
    copies: Int,
    available: Int,
    isBorrowed: Boolean,
    isReservedByMe: Boolean,
    onReserve: () -> Unit,
    onCancelReservation: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(author, style = MaterialTheme.typography.bodyMedium)
            Text("ISBN: $isbn", style = MaterialTheme.typography.bodySmall)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("Copies: $copies") })
                AssistChip(onClick = {}, label = { Text("Available: $available") })
                when {
                    isBorrowed -> AssistChip(onClick = {}, label = { Text("Borrowed") })
                    isReservedByMe -> AssistChip(onClick = {}, label = { Text("Reserved") })
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (isReservedByMe) {
                    OutlinedButton(onClick = onCancelReservation) { Text("Cancel Reservation") }
                } else {
                    Button(
                        onClick = onReserve,
                        enabled = available > 0 && !isBorrowed
                    ) { Text("Reserve") }
                }
            }
        }
    }
}


/* ---------------- Loans ---------------- */

@Composable
private fun LoansTab(repo: Repository, onSnack: (String) -> Unit) {
    var rows by remember { mutableStateOf(repo.getLoansWithMeta()) }

    fun refresh() { rows = repo.getLoansWithMeta() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (rows.isEmpty()) {
            item { Text("No current loans") }
        } else {
            items(rows, key = { it.loan.id }) { row ->
                val isReserved = row.loan.status == LoanStatus.Reserved
                LoanRow(
                    title = row.book.title,
                    author = row.book.author,
                    issue = row.loan.issueDate.toString(),
                    due = row.loan.dueDate.toString(),
                    daysLeft = row.daysLeft,
                    fine = row.fine,
                    renewals = row.loan.renewals,
                    canRenew = !isReserved && row.daysLeft >= 0 && row.loan.renewals < 1,
                    isReserved = isReserved,
                    onRenew = {
                        val res = repo.renewLoan(row.loan.id)
                        onSnack(res.getOrElse { it.message ?: "Error" })
                        rows = repo.getLoansWithMeta()
                    },
                    onCancelReservation = {
                        val res = repo.cancelReservation(row.loan.id)
                        onSnack(res.getOrElse { it.message ?: "Error" })
                        rows = repo.getLoansWithMeta()
                    }
                )
            }

        }
    }
}

@Composable
private fun LoanRow(
    title: String,
    author: String,
    issue: String,
    due: String,
    daysLeft: Int,
    fine: Int,
    renewals: Int,
    canRenew: Boolean,
    isReserved: Boolean,
    onRenew: () -> Unit,
    onCancelReservation: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(author, style = MaterialTheme.typography.bodyMedium)

            if (isReserved) {
                Text("Reservation hold until: $due", style = MaterialTheme.typography.bodySmall)
                AssistChip(onClick = {}, label = { Text("Reserved") })
            } else {
                Text("Issued: $issue • Due: $due", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (daysLeft >= 0) {
                        AssistChip(onClick = {}, label = { Text("${daysLeft}d left") })
                    } else {
                        AssistChip(
                            onClick = {},
                            label = { Text("Overdue: ${-daysLeft}d") },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        )
                    }
                    if (fine > 0) {
                        AssistChip(onClick = {}, label = { Text("Fine: €$fine") })
                    }
                    AssistChip(onClick = {}, label = { Text("Renewals: $renewals/1") })
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (isReserved) {
                    OutlinedButton(onClick = onCancelReservation) { Text("Cancel Reservation") }
                } else {
                    Button(onClick = onRenew, enabled = canRenew) { Text("Renew") }
                }
            }
        }
    }
}
