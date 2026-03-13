package com.claw.api.ui.gmail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.claw.api.data.AuthManager
import com.claw.api.data.gmail.EmailMessage
import com.claw.api.data.gmail.GmailRepository
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import kotlinx.coroutines.launch

class GmailViewModel : ViewModel() {
    var messages by mutableStateOf<List<EmailMessage>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var searchQuery by mutableStateOf("")
        private set
    
    private var gmailRepository: GmailRepository? = null
    
    fun initialize(credential: GoogleAccountCredential) {
        val gmailService = Gmail.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Claw API").build()
        
        gmailRepository = GmailRepository(gmailService)
        loadMessages()
    }
    
    fun loadMessages() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = gmailRepository?.listMessages()
            result?.fold(
                onSuccess = { messageList ->
                    messages = messageList
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.message
                    isLoading = false
                }
            )
        }
    }
    
    fun searchMessages(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadMessages()
                return@launch
            }
            
            isLoading = true
            errorMessage = null
            
            val result = gmailRepository?.searchMessages(query)
            result?.fold(
                onSuccess = { messageList ->
                    messages = messageList
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.message
                    isLoading = false
                }
            )
        }
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GmailScreen(viewModel: GmailViewModel = viewModel()) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    
    LaunchedEffect(Unit) {
        authManager.getSignedInAccount()?.let { account ->
            viewModel.initialize(authManager.getGmailCredential(account))
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search Bar
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { query ->
                viewModel.updateSearchQuery(query)
                if (query.length >= 3) {
                    viewModel.searchMessages(query)
                } else if (query.isEmpty()) {
                    viewModel.loadMessages()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("搜索邮件...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        viewModel.updateSearchQuery("")
                        viewModel.loadMessages()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            }
        )
        
        // Content
        when {
            viewModel.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            viewModel.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = viewModel.errorMessage ?: "未知错误",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadMessages() }) {
                            Text("重试")
                        }
                    }
                }
            }
            
            viewModel.messages.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "没有找到邮件",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(viewModel.messages) { message ->
                        EmailItem(message = message)
                    }
                }
            }
        }
    }
}

@Composable
fun EmailItem(message: EmailMessage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
                // TODO: 打开邮件详情
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isUnread) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = message.from ?: "未知发件人",
                        fontWeight = if (message.isUnread) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    message.date?.let { date ->
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = message.subject ?: "(无主题)",
                    fontWeight = if (message.isUnread) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = message.snippet ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (message.isUnread) {
                Icon(
                    Icons.Default.Circle,
                    contentDescription = "未读",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
