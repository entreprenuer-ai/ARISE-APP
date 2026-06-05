package com.example.core.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Singleton client for all Supabase cloud synchronization integrations.
 * Centrally manages the single OkHttpClient instance as well as base configuration properties.
 */
object SupabaseClient {
    const val URL = "https://qlxdvttdeozrpqbtacrp.supabase.co"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFseGR2dHRkZW96cnBxYnRhY3JwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAzMTcwMjgsImV4cCI6MjA5NTg5MzAyOH0.7VAV1Z4urhx8iWa3-ZDX7JlLbJmXmZMGURL9YEIu6-4"

    // Secure, hardcoded credentials for the platform admin account
    const val ADMIN_EMAIL = "veerendrabotla346@gmail.com"
    const val ADMIN_PASSWORD = "Test@123"

    @Volatile
    private var cachedAdminToken: String? = null

    // Single OkHttpClient instance for the entire application to optimize resource reuse
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    /**
     * Authenticates with Supabase using the admin credentials and returns the access_token.
     * Caches the token to avoid redundant login network requests during the session.
     */
    suspend fun getAdminSessionToken(): String {
        cachedAdminToken?.let { return it }
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("email", ADMIN_EMAIL)
                    put("password", ADMIN_PASSWORD)
                }
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = payload.toString().toRequestBody(mediaType)
                
                val loginUrl = "$URL/auth/v1/token?grant_type=password"
                val request = Request.Builder()
                    .url(loginUrl)
                    .post(body)
                    .addHeader("apikey", ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body?.string() ?: ""
                        val json = JSONObject(respBody)
                        val token = json.getString("access_token")
                        cachedAdminToken = token
                        token
                    } else {
                        ANON_KEY // Fallback
                    }
                }
            } catch (e: Exception) {
                ANON_KEY // Fallback under network issues
            }
        }
    }

    /**
     * Helper to create a request builder pre-populated with standard Supabase headers.
     * @param path The endpoint path (e.g. "/rest/v1/arise_backups") or absolute URL.
     * @param token Authentication token (JWT), defaults to using ANON_KEY Bearer token if null.
     */
    fun newRequestBuilder(path: String, token: String? = null): Request.Builder {
        val fullUrl = if (path.startsWith("http")) path else "$URL$path"
        val requestToken = token ?: ANON_KEY
        return Request.Builder()
            .url(fullUrl)
            .addHeader("apikey", ANON_KEY)
            .addHeader("Authorization", "Bearer $requestToken")
            .addHeader("Content-Type", "application/json")
    }
}
