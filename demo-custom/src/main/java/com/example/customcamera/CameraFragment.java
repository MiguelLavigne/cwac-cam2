package com.example.customcamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commonsware.cwac.cam2.ImageContext;

import java.util.ArrayList;

public class CameraFragment extends Fragment implements ConfirmationFragment.Listener, CameraViewFragment.Listener {
    private static final String[] PERMS = {Manifest.permission.CAMERA};
    private static final int REQUEST_PERMS = 1;

    private Listener listener;
    private Uri outputUri;

    private ConfirmationFragment confirmationFragment;
    private CameraViewFragment cameraViewFragment;

    public interface Listener {
        void onPictureStarted();

        void onPictureRetake();

        void onPictureSuccess(Uri uri);

        void onPictureError(Throwable t);

        void onPermissionMissing();
    }

    public CameraFragment() {
        // Required empty public constructor
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void takePicture(Uri output) {
        outputUri = output;
        if (listener != null) {
            listener.onPictureStarted();
        }
        cameraViewFragment.takePicture(output);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (useRuntimePermissions()) {
            String[] perms = netPermissions(PERMS);

            if (perms.length == 0) {
                init();
            } else {
                requestPermissions(perms, REQUEST_PERMS);
            }
        } else {
            init();
        }
    }

    @TargetApi (23)
    private boolean hasPermission(String perm) {
        if (useRuntimePermissions()) {
            return (getActivity().checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }

    private boolean useRuntimePermissions() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    private String[] netPermissions(String[] wanted) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private void init() {
        cameraViewFragment = new CameraViewFragment();
        confirmationFragment = ConfirmationFragment.newInstance(true);

        getChildFragmentManager().beginTransaction().add(R.id.container, cameraViewFragment).commit();
        getChildFragmentManager().beginTransaction().add(R.id.container, confirmationFragment).commit();

        showCameraView();
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof ConfirmationFragment) {
            ((ConfirmationFragment) childFragment).setListener(this);
        }
        if (childFragment instanceof CameraViewFragment) {
            ((CameraViewFragment) childFragment).setListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String[] perms = netPermissions(PERMS);

        if (perms.length == 0) {
            init();
        } else {
            if (listener != null) {
                listener.onPermissionMissing();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onPictureSuccess(ImageContext imageContext) {
        showConfirmation(imageContext);
    }

    @Override
    public void onPictureError(Throwable t) {
        if (listener != null) {
            listener.onPictureError(t);
        }
    }

    @Override
    public void onPictureAccepted(ImageContext imageContext) {
        if (listener != null) {
            listener.onPictureSuccess(outputUri);
        }
    }

    @Override
    public void onRetakePicture() {
        showCameraView();
        if (listener != null) {
            listener.onPictureRetake();
        }
    }

    private void showConfirmation(ImageContext imageContext) {
        confirmationFragment.setImage(imageContext, 1f);
        getChildFragmentManager().beginTransaction().hide(cameraViewFragment).show(confirmationFragment).commit();
    }

    private void showCameraView() {
        getChildFragmentManager().beginTransaction().hide(confirmationFragment).show(cameraViewFragment).commit();
    }
}
