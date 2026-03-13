package com.claw.api.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.drive.DriveScopes
import com.google.api.services.gmail.GmailScopes

class AuthManager(private val context: Context) {
    
    companion object {
        // TODO: 替换为你的 OAuth 2.0 Client ID
        // 从 Google Cloud Console 获取: https://console.cloud.google.com/apis/credentials
        const val SERVER_CLIENT_ID = "YOUR_SERVER_CLIENT_ID_HERE"
        
        val DRIVE_SCOPES = listOf(DriveScopes.DRIVE_READONLY)
        val GMAIL_SCOPES = listOf(GmailScopes.GMAIL_READONLY)
    }
    
    private val googleSignInOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(SERVER_CLIENT_ID)
            .requestScopes(
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_READONLY),
                com.google.android.gms.common.api.Scope(GmailScopes.GMAIL_READONLY)
            )
            .build()
    }
    
    val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, googleSignInOptions)
    }
    
    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    fun getDriveCredential(account: GoogleSignInAccount): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(
            context,
            DRIVE_SCOPES
        ).setBackOff(ExponentialBackOff()).setSelectedAccount(account.account)
    }
    
    fun getGmailCredential(account: GoogleSignInAccount): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(
            context,
            GMAIL_SCOPES
        ).setBackOff(ExponentialBackOff()).setSelectedAccount(account.account)
    }
    
    fun signOut() {
        googleSignInClient.signOut()
    }
}
