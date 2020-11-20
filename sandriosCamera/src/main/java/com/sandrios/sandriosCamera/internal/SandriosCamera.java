package com.sandrios.sandriosCamera.internal;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.sandrios.sandriosCamera.internal.ui.camera.Camera1Activity;
import com.sandrios.sandriosCamera.internal.ui.camera2.Camera2Activity;
import com.sandrios.sandriosCamera.internal.ui.view.CameraSwitchView;
import com.sandrios.sandriosCamera.internal.utils.CameraHelper;

/**
 * Sandrios Camera Builder Class
 * Created by Arpit Gandhi on 7/6/16.
 */
public class SandriosCamera {

    public static int RESULT_CODE = 956;
    public static String MEDIA = "media";
    private static SandriosCamera mInstance = null;
    private int mediaAction = CameraConfiguration.MEDIA_ACTION_BOTH;
    private boolean showPicker = true;
    private boolean enablePreview = true;
    private int cameraType = CameraSwitchView.CAMERA_TYPE_REAR;
    private boolean autoRecord = false;
    private boolean enableImageCrop = false;
    private long videoSize = -1;
    private CameraSwitchView.OnCameraTypeChangeListener cameraTypeChangeListener;
    private String rootMediaPath = "";
    private String extraText;

    public static SandriosCamera with() {
        if (mInstance == null) {
            mInstance = new SandriosCamera();
        }
        return mInstance;
    }

    public SandriosCamera setShowPicker(boolean showPicker) {
        this.showPicker = showPicker;
        return mInstance;
    }

    public SandriosCamera setRootMediaPath(String rootMediaPath) {
        this.rootMediaPath = rootMediaPath;
        return mInstance;
    }

    public SandriosCamera setCameraType(int cameraType) {
        if (cameraType != CameraSwitchView.CAMERA_TYPE_FRONT && cameraType != CameraSwitchView.CAMERA_TYPE_REAR)
            return mInstance;
        this.cameraType = cameraType;
        return mInstance;
    }

    public SandriosCamera setEnablePreview(boolean enablePreview) {
        this.enablePreview = enablePreview;
        return mInstance;
    }

    public SandriosCamera setMediaAction(int mediaAction) {
        this.mediaAction = mediaAction;
        return mInstance;
    }

    public SandriosCamera enableImageCropping(boolean enableImageCrop) {
        this.enableImageCrop = enableImageCrop;
        return mInstance;
    }

    @SuppressWarnings("SameParameterValue")
    public SandriosCamera setVideoFileSize(int fileSize) {
        this.videoSize = fileSize;
        return mInstance;
    }

    /**
     * Only works if Media Action is set to Video
     */
    public SandriosCamera setAutoRecord() {
        autoRecord = true;
        return mInstance;
    }

    public void launchCamera(Activity activity) {
        if (CameraHelper.hasCamera(activity)) {
            Intent cameraIntent;
            if (CameraHelper.hasCamera2(activity)) {
                cameraIntent = new Intent(activity, Camera2Activity.class);
            } else {
                cameraIntent = new Intent(activity, Camera1Activity.class);
            }
            cameraIntent.putExtra(CameraConfiguration.Arguments.SHOW_PICKER, showPicker);
            cameraIntent.putExtra(CameraConfiguration.Arguments.MEDIA_ACTION, mediaAction);
            cameraIntent.putExtra(CameraConfiguration.Arguments.ENABLE_CROP, enableImageCrop);
            cameraIntent.putExtra(CameraConfiguration.Arguments.AUTO_RECORD, autoRecord);
            cameraIntent.putExtra(CameraConfiguration.Arguments.ENABLE_PREVIEW, enablePreview);
            cameraIntent.putExtra(CameraConfiguration.Arguments.CAMERA_TYPE, cameraType);
            cameraIntent.putExtra(CameraConfiguration.Arguments.EXTRA_TEXT, extraText);
            if (!TextUtils.isEmpty(rootMediaPath))
                cameraIntent.putExtra(CameraConfiguration.Arguments.ROOT_MEDIA_PATH, rootMediaPath);
            cameraIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (videoSize > 0) {
                cameraIntent.putExtra(CameraConfiguration.Arguments.VIDEO_FILE_SIZE, videoSize * 1024 * 1024);
            }
            activity.startActivityForResult(cameraIntent, RESULT_CODE);
        }
    }

    public SandriosCamera setOnCameraTypeChangeListener(CameraSwitchView.OnCameraTypeChangeListener onCameraTypeChangeListener) {
        this.cameraTypeChangeListener = onCameraTypeChangeListener;
        return mInstance;
    }

    public SandriosCamera setExtraText(String extraText) {
        this.extraText = extraText;
        return mInstance;
    }

    public String getExtraText() {
        return extraText;
    }

    public class MediaType {
        public static final int PHOTO = 0;
        public static final int VIDEO = 1;
    }
}
