package com.praescient.components.fingerprint_fp07;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fgtit.fpcore.FPMatch;
import com.praescient.components.fgtit_fp07.ActivityList;
import java.util.Timer;
import java.util.TimerTask;
import android_serialport_api.AsyncFingerprint;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortManager;
import android_serialport_api.AsyncFingerprint.OnUpCharListener;
import android_serialport_api.AsyncFingerprint.OnGenCharListener;
import android_serialport_api.AsyncFingerprint.OnGetImageListener;
import android_serialport_api.AsyncFingerprint.OnUpImageListener;

public class Fp07 extends DialogFragment {

    private static Context context;
    private static Bundle args;
    private boolean showTitle = false;
    private boolean setCancelable = false;
    private boolean shown = false;

    private LinearLayout popup;
    private ImageView preview;
    private TextView status;
    private TextView result;

    //Fingerprint Module
    private AsyncFingerprint vFingerprint;
    private boolean	bIsCancel = false;
    private boolean	bfpWork = false;
    private Timer startTimer;
    private TimerTask startTask;
    private Handler startHandler;

    public Fp07(){
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static Fp07 newInstance(Context c, String title){
        Fp07 fragment = new Fp07();
        args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        context = c;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FPMatch.getInstance().InitMatch();
        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
        FPInit();
        FPProcess();

        popup = (LinearLayout) view.findViewById(R.id.popup);
        status = (TextView) view.findViewById(R.id.status);
        result = (TextView) view.findViewById(R.id.result);
        preview = (ImageView) view.findViewById(R.id.placeholder);

        //Fetch argument from bundle and set title
        String title = getArguments().getString("title");
        getDialog().setTitle(title);

        if(args.containsKey("status")){
            status.setText(getArguments().getString("status"));
        }

        if(args.containsKey("result")){
            result.setText(getArguments().getString("result"));
        }

        if(args.containsKey("color")){
            popup.setBackgroundColor(context.getResources().getColor(getArguments().getInt("color")));
        }

        if(args.containsKey("textSize")){
            status.setTextSize(getArguments().getFloat("textSize"));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //safety check
        if (getDialog() == null){
            return;
        }

        int width = 600;
        int height = 912;

        if(args.containsKey("width")){
            width = getArguments().getInt("width");
        }

        if(args.containsKey("height")){
            height = getArguments().getInt("height");
        }

        try {
            getDialog().getWindow().setLayout(width, height);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (shown) return;
        super.show(manager, tag);
        shown = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        shown = false;
        super.onDismiss(dialog);
    }

    public boolean isShowing(){
        return shown;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if(!showTitle){
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        if(!setCancelable){
            dialog.setCanceledOnTouchOutside(setCancelable);
        }

        return dialog;
    }

    public interface MatchListener {
        void isMatch(boolean value);
    }

    public boolean showTitle(boolean value){
        return showTitle = value;
    }

    public boolean cancelable(boolean value){
        return setCancelable = value;
    }

    public void setStatus(String value){
        args.putString("status", value);
    }

    public void setResult(String value){
        args.putString("result", value);
    }

    public void setBackgroundColor(int color){
        args.putInt("color", color);
    }

    public void match(String template){
        args.putString("template", template);
    }

    public void match(String[] templates){
        args.putStringArray("templates", templates);
    }

    public void setTextSize(float size) { args.putFloat("textSize", size); }

    public void setWidth(int width) { args.putInt("width", width); }

    public void setHeight(int height) { args.putInt("height", height); }

    private void FPProcess(){

        if(!bfpWork){
            try {
                Thread.currentThread();
                Thread.sleep(500);
            }catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            vFingerprint.FP_GetImage();
            bfpWork=true;
        }

    }

    private void FPInit(){

        vFingerprint.setOnGetImageListener(new OnGetImageListener() {
            @Override
            public void onGetImageSuccess() {
                if(ActivityList.getInstance().IsUpImage){
                    vFingerprint.FP_UpImage();
                    //fingerStatus.setText("Fingerprint image being displayed...");
                }else{
                    status.setText("Reading Fingerprint...");
                    vFingerprint.FP_GenChar(1);
                }
            }

            @Override
            public void onGetImageFail() {
                if(!bIsCancel){
                    vFingerprint.FP_GetImage();
                }
            }
        });

        vFingerprint.setOnUpImageListener(new OnUpImageListener() {
            @Override
            public void onUpImageSuccess(byte[] data) {
                Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
                //preview.setImageBitmap(image);
                vFingerprint.FP_GenChar(1);
                status.setText("Reading Fingerprint...");
            }

            @Override
            public void onUpImageFail() {
                bfpWork=false;
                TimerStart();
            }
        });

        vFingerprint.setOnGenCharListener(new OnGenCharListener() {
            @Override
            public void onGenCharSuccess(int bufferId) {
                status.setText("Matching Fingerprint...");
                vFingerprint.FP_UpChar();
            }

            @Override
            public void onGenCharFail() {
                status.setText("Match Failed！");
            }
        });

        vFingerprint.setOnUpCharListener(new OnUpCharListener() {

            @Override
            public void onUpCharSuccess(byte[] model) {

                byte[] fingerprint = new byte[0];
                MatchListener activity = (MatchListener) getActivity();

                if(args.containsKey("template")){
                    fingerprint = Base64.decode( args.getString("template"), Base64.DEFAULT);
                }

                if(args.containsKey("templates")){

                    for (int i = 0; i < args.getStringArray("templates").length; i++ ){

                        try {
                            fingerprint = Base64.decode(args.getStringArray("templates")[i], Base64.DEFAULT);

                            if(FPMatch.getInstance().MatchTemplate(model, fingerprint)>60){

                                status.setText("Match Ok!");
                                SerialPortManager.getInstance().closeSerialPort();
                                activity.isMatch(true);

                            } else {
                                status.setText("Match Failed!");
                                activity.isMatch(false);
                            }

                            bfpWork=false;
                            TimerStart();

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                if(FPMatch.getInstance().MatchTemplate(model, fingerprint)>60){

                    status.setText("Match Ok!");
                    SerialPortManager.getInstance().closeSerialPort();
                    activity.isMatch(true);

                } else {
                    SerialPortManager.getInstance().closeSerialPort();
                    status.setText("Match Failed!");
                    activity.isMatch(false);
                }


                bfpWork=false;
                TimerStart();
            }

            @Override
            public void onUpCharFail() {

                status.setText("Match Failed!");
                bfpWork=false;
                TimerStart();
            }
        });
    }

    public void TimerStart(){
        if(startTimer==null){
            startTimer = new Timer();
            startHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    TimeStop();
                    FPProcess();
                }
            };
            startTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    startHandler.sendMessage(message);
                }
            };
            startTimer.schedule(startTask, 1000, 1000);
        }
    }

    public void TimeStop(){
        if (startTimer!=null)
        {
            startTimer.cancel();
            startTimer = null;
            startTask.cancel();
            startTask=null;
        }
    }

    public void minimize(){

        if(SerialPortManager.getInstance().isOpen()){
            SerialPortManager.getInstance().closeSerialPort();
        }
    }

    public void maximize(){
        vFingerprint = SerialPortManager.getInstance().getNewAsyncFingerprint();
    }
    
}
