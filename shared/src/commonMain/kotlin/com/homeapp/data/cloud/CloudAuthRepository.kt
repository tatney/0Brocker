package com.homeapp.data.cloud

import com.homeapp.data.model.*
import com.homeapp.data.repository.*
import com.homeapp.db.HomeAppDatabase
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*

class CloudAuthRepository(private val db: HomeAppDatabase): AuthRepository {
    private val state=MutableStateFlow<AuthState>(AuthState.SignedOut)
    override fun observeSession(): Flow<AuthState> = state.asStateFlow()
    suspend fun restore() { if(SupabaseGateway.restore()) runCatching { profile() }.onFailure {SupabaseGateway.clear()} }
    private suspend fun profile(): SignInResult {
        val p=SupabaseGateway.rpc("ensure_my_profile").jsonObject
        val id=1000000000000L+p.long("id")
        val user=User(id,p.string("full_name"),p.string("phone"),p.string("email"),p.string("avatar_emoji","🙂"),p.bool("is_professional"))
        val q=db.homeAppDatabaseQueries
        if(q.selectAllUsers().executeAsList().none {it.id==id}) q.insertUser(id,user.fullName,user.phone,user.email,user.avatarEmoji,0,"")
        else q.updateUserProfile(user.fullName,user.avatarEmoji,id)
        state.value=AuthState.SignedIn(user,user.fullName.isNotBlank())
        return SignInResult(user,user.fullName.isNotBlank())
    }
    override suspend fun signIn(email: String,password: String): Result<SignInResult> = runCatching {
        SupabaseGateway.acceptSession(SupabaseGateway.auth("token?grant_type=password",json("email" to email.trim(),"password" to password)).jsonObject)
        profile()
    }.onFailure { state.value=AuthState.SignedOut;SupabaseGateway.clear() }
    override suspend fun signUp(email: String,password: String): Result<Unit> = runCatching {
        val result=SupabaseGateway.auth("signup",json("email" to email.trim(),"password" to password)).jsonObject
        if(result.string("access_token").isBlank()) throw IllegalStateException("Check your email to confirm the account, then sign in.")
        SupabaseGateway.acceptSession(result);profile();Unit
    }
    override suspend fun completeOnboarding(fullName: String,avatarEmoji: String) {
        SupabaseGateway.rpc("update_my_profile",json("p_name" to fullName,"p_avatar" to avatarEmoji));profile()
    }
    override suspend fun signOut() {
        try { runCatching {SupabaseGateway.logout()} } finally { SupabaseGateway.clear();state.value=AuthState.SignedOut }
    }
}
