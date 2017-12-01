package ru.kazantsev.testcamera.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.TextUtils;
import ru.kazantsev.testcamera.R;

import java.io.File;

public class ResultFragment  extends BaseFragment{

    public static ResultFragment show(BaseFragment fragment, String imagePath) {
        return show(fragment, ResultFragment.class, ARG_IMAGE_PATH, imagePath);
    }

    // Путь всегда одиноков но... пусь будет
    public static final String ARG_IMAGE_PATH = "path";

    @BindView(R.id.result_photo)
    ImageView photo;
    @BindView(R.id.result_start)
    Button start;
    @BindView(R.id.result_exit)
    Button exit;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);
        this.bind(rootView);
        String path = getArguments().getString(ARG_IMAGE_PATH);
        if(TextUtils.notEmpty(path)) {
            photo.setImageURI(Uri.fromFile(new File(path)));
        }
        return rootView;
    }


    @OnClick(R.id.result_start)
    public void onClickStart(View v) {
        LogoFragment.show(this);
    }

    @OnClick(R.id.result_exit)
    public void onClickExit(View v) {
        getBaseActivity().finish();
    }

}
