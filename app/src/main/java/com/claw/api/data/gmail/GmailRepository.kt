package com.claw.api.data.gmail

import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class EmailMessage(
    val id: String,
    val threadId: String,
    val from: String?,
    val to: String?,
    val subject: String?,
    val snippet: String?,
    val date: String?,
    val isUnread: Boolean
)

class GmailRepository(private val gmailService: Gmail) {
    
    private val USER_ID = "me"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    suspend fun listMessages(
        maxResults: Int = 50,
        labelIds: List<String> = listOf("INBOX")
    ): Result<List<EmailMessage>> = withContext(Dispatchers.IO) {
        try {
            val messagesResponse = gmailService.users().messages()
                .list(USER_ID)
                .setLabelIds(labelIds)
                .setMaxResults(maxResults.toLong())
                .execute()
            
            val messages = messagesResponse.messages?.mapNotNull { message ->
                getMessageDetails(message.id)
            } ?: emptyList()
            
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getMessageDetails(messageId: String): EmailMessage? {
        return try {
            val message = gmailService.users().messages()
                .get(USER_ID, messageId)
                .setFormat("metadata")
                .setMetadataHeaders(listOf("From", "To", "Subject", "Date"))
                .execute()
            
            val headers = message.payload?.headers?.associate { 
                it.name to it.value 
            } ?: emptyMap()
            
            EmailMessage(
                id = message.id,
                threadId = message.threadId,
                from = headers["From"],
                to = headers["To"],
                subject = headers["Subject"],
                snippet = message.snippet,
                date = headers["Date"],
                isUnread = message.labelIds?.contains("UNREAD") == true
            )
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun searchMessages(query: String): Result<List<EmailMessage>> = withContext(Dispatchers.IO) {
        try {
            val messagesResponse = gmailService.users().messages()
                .list(USER_ID)
                .setQ(query)
                .execute()
            
            val messages = messagesResponse.messages?.mapNotNull { message ->
                getMessageDetails(message.id)
            } ?: emptyList()
            
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMessage(messageId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val message = gmailService.users().messages()
                .get(USER_ID, messageId)
                .setFormat("full")
                .execute()
            
            val body = message.payload?.body?.data
            Result.success(body ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
