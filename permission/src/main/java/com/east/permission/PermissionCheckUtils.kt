package com.east.permission

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
    fun checkPermission(activity: FragmentActivity,permissions:Array<String>,listener : PermissionListener) {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.requestEachCombined(*permissions).subscribe {
            when {
                it.granted -> {
                    listener.onGranted()
                }
                it.shouldShowRequestPermissionRationale -> {
                    checkPermission(activity,permissions,listener)
                }
                else -> {
                    showMissingPermissionDialog(activity,listener)
                }
            }
        }
    }

    fun checkPermission(fragment: Fragment, permissions:Array<String>,listener : PermissionListener) {
        val rxPermissions = RxPermissions(fragment)
        rxPermissions.requestEachCombined(*permissions).subscribe {
            when {
                it.granted -> {
                    listener.onGranted()
                }
                it.shouldShowRequestPermissionRationale -> {
                    checkPermission(fragment,permissions,listener)
                }
                else -> {
                    showMissingPermissionDialog(fragment.context!!,listener)
                }
            }
        }
    }


    fun showMissingPermissionDialog(context: Context,listener: PermissionListener) {
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
}