package com.simbirsoft.timemeter.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.simbirsoft.timemeter.ui.settings.SettingsFragment;

import javax.annotation.Nonnull;

public class PermissionUtils {

    public enum PERMISSION {
        WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE
    }

    public interface OnPermissionResultListener {
        void onAlreadyAccess(PERMISSION type);
        void onAllowAccess(PERMISSION type);
        void onDenyAccess(PERMISSION type);
    }

    public final static int REQUEST_CODE_PERMISSION = 77;

    private final SettingsFragment fragment;

    public PermissionUtils(SettingsFragment fragment) {
        this.fragment = fragment;
    }

    public void execute(@Nonnull PERMISSION type) {
        if (isVersionM()) {
            final boolean isAlreadyAccess = isAlreadyAccess(type);
            if (isAlreadyAccess) {
                fragment.onAlreadyAccess(type);
            }
            else {
                final String[] permissions = getPermissionArray(type);
                fragment.requestPermissions(permissions, REQUEST_CODE_PERMISSION);
            }
        }
        else {
            fragment.onAlreadyAccess(type);
        }
    }

    public void result(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (permissions.length > 0 && grantResults.length > 0 && permissions.length == grantResults.length) {
                final PERMISSION type = getPermissionType(permissions[0]);
                final int grant = grantResults[0];
                if (grant == PackageManager.PERMISSION_GRANTED) {
                    fragment.onAllowAccess(type);
                }
                else {
                    fragment.onDenyAccess(type);
                }
            }
        }
    }

    private boolean isAlreadyAccess(PERMISSION type) {
        final String permission = getPermission(type);
        final int access = ContextCompat.checkSelfPermission(fragment.getActivity(), permission);
        return access == PackageManager.PERMISSION_GRANTED;
    }

    private String[] getPermissionArray(PERMISSION type) {
        final String result = getPermission(type);
        return new String[] {result};
    }

    private String getPermission(PERMISSION type) {
        switch (type) {
            case WRITE_EXTERNAL_STORAGE:
                return Manifest.permission.WRITE_EXTERNAL_STORAGE;

            case READ_EXTERNAL_STORAGE:
                // Поле добавлено в 16 версии(минимальная у проекта - 15), но этот метод будет вызван только для устройств с 23 версии и выше
                return Manifest.permission.READ_EXTERNAL_STORAGE;

            default:
                return null;
        }
    }

    private PERMISSION getPermissionType(String permission) {
        switch (permission) {
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return PERMISSION.WRITE_EXTERNAL_STORAGE;

            case Manifest.permission.READ_EXTERNAL_STORAGE:
                return PERMISSION.READ_EXTERNAL_STORAGE;

            default:
                return null;
        }
    }

    private boolean isVersionM() {
        final int version = Build.VERSION.SDK_INT;
        return version >= 23;
    }
}