package com.reactnativeaudiowaveform.permission

import java.lang.ref.Reference
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener
import com.reactnativeaudiowaveform.permission.callback.*
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.Arrays

class RuntimePermission(activity: Activity?, val listener: PermissionListener) {
  private val activityReference: Reference<Activity>
  private val permissionsToRequest = ArrayList<String>()

  private var isRationale = false
  private val acceptedCallbacks = ArrayList<(AcceptedCallback) -> Unit>()
  private val deniedCallbacks = ArrayList<(DeniedCallback) -> Unit>()
  private val foreverDeniedCallbacks = ArrayList<(ForeverDeniedCallback) -> Unit>()
  private val responseCallbacks = ArrayList<(PermissionCallback) -> Unit>()
  private val listenerCallbacks = ArrayList<(ListenerCallback) -> Unit>()

  init {
    if (activity != null) {
      this.activityReference = WeakReference(activity)
    } else {
      this.activityReference = WeakReference(null)
    }
  }

  fun goToSettings() {
    val fragmentActivity = this.activityReference.get()
    fragmentActivity?.startActivity(
      Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", fragmentActivity.packageName, null)
      )
    )
  }

  private fun onReceivedPermissionResult(
    acceptedPermissions: List<String>,
    refusedPermissions: List<String>?,
    askAgainPermissions: List<String>?
  ) {
    val permissionResult = PermissionCallback(
      this,
      acceptedPermissions,
      refusedPermissions,
      askAgainPermissions
    )

    if (permissionResult.hasAccepted()) {
      for (callback in acceptedCallbacks) {
        callback.invoke(AcceptedCallback(permissionResult.accepted))
      }
    }

    if (permissionResult.hasDenied() && !isRationale) {
      for (callback in deniedCallbacks) {
        callback.invoke(DeniedCallback(this, permissionResult.denied))
      }
    }

    if (permissionResult.hasForeverDenied()) {
      for (callback in foreverDeniedCallbacks) {
        callback.invoke(ForeverDeniedCallback(this, permissionResult.foreverDenied))
      }
    }

    if (permissionResult.hasDenied() || permissionResult.hasForeverDenied()) {
      for (callback in listenerCallbacks) {
        if (!permissionResult.hasDenied()){
          callback.invoke(ListenerCallback(this, permissionResult.foreverDenied, permissionResult.denied))
        } else if(!isRationale) {
          callback.invoke(ListenerCallback(this, permissionResult.foreverDenied, permissionResult.denied))
        }
      }
    }

    for (callback in responseCallbacks) {
      if (!permissionResult.hasDenied()){
        callback.invoke(permissionResult)
      } else if(!isRationale) {
        callback.invoke(permissionResult)
      }
    }
  }

  fun request(permissions: List<String>?): RuntimePermission {
    if (permissions != null) {
      permissionsToRequest.clear()
      permissionsToRequest.addAll(permissions)
    }
    return this
  }

  fun request(vararg permissions: String): RuntimePermission {
    return this.request(Arrays.asList(*permissions))
  }

  fun onAccepted(accepted: (AcceptedCallback) -> Unit): RuntimePermission {
    acceptedCallbacks.add(accepted)
    return this
  }

  fun onDenied(denied: (DeniedCallback) -> Unit): RuntimePermission {
    deniedCallbacks.add(denied)
    return this
  }

  fun onForeverDenied(foreverDenied: (ForeverDeniedCallback) -> Unit): RuntimePermission {
    foreverDeniedCallbacks.add(foreverDenied)
    return this
  }

  fun onResponse(callback: (PermissionCallback) -> Unit): RuntimePermission {
    responseCallbacks.add(callback)
    return this
  }

  fun onListener(accepted: (AcceptedCallback) -> Unit, denied: (ListenerCallback) -> Unit): RuntimePermission {
    acceptedCallbacks.add(accepted)
    listenerCallbacks.add(denied)
    return this
  }

  private fun findNeededPermissions(context: Context): List<String> {
    return if (permissionsToRequest.isEmpty()) {
      PermissionManifestFinder.findNeededPermissionsFromManifest(context)
    } else {
      permissionsToRequest
    }
  }

  fun ask() {
    val activity = activityReference.get()
    if (activity == null || activity.isFinishing) {
      return
    }

    val permissions = findNeededPermissions(activity)
    if (permissions.isEmpty() || Build.VERSION.SDK_INT < Build.VERSION_CODES.M || arePermissionsAlreadyAccepted(activity, permissions)) {
      onAllAccepted(permissions)
    } else {
      isRationale = arePermissionsAlreadyRationale(activity, permissions)
      val permissionAwareActivity: PermissionAwareActivity = getPermissionAwareActivity(activity)
      permissionAwareActivity.requestPermissions(permissions.toTypedArray(), 5000, listener)
    }
  }

  fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    val activity = activityReference.get()
    if (activity == null || activity.isFinishing) {
      return
    }
    val acceptedPermissions = ArrayList<String>()
    val askAgainPermissions = ArrayList<String>()
    val refusedPermissions = ArrayList<String>()

    for (i in permissions.indices) {
      val permissionName = permissions[i]
      if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
        acceptedPermissions.add(permissionName)
      } else {
        if (activity.shouldShowRequestPermissionRationale(permissionName)) {
          askAgainPermissions.add(permissionName)
        } else {
          refusedPermissions.add(permissionName)
        }
      }
    }
    onReceivedPermissionResult(acceptedPermissions, refusedPermissions, askAgainPermissions)
  }

  private fun arePermissionsAlreadyAccepted(context: Context, permissions: List<String>): Boolean {
    for (permission in permissions) {
      val permissionState = ContextCompat.checkSelfPermission(context, permission)
      if (permissionState == PackageManager.PERMISSION_DENIED) {
        return false
      }
    }
    return true
  }

  private fun arePermissionsAlreadyRationale(context: Context, permissions: List<String>): Boolean {
    for (permission in permissions) {
      if (!ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
        return false
      }
    }
    return true
  }

  private fun onAllAccepted(permissions: List<String>) {
    onReceivedPermissionResult(permissions, null, null)
  }

  private fun getPermissionAwareActivity(activity: Activity?): PermissionAwareActivity {
    checkNotNull(activity) { "Tried to use permissions API while not attached to an Activity." }
    check(activity is PermissionAwareActivity) {
      "Tried to use permissions API but the host Activity doesn't implement PermissionAwareActivity."
    }
    return activity
  }

  companion object {
    private const val TAG = "PERMISSION_FRAGMENT"

    fun askPermission(activity: Activity?, listener: PermissionListener, vararg permissions: String): RuntimePermission {
      return RuntimePermission(activity, listener).request(*permissions)
    }
  }
}
