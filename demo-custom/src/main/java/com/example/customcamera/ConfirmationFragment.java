package com.example.customcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.commonsware.cwac.cam2.ImageContext;

public class ConfirmationFragment extends Fragment {
    private static final String ARG_NORMALIZE_ORIENTATION = "normalizeOrientation";

    private Float quality;

    private ImageView iv;
    private Button undo;
    private Button done;

    private ImageContext imageContext;
    private Listener listener;

    public interface Listener {
        void onPictureAccepted(ImageContext imageContext);

        void onRetakePicture();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public static ConfirmationFragment newInstance(boolean normalizeOrientation) {
        ConfirmationFragment result = new ConfirmationFragment();
        Bundle args = new Bundle();

        args.putBoolean(ARG_NORMALIZE_ORIENTATION, normalizeOrientation);
        result.setArguments(args);

        return (result);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iv = (ImageView) view.findViewById(R.id.image);
        undo = (Button) view.findViewById(R.id.undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onRetakePicture();
                }
            }
        });
        done = (Button) view.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPictureAccepted(imageContext);
            }
        });
        if (imageContext != null) {
            loadImage(quality);
        }
    }

    public void setImage(ImageContext imageContext, Float quality) {
        this.imageContext = imageContext;
        this.quality = quality;

        if (iv != null) {
            loadImage(quality);
        }
    }

    private void loadImage(Float quality) {
        iv.setImageBitmap(imageContext.buildPreviewThumbnail(getActivity(),
                                                             quality,
                                                             getArguments().getBoolean(ARG_NORMALIZE_ORIENTATION)));
    }
}
