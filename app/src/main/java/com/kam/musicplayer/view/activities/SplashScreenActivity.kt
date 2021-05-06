package com.kam.musicplayer.view.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.kam.musicplayer.R
import com.kam.musicplayer.services.MusicPlayerService
import com.kam.musicplayer.utils.Constants
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val permissionsRequired = getPermissionData()

        if (permissionsRequired.isEmpty()) {
            beginApp()
        } else {
            getPermissions(*permissionsRequired.toTypedArray())
        }
    }

    private fun getPermissions(vararg permissions: String) {
        Dexter.withContext(this)
            .withPermissions(*permissions)
            .withListener(object: MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.areAllPermissionsGranted()) {
                    beginApp()
                } else {
                    showPermissionRationale()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                report: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showPermissionRationale()
            }

        }).onSameThread()
            .check()
    }

    private fun getPermissionData() : List<String> {
        val output: MutableList<String> = mutableListOf()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            output.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            output.add(Manifest.permission.READ_PHONE_STATE)
        }

        return output
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setMessage(Constants.PERMISSION_RATIONALE)
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun beginApp() {
        // startForegroundService(Intent(this, MusicPlayerService::class.java))
        // This isn't required as this will be called automagically once something requests the service
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}