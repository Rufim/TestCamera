package ru.kazantsev.testcamera.fragment;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import butterknife.BindView;
import butterknife.OnClick;
import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;
import ru.kazantsev.testcamera.R;

public class LogoFragment extends BaseFragment {


    public static LogoFragment show(BaseFragment fragment) {
        return show(fragment, LogoFragment.class);
    }


    @BindView(R.id.logo_start)
    Button start;
    @BindView(R.id.logo_exit)
    Button exit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logo, container, false);
        this.bind(rootView);
        return rootView;
    }

    @OnClick(R.id.logo_start)
    public void onClickStart(View v) {
        PrepareFragment.show(this);
    }

    @OnClick(R.id.logo_exit)
    public void onClickExit(View v) {
         getBaseActivity().finish();
    }
}
