package com.east.permission

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.east.permission.rxpermissions.RxPermissions

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *  @description:   权限检测工具
 *  @author: East
 *  @date: 2019-12-04
 * |---------------------------------------------------------------------------------------------------------------|
 */
object PermissionCheckUtils {
    fun checkPermission(
        activity: FragmentActivity,
        permissions: Array<String>,
        listener: PermissionListener
    ) {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.requestEachCombined(*permissions).subscribe {
            when {
                it.granted -> {
                    listener.onGranted()
                }
                it.shouldShowRequestPermissionRationale -> {
                    checkPermission(activity, permissions, listener)
                }
                else -> {
                    showMissingPermissionDialog(activity, listener)
                }
            }
        }
    }

    fun checkPermission(
        fragment: Fragment,
        permissions: Array<String>,
        listener: PermissionListener
    ) {
        val rxPermissions = RxPermissions(fragment)
        rxPermissions.requestEachCombined(*permissions).subscribe {
            when {
                it.granted -> {
                    listener.onGranted()
                }
                it.shouldShowRequestPermissionRationale -> {
                    checkPermission(fragment, permissions, listener)
                }
                else -> {
                    showMissingPermissionDialog(fragment.context!!, listener)
                }
            }
        }
    }

    fun checkeHasPermission(context: Context, permission: String): Boolean {
        val pm = context.packageManager
        return PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permission, context.packageName)
    }

    fun checkeHasPermission(context: Context, permissions: Array<String>): Boolean {
        val pm = context.packageManager
        var grantedPermissions = true
        for (permission in permissions) {
            if (PackageManager.PERMISSION_GRANTED != pm.checkPermission(
                    permission,
                    context.packageName
                )
            ) {
                grantedPermissions = false
                break
            }
        }
        return grantedPermissions
    }


    fun showMissingPermissionDialog(context: Context, listener: PermissionListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("提示")
        builder.setMessage("当前应用缺少必要权限。请点击\"设置\"-\"权限\"-打开所需权限。")

        // 拒绝, 退出应用
        builder.setNegativeButton(
            "取消"
        ) { _, _ -> listener.onCancel() }

        builder.setPositiveButton(
            "设置"
        ) { _, _ -> startAppSettings(context) }

        builder.setCancelable(false)

        builder.show()
    }

    /**
     * 启动应用的设置
     */
    fun startAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        )
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
//        finish()
    }

    /**
     * 是否开启位置服务
     */
    open fun isLocationServiceEnabled(context: Context): Boolean {
        val locationProviders: String
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            var locationMode = try {
                Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                return false
            }
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            locationProviders = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
            !TextUtils.isEmpty(locationProviders)
        }
    }

    /**
     *  提示让用户打开位置服务设置
     */
    fun showMissingLocationServiceDialog(context: Context, listener: PermissionListener) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("提示")
        builder.setMessage("手机未打开位置服务。请进入设置打开")

        // 拒绝, 退出应用
        builder.setNegativeButton(
            "取消"
        ) { _, _ -> listener.onCancel() }

        builder.setPositiveButton(
            "设置"
        ) { _, _ -> startAppLocationSettings(context) }

        builder.setCancelable(false)

        builder.show()
    }

    /**
     *  打开位置服务设置界面
     */
    fun startAppLocationSettings(context: Context) {
        val intent = Intent()
        //定位服务页面
        intent.action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            //如果页面无法打开，进入设置页面
            intent.action = Settings.ACTION_SETTINGS
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}