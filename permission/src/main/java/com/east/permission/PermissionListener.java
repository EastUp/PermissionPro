package com.east.permission;

/**
 * |---------------------------------------------------------------------------------------------------------------|
 *
 *  @description: 权限检查回调
 *  @author: jamin
 *  @date: 2020/4/10
 * |---------------------------------------------------------------------------------------------------------------|
 */
public interface PermissionListener {
    /**
     *  权限同意了
     */
    default void onGranted(){}

    /**
     *  权限拒绝了
     */
    default void  onCancel(){}
}
