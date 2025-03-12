package com.example.edmund.ui.fragment

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.edmund.data.di.RepositoryModule
import com.example.edmund.domain.repository.PermissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

//@HiltViewModel
//class BrowseViewModel @Inject constructor(
//    private val repository: PermissionRepository
//) : ViewModel() {
//
//    suspend fun grantPersistableUriPermission(uri: Uri) {
//        repository.grantPersistableUriPermission(uri)
//    }
//
//}