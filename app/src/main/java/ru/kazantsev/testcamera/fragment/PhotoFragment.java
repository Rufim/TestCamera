package ru.kazantsev.testcamera.fragment;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.*;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import net.vrallev.android.cat.Cat;
import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.testcamera.R;
import ru.kazantsev.testcamera.activity.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PhotoFragment extends BaseFragment {

    public static PhotoFragment show(BaseFragment fragment, Integer topAngle, Integer bottomAngle) {
        return fragment.isAdded() ? (new FragmentBuilder(fragment.getFragmentManager())).newFragment().addToBackStack().putArg(ARG_TOP_LIMIT, topAngle).putArg(ARG_BOTTOM_LIMIT, bottomAngle).replaceFragment(fragment, PhotoFragment.class) : null;
    }

    public static final String ARG_TOP_LIMIT = "top_limit";
    public static final String ARG_BOTTOM_LIMIT = "bottom_limit";

    private Integer topAngle;
    private Integer bottomAngle;

    @BindView(R.id.photo_take)
    FloatingActionButton actionButton;
    @BindView(R.id.photo_view)
    SurfaceView photoView;
    SurfaceHolder holder;
    Camera camera;

    final int CAMERA_ID = 0;
    final boolean FULL_SCREEN = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
        this.bind(rootView);
        topAngle = getArguments().getInt(ARG_TOP_LIMIT);
        bottomAngle = getArguments().getInt(ARG_BOTTOM_LIMIT);
        actionButton.setBackgroundTintList(getResources().getColorStateList(R.color.float_action_button));
        ((MainActivity) getActivity()).setOnTiltDegreesChanged(new MainActivity.OnTiltDegreesChanged() {
            @Override
            public void onTiltDegreesChanged(int degrees, boolean lookDown) {
                if (isAdded()) {
                    if (degrees < 0) {
                        degrees += 90;
                        if ((lookDown && degrees < bottomAngle) || (!lookDown && degrees < topAngle)) {
                            actionButton.setEnabled(true);
                        } else {
                            actionButton.setEnabled(false);
                        }
                    } else {
                        degrees -= 90;
                        if ((lookDown && degrees < bottomAngle) || (!lookDown && degrees < topAngle)) {
                            actionButton.setEnabled(true);
                        } else {
                            actionButton.setEnabled(false);
                        }
                    }
                }
            }
        });
        holder = photoView.getHolder();

      /*  holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                camera.stopPreview();
                setCameraDisplayOrientation(CAMERA_ID);
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });*/
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open(CAMERA_ID);
        setPreviewSize(FULL_SCREEN);
    }

    public static int getPictureSizeIndexForHeight(List<Camera.Size> sizeList, int height) {
        int chosenHeight = -1;
        for (int i = 0; i < sizeList.size(); i++) {
            if (sizeList.get(i).height < height) {
                chosenHeight = i - 1;
                if (chosenHeight == -1)
                    chosenHeight = 0;
                break;
            }
        }
        return chosenHeight;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    void setPreviewSize(boolean fullScreen) {

        // получаем размеры экрана
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();

        // определяем размеры превью камеры
        Camera.Size size = camera.getParameters().getPreviewSize();

        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();

        // RectF экрана, соотвествует размерам экрана
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());

        // RectF первью
        if (widthIsMax) {
            // превью в горизонтальной ориентации
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            // превью в вертикальной ориентации
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();
        // подготовка матрицы преобразования
        if (!fullScreen) {
            // если превью будет "втиснут" в экран (второй вариант из урока)
            matrix.setRectToRect(rectPreview, rectDisplay,
                    Matrix.ScaleToFit.START);
        } else {
            // если экран будет "втиснут" в превью (третий вариант из урока)
            matrix.setRectToRect(rectDisplay, rectPreview,
                    Matrix.ScaleToFit.START);
            matrix.invert(matrix);
        }
        // преобразование
        matrix.mapRect(rectPreview);

        // установка размеров surface из получившегося преобразования
        photoView.getLayoutParams().height = (int) (rectPreview.bottom);
        photoView.getLayoutParams().width = (int) (rectPreview.right);
    }

    void setCameraDisplayOrientation(int cameraId) {
        // определяем насколько повернут экран от нормального положения
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        // получаем инфо по камере cameraId
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        Camera.Parameters params = camera.getParameters();

        List<Camera.Size> sizeList = params.getSupportedPictureSizes();
        int chosenSize = getPictureSizeIndexForHeight(sizeList, 800);
        params.setPictureSize(sizeList.get(chosenSize).width, sizeList.get(chosenSize).height);
        params.setRotation(result);
        camera.setParameters(params);
        camera.setDisplayOrientation(result);
    }

    @OnClick(R.id.photo_take)
    public void onClickTake(View view) {
        if (view.isEnabled()) {
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try {
                        File file = new File(getContext().getCacheDir(), "photo.jpeg");
                        file.delete();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.close();
                        ResultFragment.show(PhotoFragment.this, file.getAbsolutePath());
                    } catch (Exception e) {
                        Cat.e(e);
                    }
                }
            });
        }
    }

}
