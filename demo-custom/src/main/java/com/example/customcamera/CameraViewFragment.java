package com.example.customcamera;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraSelectionCriteria;
import com.commonsware.cwac.cam2.CameraView;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.FocusMode;
import com.commonsware.cwac.cam2.ImageContext;
import com.commonsware.cwac.cam2.PictureTransaction;

import java.util.Arrays;
import java.util.LinkedList;

import de.greenrobot.event.EventBus;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraViewFragment extends Fragment {
    private Listener listener;
    private CameraController cameraController;

    private CameraView cameraView;

    public interface Listener {
        void onPictureSuccess(ImageContext imageContext);

        void onPictureError(Throwable t);
    }

    public CameraViewFragment() {
        // Required empty public constructor
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        if (cameraController != null) {
            try {
                cameraController.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cameraController = new CameraController(FocusMode.CONTINUOUS, null, false, false);

        CameraEngine.ID engineId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? CameraEngine.ID.CAMERA2 :
                CameraEngine.ID.CLASSIC;
        CameraEngine engine = CameraEngine.buildInstance(getActivity(), engineId);
        engine.setPreferredFlashModes(Arrays.asList(FlashMode.AUTO, FlashMode.OFF));

        CameraSelectionCriteria criteria = new CameraSelectionCriteria.Builder().facing(Facing.BACK).build();

        cameraController.setEngine(engine, criteria);
        cameraController.setCurrentCamera(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraView = (CameraView) view.findViewById(R.id.container);

        if (cameraController != null && cameraController.getNumberOfCameras() > 0) {
            prepController();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (cameraController != null) {
            cameraController.start();
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        try {
            if (cameraController != null) {
                cameraController.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (cameraController != null) {
            cameraController.destroy();
        }
        super.onDestroy();
    }

    public void takePicture(Uri uri) {
        PictureTransaction.Builder b = new PictureTransaction.Builder();
        b.toUri(getActivity(), uri, false, false);
        cameraController.takePicture(b.build());
    }

    @SuppressWarnings ("unused")
    public void onEventMainThread(CameraEngine.PictureTakenEvent event) {
        if (listener != null && event != null) {
            if (event.exception == null) {
                listener.onPictureSuccess(event.getImageContext());
            } else {
                listener.onPictureError(event.exception);
            }
        }
    }

    @SuppressWarnings ("unused")
    public void onEventMainThread(CameraController.ControllerReadyEvent event) {
        if (event.isEventForController(cameraController)) {
            prepController();
        }
    }

    private void prepController() {
        cameraView.setMirror(false);
        LinkedList<CameraView> queue = new LinkedList<>();
        queue.add(cameraView);
        cameraController.setCameraViews(queue);
        cameraController.setQuality(1);
    }
}
