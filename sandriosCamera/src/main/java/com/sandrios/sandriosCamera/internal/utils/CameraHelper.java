package com.sandrios.sandriosCamera.internal.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.yalantis.ucrop.util.BitmapLoadUtils.calculateInSampleSize;

/**
 * Created by Arpit Gandhi on 7/6/16.
 * <p/>
 * Class with some common methods to work with camera.
 */
public final class CameraHelper {

    public final static String TAG = "CameraHelper";
    public static final String CAPTURED_IMAGES_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();


    private CameraHelper() {

    }

    public static boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasCamera2(Context context) {
        if (context == null) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return false;
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] idList = manager.getCameraIdList();
            boolean notNull = true;
            if (idList.length == 0) {
                notNull = false;
            } else {
                for (final String str : idList) {
                    if (str == null || str.trim().isEmpty()) {
                        notNull = false;
                        break;
                    }
                    final CameraCharacteristics characteristics = manager.getCameraCharacteristics(str);

                    final int supportLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                    if (supportLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                        notNull = false;
                        break;
                    }
                }
            }
            return notNull;
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static File getOutputMediaFile(Context context, String rootMediaPath, @CameraConfiguration.MediaAction int mediaAction) {
        if (TextUtils.isEmpty(rootMediaPath)) rootMediaPath = CAPTURED_IMAGES_DIR;
        File mediaStorageDir = new File(rootMediaPath);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Failed to create directory.");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.ENGLISH)
                .format(new Date());
        File mediaFile;
        if (mediaAction == CameraConfiguration.MEDIA_ACTION_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (mediaAction == CameraConfiguration.MEDIA_ACTION_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @SuppressWarnings("deprecation")
    public static Size getPictureSize(List<Size> choices, @CameraConfiguration.MediaQuality int mediaQuality) {
        if (choices == null || choices.isEmpty()) return null;
        if (choices.size() == 1) return choices.get(0);

        Size result = null;
        Size maxPictureSize = Collections.max(choices, new CompareSizesByArea2());
        Size minPictureSize = Collections.min(choices, new CompareSizesByArea2());

        Collections.sort(choices, new CompareSizesByArea2());

        if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGHEST) {
            result = maxPictureSize;
        } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOW) {
            if (choices.size() == 2) result = minPictureSize;
            else {
                int half = choices.size() / 2;
                int lowQualityIndex = (choices.size() - half) / 2;
                result = choices.get(lowQualityIndex + 1);
            }
        } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGH) {
            if (choices.size() == 2) result = maxPictureSize;
            else {
                int half = choices.size() / 2;
                int highQualityIndex = (choices.size() - half) / 2;
                result = choices.get(choices.size() - highQualityIndex - 1);
            }
        } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_MEDIUM) {
            if (choices.size() == 2) result = minPictureSize;
            else {
                int mediumQualityIndex = choices.size() / 2;
                result = choices.get(mediumQualityIndex);
            }
        } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOWEST) {
            result = minPictureSize;
        }

        return result;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size getPictureSize(Size[] sizes, @CameraConfiguration.MediaQuality int mediaQuality) {
        if (sizes == null || sizes.length == 0) return null;

        List<Size> choices = Arrays.asList(sizes);

        if (choices.size() == 1) return choices.get(0);

        Size result = null;
        Size maxPictureSize = Collections.max(choices, new CompareSizesByArea2());
        Size minPictureSize = Collections.min(choices, new CompareSizesByArea2());

        Collections.sort(choices, new CompareSizesByArea2());

        if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGHEST) {
            result = maxPictureSize;
        } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOW) {
            if (choices.size() == 2) result = minPictureSize;
            else {
                int half = choices.size() / 2;
                int lowQualityIndex = (choices.size() - half) / 2;
                result = choices.get(lowQualityIndex + 1);
            }
        } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGH) {
            if (choices.size() == 2) result = maxPictureSize;
            else {
                int half = choices.size() / 2;
                int highQualityIndex = (choices.size() - half) / 2;
                result = choices.get(choices.size() - highQualityIndex - 1);
            }
        } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_MEDIUM) {
            if (choices.size() == 2) result = minPictureSize;
            else {
                int mediumQualityIndex = choices.size() / 2;
                result = choices.get(mediumQualityIndex);
            }
        } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOWEST) {
            result = minPictureSize;
        }

        return result;
    }

    @SuppressWarnings("deprecation")
    public static Size getOptimalPreviewSize(List<Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;

        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @SuppressWarnings("deprecation")
    public static Size getSizeWithClosestRatio(List<Size> sizes, int width, int height) {
        LogUtils.d("getSizeWithClosestRatio() called with: sizes = [" + sizes + "], width = [" + width + "], height = [" + height + "]");
        if (sizes == null) return null;

        double MIN_TOLERANCE = 100;
        double targetRatio = (double) height / width;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
            if (size.getWidth() == width && size.getHeight() == height)
                return size;

            double ratio = (double) size.getHeight() / size.getWidth();

            if (Math.abs(ratio - targetRatio) < MIN_TOLERANCE) MIN_TOLERANCE = ratio;
            else continue;

            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size getOptimalPreviewSize(Size[] sizes, int width, int height) {
        LogUtils.d("getOptimalPreviewSize() called with: sizes = [" + sizes + "], width = [" + width + "], height = [" + height + "]");
        if (sizes == null) return null;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
//            if (size.getWidth() == width && size.getHeight() == height)
//                return size;
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size getSizeWithClosestRatio(Size[] sizes, int width, int height) {
        LogUtils.d("getSizeWithClosestRatio() called with: sizes = [" + sizes + "], width = [" + width + "], height = [" + height + "]");
        if (sizes == null) return null;

        double MIN_TOLERANCE = 100;
        double targetRatio = (double) height / width;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        for (Size size : sizes) {
//            if (size.getWidth() == width && size.getHeight() == height)
//                return size;

            double ratio = (double) size.getHeight() / size.getWidth();

            if (Math.abs(ratio - targetRatio) < MIN_TOLERANCE) MIN_TOLERANCE = ratio;
            else continue;

            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        LogUtils.d("chooseOptimalSize() called with: choices = [" + choices + "], width = [" + width + "], height = [" + height + "], aspectRatio = [" + aspectRatio + "]");
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea2());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return null;
        }
    }

    private static double calculateApproximateVideoSize(CamcorderProfile camcorderProfile, int seconds) {
        return ((camcorderProfile.videoBitRate / (float) 1 + camcorderProfile.audioBitRate / (float) 1) * seconds) / (float) 8;
    }

    public static double calculateApproximateVideoDuration(CamcorderProfile camcorderProfile, long maxFileSize) {
        return 8 * maxFileSize / (camcorderProfile.videoBitRate + camcorderProfile.audioBitRate);
    }

    private static long calculateMinimumRequiredBitRate(CamcorderProfile camcorderProfile, long maxFileSize, int seconds) {
        return 8 * maxFileSize / seconds - camcorderProfile.audioBitRate;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static CamcorderProfile getCamcorderProfile(String cameraId, long maximumFileSize, int minimumDurationInSeconds) {
        if (TextUtils.isEmpty(cameraId)) {
            return null;
        }
        int cameraIdInt = Integer.parseInt(cameraId);
        return getCamcorderProfile(cameraIdInt, maximumFileSize, minimumDurationInSeconds);
    }

    public static CamcorderProfile getCamcorderProfile(int currentCameraId, long maximumFileSize, int minimumDurationInSeconds) {
        if (maximumFileSize <= 0)
            return CamcorderProfile.get(currentCameraId, CameraConfiguration.MEDIA_QUALITY_HIGHEST);

        int[] qualities = new int[]{CameraConfiguration.MEDIA_QUALITY_HIGHEST,
                CameraConfiguration.MEDIA_QUALITY_HIGH, CameraConfiguration.MEDIA_QUALITY_MEDIUM,
                CameraConfiguration.MEDIA_QUALITY_LOW, CameraConfiguration.MEDIA_QUALITY_LOWEST};

        CamcorderProfile camcorderProfile;
        for (int i = 0; i < qualities.length; ++i) {
            camcorderProfile = CameraHelper.getCamcorderProfile(qualities[i], currentCameraId);
            double fileSize = CameraHelper.calculateApproximateVideoSize(camcorderProfile, minimumDurationInSeconds);

            if (fileSize > maximumFileSize) {
                long minimumRequiredBitRate = calculateMinimumRequiredBitRate(camcorderProfile, maximumFileSize, minimumDurationInSeconds);

                if (minimumRequiredBitRate >= camcorderProfile.videoBitRate / 4 && minimumRequiredBitRate <= camcorderProfile.videoBitRate) {
                    camcorderProfile.videoBitRate = (int) minimumRequiredBitRate;
                    return camcorderProfile;
                }
            } else return camcorderProfile;
        }
        return CameraHelper.getCamcorderProfile(CameraConfiguration.MEDIA_QUALITY_LOWEST, currentCameraId);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static CamcorderProfile getCamcorderProfile(@CameraConfiguration.MediaQuality int mediaQuality, String cameraId) {
        if (TextUtils.isEmpty(cameraId)) {
            return null;
        }
        int cameraIdInt = Integer.parseInt(cameraId);
        return getCamcorderProfile(mediaQuality, cameraIdInt);
    }

    public static CamcorderProfile getCamcorderProfile(@CameraConfiguration.MediaQuality int mediaQuality, int cameraId) {
        if (Build.VERSION.SDK_INT > 10) {
            if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGHEST) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGH) {
                if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P);
                } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                } else {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
                }
            } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_MEDIUM) {
                if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P);
                } else if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                } else {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                }
            } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOW) {
                if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
                } else {
                    return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
                }
            } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOWEST) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            }
        } else {
            if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGHEST) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_HIGH) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_MEDIUM) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOW) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else if (mediaQuality == CameraConfiguration.MEDIA_QUALITY_LOWEST) {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW);
            } else {
                return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class CompareSizesByArea2 implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }



    /**
     * Rotate an image if required.
     *
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    public static Bitmap getBitmap(Context context, String selectedImage) throws IOException {
        int maxDimen = 1152;
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImage, o);
//        Log.e("Test", "ABC");
        int original_width = o.outWidth;
        int original_height = o.outHeight;
        int new_width = original_width;
        int new_height = original_height;
        if (original_width > maxDimen) {
            new_width = maxDimen;
            new_height = (new_width * original_height) / original_width;
        }
        if (new_height > maxDimen) {
            new_height = maxDimen;
            new_width = (new_height * original_width) / original_height;
        }

        //Decode with inSampleSize
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(o, new_width, new_height);
        Bitmap bitmap = BitmapFactory.decodeFile(selectedImage, options);

        InputStream input = context.getContentResolver().openInputStream(Uri.fromFile(new File(selectedImage)));
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage);

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}
