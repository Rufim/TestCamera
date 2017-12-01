package ru.kazantsev.testcamera.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.kazantsev.template.activity.BaseActivity;
import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.PermissionMaster;
import ru.kazantsev.testcamera.R;

public class PrepareFragment extends BaseFragment {


    public static PrepareFragment show(BaseFragment fragment) {
        return show(fragment, PrepareFragment.class);
    }


    @BindView(R.id.prepare_next)
    Button next;
    @BindView(R.id.prepare_top_angle_value)
    TextView topAngleValue;
    @BindView(R.id.prepare_top_angle_seek)
    SeekBar topAngleSeek;
    @BindView(R.id.prepare_bottom_angle_value)
    TextView bottomAngleValue;
    @BindView(R.id.prepare_bottom_angle_seek)
    SeekBar bottomAngleSeek;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_prepare, container, false);
        this.bind(rootView);
        topAngleSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                topAngleValue.setText(progress + "°");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        bottomAngleSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bottomAngleValue.setText(progress + "°");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return rootView;
    }

    @OnClick(R.id.prepare_next)
    public void onClickNext(View view) {
        getBaseActivity().doActionWithPermission("android.permission.CAMERA", new BaseActivity.PermissionAction() {
            @Override
            public void doAction(boolean obtained) {
                if(obtained) {
                    PhotoFragment.show(PrepareFragment.this, topAngleSeek.getProgress(), bottomAngleSeek.getProgress());
                }
            }
        });
    }


}
