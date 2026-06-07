package com.kalindu.pocketfit.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkMonitor(context: Context) {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    val connectivity: Flow<ConnectivityStatus> = callbackFlow {
        fun sendStatus(isOnline: Boolean) {
            trySend(if (isOnline) ConnectivityStatus.ONLINE else ConnectivityStatus.OFFLINE)
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                sendStatus(capabilities.hasValidatedInternet())
            }

            override fun onLost(network: Network) {
                sendStatus(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                sendStatus(networkCapabilities.hasValidatedInternet())
            }

            override fun onUnavailable() {
                sendStatus(false)
            }
        }

        val initialCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        sendStatus(initialCapabilities.hasValidatedInternet())
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun NetworkCapabilities?.hasValidatedInternet(): Boolean =
        this?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

enum class ConnectivityStatus {
    CHECKING,
    ONLINE,
    OFFLINE
}
