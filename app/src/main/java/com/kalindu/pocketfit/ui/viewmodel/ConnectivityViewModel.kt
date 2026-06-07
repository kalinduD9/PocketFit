package com.kalindu.pocketfit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kalindu.pocketfit.utils.ConnectivityStatus
import com.kalindu.pocketfit.utils.NetworkMonitor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ConnectivityViewModel(application: Application) : AndroidViewModel(application) {
    val connectivity: StateFlow<ConnectivityStatus> =
        NetworkMonitor(application.applicationContext).connectivity.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ConnectivityStatus.CHECKING
        )
}
