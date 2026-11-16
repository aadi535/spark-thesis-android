package com.spark.data.network

import android.util.Log
import com.spark.data.local.AccountSessionManager
import com.spark.data.local.DataStoreManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Best-effort saves survive screen dismissal; local progress remains saved if offline. */
class ProgressSync(private val api: SparkApiService, private val store: DataStoreManager) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val saves = Mutex()
    private var deletingOwner: String? = null

    fun saveDisplayName(name: String) {
        val owner = AccountSessionManager.accountKey ?: return
        if (owner == AccountSessionManager.GUEST || owner == deletingOwner) return
        val session = AccountSessionManager.generation
        scope.launch {
            saves.withLock {
                try {
                    val (token, storedOwner) = store.sessionFlow.first()
                    if (owner == deletingOwner || owner != storedOwner || token.isNullOrBlank() ||
                        session != AccountSessionManager.generation) return@withLock
                    api.saveUserProgress(UserProgressDto(
                        name = name, xp = 0, streak = -1, lastClaimDate = null,
                        isPro = false, proExpiresAt = null, dailySparksCount = 0,
                        dailyWormholeCount = 0, couponRedeemed = false
                    ), "Bearer $token")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w("ProgressSync", "Display name save failed; the local profile is retained.")
                }
            }
        }
    }

    fun saveCompletion(totalXp: Int, history: List<CompletedTaskDto>) {
        val owner = AccountSessionManager.accountKey ?: return
        if (owner == AccountSessionManager.GUEST || owner == deletingOwner) return
        val records = history.toList()
        val session = AccountSessionManager.generation
        scope.launch {
            saves.withLock {
                try {
                    val (token, storedOwner) = store.sessionFlow.first()
                    if (owner == deletingOwner || owner != storedOwner || token.isNullOrBlank() || session != AccountSessionManager.generation) return@withLock
                    val authorization = "Bearer $token"
                    val progress = api.getUserProgress(authorization)
                    if (owner == deletingOwner || session != AccountSessionManager.generation) return@withLock
                    // Save the already-awarded total once. Preserve the backend streak and other fields.
                    api.saveUserProgress(progress.copy(xp = totalXp, completedTasks = records.takeIf { it.isNotEmpty() }), authorization)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.w("ProgressSync", "Backend progress save failed; account-local XP and history are retained.")
                }
            }
        }
    }

    suspend fun deleteAccount(owner: String) {
        check(deletingOwner == null) { "Account deletion is already running" }
        deletingOwner = owner
        try {
            // Let an already-sent save finish before DELETE; queued/new saves are skipped.
            saves.withLock {
                check(owner == AccountSessionManager.accountKey)
                if (owner != AccountSessionManager.GUEST) {
                    val (token, storedOwner) = store.sessionFlow.first()
                    check(owner == storedOwner && !token.isNullOrBlank())
                    api.deleteAccount("Bearer $token")
                }
            }
        } catch (e: Exception) {
            deletingOwner = null
            throw e
        }
        // Stay blocked until the caller has cleared the session and local account data.
    }

    fun finishDeletion() {
        deletingOwner = null
    }
}
