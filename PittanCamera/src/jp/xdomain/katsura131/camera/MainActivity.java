package jp.xdomain.katsura131.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends Activity {
	protected PhotoView pv;
	protected WatermarkView wm;

	protected TextView commentWindow;

	protected RelativeLayout seekBarsLayout;
	protected SeekBar scaleSeekBar;
	protected SeekBar opacitySeekBar;

	protected Button closeButton;
	protected Button plusButton;
	protected Button minusButton;

	protected ImageButton shutter;
	protected ImageButton wmButton;
	protected ImageButton switchButton;
	protected ImageButton optionsButton;

	protected ImageButton addTuningButton;
	protected ImageButton addChangeScaleButton;
	protected ImageButton addChangeOpacityButton;
	protected ImageButton addBlinkingButton;
	protected ImageButton addAutoRefreshButton;
	protected ImageButton[] addition;
	protected boolean optionOpen = false;

	protected RelativeLayout tuningLayout;
	protected TextView tuningMessage;
	protected Button tuningOKButton;

	final public int SCALE_MAX = 200;
	final public int OPACITY_MAX = 50;

	public int seekBarMode;

	public int tuningStep;
	final public int TS_NOT_TUNED = -1;
	final public int TS_NORMAL = 0;
	final public int TS_TUNING_BACK_TAKE = 1;
	final public int TS_TUNING_BACK_TUNE = 2;
	final public int TS_TUNING_FRONT_TAKE = 3;
	final public int TS_TUNING_FRONT_TUNE = 4;
	public boolean freezePreview = false;

	final public int BACK = 0;
	final public int FRONT = 1;

	final public int WC = RelativeLayout.LayoutParams.WRAP_CONTENT;

	//for click listener
	final private int ID_WATERMARK_BUTTON = 0;
	final private int ID_OPTIONS_BUTTON = 1;
	final private int ID_ADD_TUNING_BUTTON = 2;
	final private int ID_ADD_CHANGE_SCALE_BUTTON = 3;
	final private int ID_ADD_CHANGE_OPACITY_BUTTON = 4;
	final private int ID_ADD_BLINKING_BUTTON = 5;
	final private int ID_ADD_AUTO_REFRESH_BUTTON = 6;
	final private int ID_CLOSE_BUTTON = 7;
	final private int ID_PLUS_BUTTON = 8;
	final private int ID_MINUS_BUTTON = 9;
	final private int ID_TUNING_OK_BUTTON = 10;

	final MainActivity self = this;

	protected void gradationVertical(){
		RelativeLayout root = (RelativeLayout) findViewById(R.id.rootLayout);
		root.setBackgroundResource(R.drawable.gradv);
	}
	protected void gradationHorizontal(){
		RelativeLayout root = (RelativeLayout) findViewById(R.id.rootLayout);
		root.setBackgroundResource(R.drawable.gradh);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.camera);
        pv = (PhotoView) findViewById(R.id.preview);
        wm = (WatermarkView) findViewById(R.id.watermarkView);
        pv.watermarkView = wm;

        shutter = (ImageButton)findViewById(R.id.shutter);
        shutter.setOnClickListener(pv.shutterClickListener);
        shutter.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentShutter)));
        pv.shutter = shutter;


        wmButton = (ImageButton)findViewById(R.id.watermarkButton);
        wmButton.setOnClickListener(getAnyButtonClickListener(ID_WATERMARK_BUTTON));
        wmButton.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentWatermark)));

        //switch camera button
        switchButton = (ImageButton)findViewById(R.id.switchCameraButton);
        switchButton.setOnClickListener(pv.switchCameraButtonClickListener);
        switchButton.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentSwitch)));
        pv.switchButton = switchButton;

        //options buttons
        optionsButton = (ImageButton)findViewById(R.id.optionsButton);
        optionsButton.setOnClickListener(getAnyButtonClickListener(ID_OPTIONS_BUTTON));
        optionsButton.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentOptions)));

        addChangeScaleButton = (ImageButton)findViewById(R.id.changeScaleButton);
        addChangeScaleButton.setOnClickListener(getAnyButtonClickListener(ID_ADD_CHANGE_SCALE_BUTTON));
        addChangeScaleButton.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentScale)));
        addChangeOpacityButton = (ImageButton)findViewById(R.id.changeOpacityButton);
        addChangeOpacityButton.setOnClickListener(getAnyButtonClickListener(ID_ADD_CHANGE_OPACITY_BUTTON));
        addChangeOpacityButton.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentOpacity)));
        addBlinkingButton = (ImageButton)findViewById(R.id.blinkingButton);
        addBlinkingButton.setOnClickListener(getAnyButtonClickListener(ID_ADD_BLINKING_BUTTON));
        addBlinkingButton.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentBlinking)));
        addAutoRefreshButton = (ImageButton)findViewById(R.id.autoRefreshButton);
        addAutoRefreshButton.setOnClickListener(getAnyButtonClickListener(ID_ADD_AUTO_REFRESH_BUTTON));
        addAutoRefreshButton.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentAutoRefresh)));
        addTuningButton = (ImageButton)findViewById(R.id.tuningButton);
        addTuningButton.setOnClickListener(getAnyButtonClickListener(ID_ADD_TUNING_BUTTON));
        addTuningButton.setOnTouchListener(getAnyButtonTouchListener(getResources().getString(R.string.commentTuning)));

        ImageButton[] additionC = {addChangeScaleButton,addChangeOpacityButton,addBlinkingButton,addAutoRefreshButton,addTuningButton};
        addition = additionC;

        //comment
        commentWindow = (TextView)findViewById(R.id.commentWindow);

        //seek bar
        seekBarsLayout = (RelativeLayout) findViewById(R.id.seekbarsLayout);
        //scale seek bar
        scaleSeekBar = (SeekBar) findViewById(R.id.seekbar_Scale);
        scaleSeekBar.setMax(SCALE_MAX);
        wm.syncScaleFactor(scaleSeekBar);
        scaleSeekBar.setOnSeekBarChangeListener(wm.scaleSeekBarListener);
        pv.scaleSeekBar = scaleSeekBar;
        //opacity seek bar
        opacitySeekBar = (SeekBar) findViewById(R.id.seekbar_Opacity);
        opacitySeekBar.setMax(OPACITY_MAX);
        wm.syncOpacity(opacitySeekBar);
        opacitySeekBar.setOnSeekBarChangeListener(wm.opacitySeekBarListener);
        pv.opacitySeekBar = opacitySeekBar;

        //atom button
        closeButton = (Button) findViewById(R.id.seekbar_closeButton);
        closeButton.setOnClickListener(getAnyButtonClickListener(ID_CLOSE_BUTTON));
        plusButton = (Button) findViewById(R.id.seekbar_plusButton);
        plusButton.setOnClickListener(getAnyButtonClickListener(ID_PLUS_BUTTON));
        minusButton = (Button) findViewById(R.id.seekbar_minusButton);
        minusButton.setOnClickListener(getAnyButtonClickListener(ID_MINUS_BUTTON));

    	SharedPreferences prefSeekbar = getApplicationContext().getSharedPreferences( "seekBar", Context.MODE_PRIVATE );

    	seekBarMode = prefSeekbar.getInt( "seekBarMode", 0 );
    	switchSeekBar(seekBarMode);

    	//tuning mode layout
    	tuningLayout = (RelativeLayout) findViewById(R.id.tuningLayout);
    	tuningLayout.setVisibility(View.GONE);
    	tuningMessage = (TextView) findViewById(R.id.tuningModeMessage);

    	tuningOKButton = (Button) findViewById(R.id.tuningOK);
        tuningOKButton.setOnClickListener(getAnyButtonClickListener(ID_TUNING_OK_BUTTON));
    	tuningOKButton.setVisibility(View.GONE);

    	//tuningStep�ǂݍ���
    	SharedPreferences prefTuning = getApplicationContext().getSharedPreferences( "tuning", Context.MODE_PRIVATE );
    	tuningStep = prefTuning.getInt("tuningStep", TS_NOT_TUNED);
    	if(tuningStep == TS_NOT_TUNED){
    		//�_�C�A���O
    		new AlertDialog.Builder(this)
            	.setTitle(R.string.firstTuningAlertTitle)
            	.setMessage(R.string.firstTuningAlertMessage)
            	.setPositiveButton("OK", null)
            	.show();
    		advanceTuningStep();
    	}
    	applyTuningStep();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus){
	    super.onWindowFocusChanged(hasFocus);
	    Point dispSize = new Point();
	    ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(dispSize);
        final float radius = 40 + optionsButton.getHeight();
	    final int px,py;
	    int x,y;
	    px = optionsButton.getLeft();
	    py = optionsButton.getTop();
	    x = Math.round((float)Math.cos(270 * Math.PI / 180) * radius);
	    y = Math.round((float)Math.sin(270 * Math.PI / 180) * radius * 1.4f);
	    ((LinearLayout)findViewById(R.id.changeScaleButtonLayout)).setPadding(px + x, py + y, 0, 0);
	    x = Math.round((float)Math.cos(230 * Math.PI / 180) * radius);
	    y = Math.round((float)Math.sin(230 * Math.PI / 180) * radius * 1.4f);
	    ((LinearLayout)findViewById(R.id.changeOpacityButtonLayout)).setPadding(px + x, py + y, 0, 0);
	    x = Math.round((float)Math.cos(130 * Math.PI / 180) * radius);
	    y = Math.round((float)Math.sin(130 * Math.PI / 180) * radius * 1.4f);
	    ((LinearLayout)findViewById(R.id.blinkingButtonLayout)).setPadding(px + x, py, 0, dispSize.y - py - y - optionsButton.getHeight());
	    x = Math.round((float)Math.cos(90 * Math.PI / 180) * radius);
	    y = Math.round((float)Math.sin(90 * Math.PI / 180) * radius * 1.4f);
	    ((LinearLayout)findViewById(R.id.autoRefreshButtonLayout)).setPadding(px + x, py, 0, dispSize.y - py - y - optionsButton.getHeight());
	    x = Math.round((float)Math.cos(180 * Math.PI / 180) * radius);
	    y = Math.round((float)Math.sin(180 * Math.PI / 180) * radius * 1.4f);
	    ((LinearLayout)findViewById(R.id.tuningButtonLayout)).setPadding(px + x, py + y, 0, 0);
	}

	public View.OnTouchListener getAnyButtonTouchListener(final String comment){
		View.OnTouchListener anyButtonTouchListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				if(e.getAction() == MotionEvent.ACTION_DOWN){
					//�ݒ�p�{�^��
					//RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(WC,WC);
					//lp.addRule(RelativeLayout.LEFT_OF, v.getId());
					//lp.addRule(RelativeLayout.ALIGN_TOP,v.getId());
				    Point dispSize = new Point();
				    ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(dispSize);
					commentWindow.setText(comment);
					commentWindow.setVisibility(View.VISIBLE);
					//commentWindow.setLayoutParams(lp);

					int x = dispSize.x - (v.getLeft() + v.getWidth());
				    int y =  dispSize.y - v.getTop();
				    ((LinearLayout)findViewById(R.id.commentWindowLayout)).setPadding(0, 0, x, y);

			    }else if(e.getAction() == MotionEvent.ACTION_UP){
					commentWindow.setVisibility(View.GONE);
			    }
			    return false;
			}
		};
		return anyButtonTouchListener;
	}
	public View.OnClickListener getAnyButtonClickListener(final int num) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch(num){
				case ID_WATERMARK_BUTTON:
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_PICK);
					startActivityForResult(intent, 0);
					break;
				case ID_OPTIONS_BUTTON:
					if (optionOpen) {
						closeAdditionalButtons();
					}else{
						openAdditionalButtons();
					}
					break;
				case ID_ADD_TUNING_BUTTON:
					advanceTuningStep();
					break;
				case ID_ADD_CHANGE_SCALE_BUTTON:
					if(seekBarMode == 1){
						switchSeekBar(0);
					}else{
						switchSeekBar(1);
					}
					break;
				case ID_ADD_CHANGE_OPACITY_BUTTON:
					if(seekBarMode == 2){
						switchSeekBar(0);
					}else{
						switchSeekBar(2);
					}
					break;
				case ID_ADD_BLINKING_BUTTON:
					if(wm.blinking){
						wm.changeBlinking(false);
					}else{
						wm.changeBlinking(true);
					}
					break;
				case ID_ADD_AUTO_REFRESH_BUTTON:
					if(pv.latest){
						pv.latest = false;
			    		new AlertDialog.Builder(self)
			            	.setTitle(R.string.autoRefreshAlertTitle)
			            	.setMessage(R.string.autoRefreshAlertMessageOff)
			            	.setPositiveButton("OK", null)
			            	.show();
					}else{
						pv.latest = true;
			    		new AlertDialog.Builder(self)
		            	.setTitle(R.string.autoRefreshAlertTitle)
		            	.setMessage(R.string.autoRefreshAlertMessageOn)
		            	.setPositiveButton("OK", null)
		            	.show();
					}
					break;
				case ID_CLOSE_BUTTON:
					switchSeekBar(0);
					break;
				case ID_PLUS_BUTTON:
					plusMinus(1);
					break;
				case ID_MINUS_BUTTON:
					plusMinus(-1);
					break;
				case ID_TUNING_OK_BUTTON:
					advanceTuningStep();
					break;
				}
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0 && resultCode == RESULT_OK) {
			try {
				ContentResolver cr = getContentResolver();
	            String[] columns = {MediaColumns.DATA };
	            Cursor c = cr.query(data.getData(), columns, null, null, null);
	            c.moveToFirst();
	            wm.savePath(c.getString(0));
	            c.close();
				wm.setView();
			} catch (Exception e) {

			}
		}
	}

	public void openAdditionalButtons(){
		optionOpen = true;
		optionsButton.setBackgroundResource(R.drawable.smallbutton_dark);
		TranslateAnimation anime;
		float x, y;
		for(int i = 0; i < addition.length; i++){
			addition[i].setVisibility(View.VISIBLE);
			x = optionsButton.getLeft() - addition[i].getLeft();
			y = optionsButton.getTop() - addition[i].getTop();
			anime = new TranslateAnimation(Animation.ABSOLUTE, x, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, y, Animation.RELATIVE_TO_SELF, 0);
			anime.setDuration(80);
		    addition[i].startAnimation(anime);

		}
	}

	public void closeAdditionalButtons(){
		optionOpen = false;
		optionsButton.setBackgroundResource(R.drawable.smallbutton);
		TranslateAnimation anime;
		float x, y;
		for(int i = 0; i < addition.length; i++){
			addition[i].setVisibility(View.INVISIBLE);
			x = optionsButton.getLeft() - addition[i].getLeft();
			y = optionsButton.getTop() - addition[i].getTop();
			anime = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, x, Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, y);
			anime.setDuration(100);
		    addition[i].startAnimation(anime);
		}
	}
	private void plusMinus(int val){
		if(scaleSeekBar.getVisibility() == View.VISIBLE){
			wm.moveScale(scaleSeekBar,scaleSeekBar.getProgress() + val);
	        wm.syncScaleFactor(scaleSeekBar);

		}else if(opacitySeekBar.getVisibility() == View.VISIBLE){
			wm.moveOpacity(opacitySeekBar,opacitySeekBar.getProgress() + val);
	        wm.syncOpacity(opacitySeekBar);
		}
	}

	public void switchSeekBar(int newMode){
		switch (newMode){
		case 0:
			addChangeScaleButton.setBackgroundResource(R.drawable.smallbutton);
			addChangeOpacityButton.setBackgroundResource(R.drawable.smallbutton);
			break;
		case 1:
			addChangeOpacityButton.setBackgroundResource(R.drawable.smallbutton);
			addChangeScaleButton.setBackgroundResource(R.drawable.smallbutton_dark);
			break;
		case 2:
			addChangeScaleButton.setBackgroundResource(R.drawable.smallbutton);
			addChangeOpacityButton.setBackgroundResource(R.drawable.smallbutton_dark);
		}

		TranslateAnimation down = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);

		if(seekBarMode != 0){
			if(newMode == 0){
				Log.d("TAG", "b");
	    	    down.setDuration(300);
			}else{
				Log.d("TAG", "c");
	    	    down.setDuration(100);
			}
			seekBarMode = newMode;
			down.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationEnd(Animation arg0) {appearSeekBar();}
				@Override
				public void onAnimationRepeat(Animation arg0) {}
				@Override
				public void onAnimationStart(Animation arg0) {}
	        });
		    seekBarsLayout.startAnimation(down);
		}else{
			seekBarMode = newMode;
			appearSeekBar();
		}

    	SharedPreferences pref = getApplicationContext().getSharedPreferences( "seekBar", Context.MODE_PRIVATE );
		Editor editor = pref.edit();
		editor.putInt( "seekBarMode", seekBarMode );
		editor.commit();
	}

	private void appearSeekBar(){
		TranslateAnimation up = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);

		printValue();
		switch (seekBarMode) {
    	case 0:
    		seekBarsLayout.setVisibility(View.GONE);
    		break;
    	case 1:
    		((TextView)findViewById(R.id.seekbar_title)).setText(R.string.scaleOfWatermark);
    		seekBarsLayout.setVisibility(View.VISIBLE);
    		scaleSeekBar.setVisibility(View.VISIBLE);
    		opacitySeekBar.setVisibility(View.GONE);
    		seekBarsLayout.setBackgroundResource(R.drawable.scaletuner);

    	    up.setDuration(300);
    	    seekBarsLayout.startAnimation(up);
    		break;
    	case 2:
    		((TextView)findViewById(R.id.seekbar_title)).setText(R.string.opacityOfWatermark);
    		seekBarsLayout.setVisibility(View.VISIBLE);
    		scaleSeekBar.setVisibility(View.GONE);
    		opacitySeekBar.setVisibility(View.VISIBLE);
    		seekBarsLayout.setBackgroundResource(R.drawable.opacitytuner);

    	    up.setDuration(300);
    	    seekBarsLayout.startAnimation(up);
    		break;
    	}
	}

	public void advanceTuningStep(){
		switch(tuningStep){
		case TS_NOT_TUNED:
			tuningStep = TS_TUNING_BACK_TAKE;
        	wm.setView();
			applyTuningStep();
			break;
		case TS_NORMAL:
			tuningStep = TS_TUNING_BACK_TAKE;
        	pv.changeCamera(BACK);
            wm.setView();
			applyTuningStep();
			break;
		case TS_TUNING_BACK_TAKE:
    		tuningStep = TS_TUNING_BACK_TUNE;
    		wm.setView();
			applyTuningStep();
			break;
		case TS_TUNING_BACK_TUNE:
	        CameraInfo caminfo = new CameraInfo();
	        boolean frontCameraUsable = false;
	        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
	        	Camera.getCameraInfo(i, caminfo);
	        	if (caminfo.facing == CameraInfo.CAMERA_FACING_FRONT){
	        		frontCameraUsable = true;
	        		break;
	        	}
	        }
    		if(frontCameraUsable){
    			tuningStep = TS_TUNING_FRONT_TAKE;
            	pv.changeCamera(FRONT);
            	wm.setView();
    			applyTuningStep();
    		}else{
    			quitTuning();
    		}
			break;
		case TS_TUNING_FRONT_TAKE:
			tuningStep = TS_TUNING_FRONT_TUNE;
        	wm.setView();
			applyTuningStep();
			break;
		case TS_TUNING_FRONT_TUNE:
			quitTuning();
			break;
		}
	}
	public void quitTuning(){
		tuningStep = TS_NORMAL;
		closeButton.setVisibility(View.VISIBLE);
    	pv.changeCamera(BACK);
    	wm.setView();
		applyTuningStep();
		new AlertDialog.Builder(this)
	    	.setTitle(R.string.tuningQuitAlertTitle)
	    	.setMessage(R.string.tuningQuitAlertMessage)
	    	.setPositiveButton("OK", null)
	    	.show();
	}
	private void applyTuningStep(){
		switch(tuningStep){
		case TS_NOT_TUNED:
		case TS_NORMAL:
    		shutter.setVisibility(View.VISIBLE);
    		wmButton.setVisibility(View.VISIBLE);
    		switchButton.setVisibility(View.VISIBLE);
    		optionsButton.setVisibility(View.VISIBLE);
    		switchSeekBar(0);
    		tuningLayout.setVisibility(View.GONE);
    		tuningOKButton.setVisibility(View.GONE);
        	freezePreview = false;
    		break;
		case TS_TUNING_BACK_TAKE:
			shutter.setVisibility(View.VISIBLE);
    		wmButton.setVisibility(View.GONE);
    		switchButton.setVisibility(View.GONE);
    		optionsButton.setVisibility(View.GONE);
    		switchSeekBar(0);
    		tuningLayout.setVisibility(View.VISIBLE);
    		tuningMessage.setText(R.string.tuningModeMessage1);
    		tuningOKButton.setVisibility(View.GONE);
    		closeButton.setVisibility(View.GONE);
        	freezePreview = false;
        	closeAdditionalButtons();
			break;
		case TS_TUNING_BACK_TUNE:
    		shutter.setVisibility(View.GONE);
    		wmButton.setVisibility(View.GONE);
    		switchButton.setVisibility(View.GONE);
    		optionsButton.setVisibility(View.GONE);
    		switchSeekBar(1);
    		tuningLayout.setVisibility(View.VISIBLE);
    		tuningMessage.setText(R.string.tuningModeMessage2);
    		tuningOKButton.setVisibility(View.VISIBLE);
        	freezePreview = true;
			break;
		case TS_TUNING_FRONT_TAKE:
        	shutter.setVisibility(View.VISIBLE);
        	wmButton.setVisibility(View.GONE);
        	switchButton.setVisibility(View.GONE);
        	optionsButton.setVisibility(View.GONE);
        	switchSeekBar(0);
    		tuningLayout.setVisibility(View.VISIBLE);
        	tuningMessage.setText(R.string.tuningModeMessage3);
        	tuningOKButton.setVisibility(View.GONE);
        	freezePreview = false;
			break;
		case TS_TUNING_FRONT_TUNE:
    		shutter.setVisibility(View.GONE);
    		wmButton.setVisibility(View.GONE);
    		switchButton.setVisibility(View.GONE);
    		optionsButton.setVisibility(View.GONE);
    		switchSeekBar(1);
    		tuningLayout.setVisibility(View.VISIBLE);
    		tuningMessage.setText(R.string.tuningModeMessage2);
    		tuningOKButton.setVisibility(View.VISIBLE);
        	freezePreview = true;
			break;
		}

		SharedPreferences prefTuning = getApplicationContext().getSharedPreferences( "tuning", Context.MODE_PRIVATE );
    	prefTuning.edit().putInt( "tuningStep", tuningStep ).commit();
	}
	@Override
	  public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode==KeyEvent.KEYCODE_BACK){
	      if(tuningStep != TS_NORMAL && tuningStep != TS_NOT_TUNED){
	    	  new AlertDialog.Builder(this)
          		.setTitle(R.string.tuningQuitQuestionTitle)
          		.setMessage(R.string.tuningQuitQuestionMessage)
          		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
					public void onClick(DialogInterface dialog, int which) {
                        quitTuning();
                    }
                })
          		.setNegativeButton("Cancel", null)
          		.show();
	    	  return true;
	      }
	      else{
	    	  return super.onKeyDown(keyCode,event);
	      }
	    }
	    return false;
	  }


    public void printValue(){
		float val = 0;
    	switch(seekBarMode){
    	case 1:
    		val = wm.scaleFactor[wm.mode];
    		break;
    	case 2:
    		val = wm.opacity[wm.mode];
    		break;
    	}
    	int n = Math.round(val * 100f * 10f);
    	float result = n / 10f;
		((TextView)findViewById(R.id.seekbar_value)).setText(String.valueOf(result)+"%");
    }
}


//�u�s�����x�������I�ɕω�������FON/OFF�v�{�^���𓧖��x�E�B���h�E�̏�̐^�񒆂ɔz�u�A����l��OFF
//ON�ɂ���ƃV�[�N�o�[�ƒ����{�^���������ĂR�̑I��������I������g�ɂȂ