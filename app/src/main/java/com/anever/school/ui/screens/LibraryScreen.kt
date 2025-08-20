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
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen() {
    val repo = remember { Repository() }

    // UI bits
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Tabs
    var tab by remember { mutableStateOf(0) } // 0: Browse, 1: My Loans

    // Shared state
    var query by remember { mutableStateOf("") }
    var browse by remember { mutableStateOf(repo.browseBooks(query)) }  // List<Repository.BookRow>
    var loans by remember { mutableStateOf(repo.getLoansWithMeta()) }   // List<Repository.LoanMeta>

    fun refreshBrowse() { browse = repo.browseBooks(query) }
    fun refreshLoans() { loans = repo.getLoansWithMeta() }
    fun refreshBoth() { refreshBrowse(); refreshLoans() }

    // Initial + search refresh
    LaunchedEffect(Unit) { refreshBoth() }
    LaunchedEffect(query) { refreshBrowse() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            // Header
            com.anever.school.ui.design.EduHeroHeader(
                title = "Library",
                subtitle = "Browse and manage loans",
                seed = "library"
            )

            // Tabs
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Browse") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("My Loans") })
            }

            if (tab == 0) {
                BrowseTab(
                    query = query,
                    onQueryChange = { query = it },
                    rows = browse,
                    onReserve = { bookId ->
                        val res = repo.reserveBook(bookId)
                        if (res.isSuccess) {
                            refreshBoth()
                            scope.launch { snackbarHostState.showSnackbar(res.getOrNull() ?: "Reserved") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Reserve failed") }
                        }
                    }
                )
            } else {
                LoansTab(
                    rows = loans,
                    onCancelReservation = { loanId ->
                        val res = repo.cancelReservation(loanId)
                        if (res.isSuccess) {
                            refreshBoth()
                            scope.launch { snackbarHostState.showSnackbar(res.getOrNull() ?: "Reservation canceled") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Cancel failed") }
                        }
                    },
                    onRenew = { loanId ->
                        val res = repo.renewLoan(loanId)
                        if (res.isSuccess) {
                            refreshLoans()
                            scope.launch { snackbarHostState.showSnackbar(res.getOrNull() ?: "Renewed") }
                        } else {
                            scope.launch { snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Renew failed") }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BrowseTab(
    query: String,
    onQueryChange: (String) -> Unit,
    rows: List<Repository.BookRow>,
    onReserve: (bookId: String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search by title/author") },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            singleLine = true
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (rows.isEmpty()) {
                item { Text("No books found") }
            } else {
                items(rows, key = { it.book.id }) { row ->
                    val b = row.book
                    val canReserve = b.available > 0 && !row.isBorrowed && row.reservedLoanId == null
                    com.anever.school.ui.design.EduCard(seed = b.title) {
                        ListItem(
                            headlineContent = { Text(b.title, fontWeight = FontWeight.SemiBold) },
                            supportingContent = { Text("by ${b.author} • ${b.copies} copies • Available: ${b.available}") },
                            trailingContent = {
                                Button(
                                    onClick = { onReserve(b.id) },
                                    enabled = canReserve
                                ) { Text(if (row.reservedLoanId != null) "Reserved" else "Reserve") }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoansTab(
    rows: List<Repository.LoanMeta>,
    onCancelReservation: (loanId: String) -> Unit,
    onRenew: (loanId: String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (rows.isEmpty()) {
            item { Text("No active loans") }
        } else {
            items(rows, key = { it.loan.id }) { row ->
                val loan = row.loan
                val book = row.book
                val isReserved = loan.status.name == "Reserved"
                val isCurrent = loan.status.name == "Current"

                com.anever.school.ui.design.EduCard(seed = book.title) {
                    ListItem(
                        headlineContent = { Text(book.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = {
                            Column {
                                Text("Due: ${loan.dueDate}")
                                Text("Days left: ${row.daysLeft} • Fine: ₹${row.fine}")
                            }
                        },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isReserved) {
                                    TextButton(onClick = { onCancelReservation(loan.id) }) { Text("Cancel") }
                                }
                                if (isCurrent) {
                                    Button(onClick = { onRenew(loan.id) }) { Text("Renew") }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
