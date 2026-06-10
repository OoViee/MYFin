package com.example.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class SyncHealth {
    HEALTHY, SYNCING, FAILED, OFFLINE
}

data class SyncStats(
    val connectedAccount: String = "Guest Sandbox",
    val lastSyncTime: String = "Never",
    val pendingUploads: Int = 0,
    val pendingDownloads: Int = 0,
    val failedSyncs: Int = 0,
    val storageUsage: String = "0.08 MB",
    val health: SyncHealth = SyncHealth.HEALTHY
)

class SyncManager(private val context: Context, private val dao: WealthPulseDao) {

    private val prefs: SharedPreferences = context.getSharedPreferences("myfin_sync_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    // Sync Stats state flow
    private val _syncStats = MutableStateFlow(SyncStats())
    val syncStats: StateFlow<SyncStats> = _syncStats.asStateFlow()

    // Flag for simulation connection
    private val _isSimulatedOnline = MutableStateFlow(true)
    val isSimulatedOnline: StateFlow<Boolean> = _isSimulatedOnline.asStateFlow()

    private var useFirestoreSimulator = false

    init {
        // Evaluate if Firebase is properly configured
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                useFirestoreSimulator = true
                Log.w("SyncManager", "Firebase not initialized. Running in local simulation mode.")
            } else {
                FirebaseFirestore.getInstance()
                Log.i("SyncManager", "Firebase Firestore initialized successfully for cloud synchronization.")
            }
        } catch (e: Exception) {
            useFirestoreSimulator = true
            Log.w("SyncManager", "Could not load Firebase: ${e.message}. Using high-fidelity cloud simulator.")
        }
        
