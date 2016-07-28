package com.example.customcamera;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements CameraFragment.Listener {
    private CameraFragment cameraFragment;
    private Button takePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            cameraFragment = new CameraFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.camera_preview_container, cameraFragment).commit();
        }

        takePicture = (Button) findViewById(R.id.take_picture);
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraFragment.takePicture(Uri.fromFile(new File(getPicturesDirectory(), "tmp.png")));
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                cameraFragment = new CameraFragment();
                getSupportFragmentManager().beginTransaction()
                                           .add(R.id.camera_preview_container, cameraFragment)
                                           .commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof CameraFragment) {
            ((CameraFragment) fragment).setListener(this);
        }
    }

    public File getPicturesDirectory() {
        File folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (folder != null && !folder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            folder.mkdirs();
        }

        return folder;
    }

    @Override
    public void onPictureStarted() {
        takePicture.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPictureRetake() {
        takePicture.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPictureSuccess(Uri uri) {
        takePicture.setVisibility(View.VISIBLE);
        Toast.makeText(this, "photo " + uri, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPictureError(Throwable t) {
        takePicture.setVisibility(View.VISIBLE);
        Toast.makeText(this, "error taking picture " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionMissing() {
        Toast.makeText(this, "permission failed", Toast.LENGTH_SHORT).show();
    }
}
