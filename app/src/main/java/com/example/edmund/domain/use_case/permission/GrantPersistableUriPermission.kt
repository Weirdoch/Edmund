package com.example.edmund.domain.use_case.permission

import android.net.Uri
import com.example.edmund.domain.repository.PermissionRepository
import javax.inject.Inject

class GrantPersistableUriPermission @Inject constructor(
    private val repository: PermissionRepository
) {

    suspend fun execute(uri: Uri) {
        repository.grantPersistableUriPermission(uri)
    }
}