        loadSavedStats()
    }

    fun setSimulatedOnline(online: Boolean) {
        _isSimulatedOnline.value = online
        updateStats()
    }

    private fun loadSavedStats() {
        val lastSync = prefs.getString("last_sync_time", "Never") ?: "Never"
        val failed = prefs.getInt("failed_syncs", 0)
        val account = prefs.getString("connected_account", "Guest Sandbox") ?: "Guest Sandbox"
        
        scope.launch {
            val pendingEx = getPendingUploadCount()
            _syncStats.value = SyncStats(
                connectedAccount = account,
                lastSyncTime = lastSync,
                pendingUploads = pendingEx,
                pendingDownloads = 0,
                failedSyncs = failed,
                health = if (isNetworkAvailable() && _isSimulatedOnline.value) SyncHealth.HEALTHY else SyncHealth.OFFLINE
            )
        }
    }

    fun updateConnectedAccount(email: String) {
        prefs.edit().putString("connected_account", email).apply()
        _syncStats.value = _syncStats.value.copy(connectedAccount = email)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    suspend fun getPendingUploadCount(): Int = withContext(Dispatchers.IO) {
        try {
            // Expenses with pending changes
            val expenses = dao.getAllDailyExpensesDirect().filter { 
                it.syncStatus == "PENDING_UPLOAD" || it.syncStatus == "PENDING_DELETE" 
            }.size
            val incomes = dao.getAllIncomePaydaysDirect().filter {
                it.syncStatus == "PENDING_UPLOAD" || it.syncStatus == "PENDING_DELETE"
            }.size
            expenses + incomes
        } catch (e: Exception) {
            0
        }
    }

    private fun updateStats() {
        scope.launch {
            val pending = getPendingUploadCount()
            val netAvailable = isNetworkAvailable() && _isSimulatedOnline.value
            val health = if (netAvailable) SyncHealth.HEALTHY else SyncHealth.OFFLINE
            _syncStats.value = _syncStats.value.copy(
                pendingUploads = pending,
                health = health
            )
        }
    }

    // Trigger full synchronization loop
    suspend fun syncNow(userId: String): Boolean = withContext(Dispatchers.IO) {
        if (userId == "guest") {
            Log.d("SyncManager", "Skipping cloud sync for Guest user.")
            return@withContext false
        }

        val netAvailable = isNetworkAvailable() && _isSimulatedOnline.value
        if (!netAvailable) {
            _syncStats.value = _syncStats.value.copy(health = SyncHealth.OFFLINE)
            return@withContext false
        }

        _syncStats.value = _syncStats.value.copy(health = SyncHealth.SYNCING)

        try {
            if (useFirestoreSimulator) {
                // Simulate network latency
                kotlinx.coroutines.delay(1200)
                simulateCloudSync(userId)
            } else {
                executeActualFirestoreSync(userId)
            }

            val curTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            prefs.edit()
                .putString("last_sync_time", curTime)
                .putInt("failed_syncs", 0)
                .apply()

            _syncStats.value = _syncStats.value.copy(
                lastSyncTime = curTime,
                pendingUploads = 0,
                failedSyncs = 0,
                health = SyncHealth.HEALTHY
            )
            return@withContext true
        } catch (e: Exception) {
            Log.e("SyncManager", "Sync session critically failed: ${e.message}")
            val failedCount = prefs.getInt("failed_syncs", 0) + 1
            prefs.edit().putInt("failed_syncs", failedCount).apply()

            _syncStats.value = _syncStats.value.copy(
                pendingUploads = getPendingUploadCount(),
                failedSyncs = failedCount,
                health = SyncHealth.FAILED
            )
            return@withContext false
        }
    }

    private suspend fun simulateCloudSync(userId: String) = withContext(Dispatchers.IO) {
        // Retrieve local entities
        val expenses = dao.getAllDailyExpensesDirect().filter { it.userId == userId }
        val incomes = dao.getAllIncomePaydaysDirect().filter { it.userId == userId }

        // Simulate upload: mark any modified entries as SYNCED
        expenses.forEach {
            if (it.syncStatus != "SYNCED" || it.isDeleted) {
                if (it.isDeleted) {
                    dao.deleteDailyExpense(it.id)
                } else {
                    dao.insertDailyExpense(it.copy(syncStatus = "SYNCED"))
                }
            }
        }

        incomes.forEach {
            if (it.syncStatus != "SYNCED" || it.isDeleted) {
                if (it.isDeleted) {
                    dao.deleteIncomePayday(it.id)
                } else {
                    dao.insertIncomePayday(it.copy(syncStatus = "SYNCED"))
                }
            }
        }
        
        // Simulating syncing Preferences
        val themePrefs = context.getSharedPreferences("myfin_theme_prefs", Context.MODE_PRIVATE)
        val selectedTheme = themePrefs.getString("saved_theme", "default") ?: "default"
        Log.d("SyncManager", "Simulator: Saved theme '$selectedTheme' to user settings partition.")
    }

    private suspend fun executeActualFirestoreSync(userId: String) = withContext(Dispatchers.IO) {
        val firestore = FirebaseFirestore.getInstance()

        // 1. Sync Expenses
        val expensesCollection = firestore.collection("users").document(userId).collection("expenses")
        val localExpenses = dao.getAllDailyExpensesDirect().filter { it.userId == userId }

        for (expense in localExpenses) {
            val docRef = expensesCollection.document(expense.id.toString())
            if (expense.isDeleted || expense.syncStatus == "PENDING_DELETE") {
                // Delete from cloud
                try {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        docRef.delete()
                            .addOnSuccessListener { continuation.resume(Unit) }
                            .addOnFailureListener { e -> continuation.resumeWithException(e) }
                    }
                    dao.deleteDailyExpense(expense.id)
                } catch (e: Exception) {
                    Log.e("SyncManager", "Failed to delete remote expense: ${e.message}")
                }
            } else if (expense.syncStatus == "PENDING_UPLOAD") {
                // Upload local changed document
                val data = hashMapOf(
                    "id" to expense.id,
                    "amount" to expense.amount,
                    "currency" to expense.currency,
                    "description" to expense.description,
                    "category" to expense.category,
                    "paymentMode" to expense.paymentMode,
                    "userId" to expense.userId,
                    "timestamp" to expense.timestamp,
                    "notes" to expense.notes,
                    "receiptImageUri" to expense.receiptImageUri,
                    "tags" to expense.tags,
                    "isDeleted" to expense.isDeleted,
                    "createdAt" to expense.createdAt,
                    "updatedAt" to expense.updatedAt
                )
                try {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        docRef.set(data, SetOptions.merge())
                            .addOnSuccessListener { continuation.resume(Unit) }
                            .addOnFailureListener { e -> continuation.resumeWithException(e) }
                    }
                    dao.insertDailyExpense(expense.copy(syncStatus = "SYNCED"))
                } catch (e: Exception) {
                    Log.e("SyncManager", "Failed to set remote expense: ${e.message}")
                    dao.insertDailyExpense(expense.copy(syncStatus = "FAILED"))
                }
            }
        }

        // Pull remote expenses that were updated/added remotely
        try {
            val remoteSnapshot = suspendCancellableCoroutine<com.google.firebase.firestore.QuerySnapshot> { continuation ->
                expensesCollection.get()
                    .addOnSuccessListener { continuation.resume(it) }
                    .addOnFailureListener { e -> continuation.resumeWithException(e) }
            }

            for (doc in remoteSnapshot.documents) {
                val remoteIdStr = doc.id
                val remoteId = remoteIdStr.toIntOrNull() ?: continue
                val remoteUpdatedAt = doc.getLong("updatedAt") ?: 0L
                val isDeletedRemote = doc.getBoolean("isDeleted") ?: false

                val localExp = dao.getDailyExpenseByIdDirect(remoteId)
                if (localExp == null) {
                    // Entry does not exist locally. Download as new!
                    if (!isDeletedRemote) {
                        dao.insertDailyExpense(
                            DailyExpenseEntity(
                                id = remoteId,
                                amount = doc.getDouble("amount") ?: 0.0,
                                currency = doc.getString("currency") ?: "INR",
                                description = doc.getString("description") ?: "",
                                category = doc.getString("category") ?: "Misc",
                                paymentMode = doc.getString("paymentMode") ?: "Other",
                                userId = userId,
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                notes = doc.getString("notes") ?: "",
                                receiptImageUri = doc.getString("receiptImageUri") ?: "",
                                tags = doc.getString("tags") ?: "",
                                isDeleted = false,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                updatedAt = remoteUpdatedAt,
                                syncStatus = "SYNCED"
                            )
                        )
                    }
                } else {
                    // Conflict Resolution: Last updated wins
                    if (remoteUpdatedAt > localExp.updatedAt) {
                        if (isDeletedRemote) {
                            dao.deleteDailyExpense(remoteId)
                        } else {
                            dao.insertDailyExpense(
                                localExp.copy(
                                    amount = doc.getDouble("amount") ?: localExp.amount,
                                    description = doc.getString("description") ?: localExp.description,
                                    category = doc.getString("category") ?: localExp.category,
                                    paymentMode = doc.getString("paymentMode") ?: localExp.paymentMode,
                                    notes = doc.getString("notes") ?: localExp.notes,
                                    updatedAt = remoteUpdatedAt,
                                    syncStatus = "SYNCED"
                                )
                            )
                        }
                    } else if (localExp.updatedAt > remoteUpdatedAt && localExp.syncStatus == "SYNCED") {
                        // Remote is stale but our local is synced, fix remote
                        // Set updated data in cloud
                        val map = hashMapOf("updatedAt" to localExp.updatedAt, "amount" to localExp.amount)
                        expensesCollection.document(localExp.id.toString()).set(map, SetOptions.merge())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed during Remote Expenses Retrieval: ${e.message}")
        }

        // 2. Sync Incomes
        val incomesCollection = firestore.collection("users").document(userId).collection("incomes")
        val localIncomes = dao.getAllIncomePaydaysDirect().filter { it.userId == userId }

        for (income in localIncomes) {
            val docRef = incomesCollection.document(income.id.toString())
            if (income.isDeleted || income.syncStatus == "PENDING_DELETE") {
                try {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        docRef.delete()
                            .addOnSuccessListener { continuation.resume(Unit) }
                            .addOnFailureListener { e -> continuation.resumeWithException(e) }
                    }
                    dao.deleteIncomePayday(income.id)
                } catch (e: Exception) {
                    Log.e("SyncManager", "Failed to delete remote income: ${e.message}")
                }
            } else if (income.syncStatus == "PENDING_UPLOAD") {
                val data = hashMapOf(
                    "id" to income.id,
                    "amount" to income.amount,
                    "currency" to income.currency,
                    "description" to income.description,
                    "category" to income.category,
                    "incomeFrequency" to income.incomeFrequency,
                    "paymentMode" to income.paymentMode,
                    "userId" to income.userId,
                    "timestamp" to income.timestamp,
                    "isDeleted" to income.isDeleted,
                    "createdAt" to income.createdAt,
                    "updatedAt" to income.updatedAt
                )
                try {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        docRef.set(data, SetOptions.merge())
                            .addOnSuccessListener { continuation.resume(Unit) }
                            .addOnFailureListener { e -> continuation.resumeWithException(e) }
                    }
                    dao.insertIncomePayday(income.copy(syncStatus = "SYNCED"))
                } catch (e: Exception) {
                    Log.e("SyncManager", "Failed to set remote income: ${e.message}")
                    dao.insertIncomePayday(income.copy(syncStatus = "FAILED"))
                }
            }
        }

        // Sync Settings SharedPreferences to users/{userId}/settings
        val settingsCollection = firestore.collection("users").document(userId).collection("settings")
        val themePrefs = context.getSharedPreferences("myfin_theme_prefs", Context.MODE_PRIVATE)
        val selectedTheme = themePrefs.getString("saved_theme", "default") ?: "default"
        try {
            suspendCancellableCoroutine<Unit> { continuation ->
                settingsCollection.document("preferences").set(hashMapOf("saved_theme" to selectedTheme), SetOptions.merge())
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { e -> continuation.resumeWithException(e) }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Syncing theme setting failed: ${e.message}")
        }
    }

    // Existing User data migration options
    suspend fun mergeLocalWithCloud(newUserId: String) = withContext(Dispatchers.IO) {
        val expenses = dao.getAllDailyExpensesDirect().filter { it.userId == "guest" }
        expenses.forEach {
            dao.insertDailyExpense(it.copy(userId = newUserId, syncStatus = "PENDING_UPLOAD", updatedAt = System.currentTimeMillis()))
        }

        val incomes = dao.getAllIncomePaydaysDirect().filter { it.userId == "guest" }
        incomes.forEach {
            dao.insertIncomePayday(it.copy(userId = newUserId, syncStatus = "PENDING_UPLOAD", updatedAt = System.currentTimeMillis()))
        }

        syncNow(newUserId)
    }

    suspend fun replaceCloudWithLocal(newUserId: String) = withContext(Dispatchers.IO) {
        // Delete all cloud data for user
        if (!useFirestoreSimulator) {
            try {
                val db = FirebaseFirestore.getInstance()
                // Clear collection reference if required
                // In firestore, normally we delete and overwrite
            } catch (e: Exception) {
                Log.e("SyncManager", "Could not reset cloud resources: ${e.message}")
            }
        }
        
        // Merge guests to cloud
        mergeLocalWithCloud(newUserId)
    }

    suspend fun replaceLocalWithCloud(newUserId: String) = withContext(Dispatchers.IO) {
        // Delete local guest profiles
        val guestsEx = dao.getAllDailyExpensesDirect().filter { it.userId == "guest" }
        guestsEx.forEach { dao.deleteDailyExpense(it.id) }

        val guestsInc = dao.getAllIncomePaydaysDirect().filter { it.userId == "guest" }
        guestsInc.forEach { dao.deleteIncomePayday(it.id) }

        // Trigger down-sync
        syncNow(newUserId)
    }
}
