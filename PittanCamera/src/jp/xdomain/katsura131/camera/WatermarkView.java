package jp.xdomain.katsura131.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class WatermarkView extends ImageView{
	final public int SCALE_MAX = 200;
	final public int OPACITY_MAX = 50;
	
    protected float[] scaleFactor = new float[2];
    protected float[] opacity = new float[2];
    protected boolean blinking = false;
	public String[] path = new String[2];
	public int mode;
	
	final public int BACK = 0;
	final public int FRONT = 1;
	
	final private MainActivity activity = ((MainActivity)this.getContext());

    float press;
    
    AlphaAnimation alpha = (new AlphaAnimation(1.0f, 0));
    

    public void loadScaleFactor(){
    	SharedPreferences pref = getContext().getSharedPreferences( "watermark", Context.MODE_PRIVATE );
    	scaleFactor[BACK] = pref.getFloat( "scaleFactorB", 1.4f );
    	scaleFactor[FRONT] = pref.getFloat( "scaleFactorF", 1.4f );
    }
    public void loadOpacity(){
    	SharedPreferences pref = getContext().getSharedPreferences( "watermark", Context.MODE_PRIVATE );
    	opacity[BACK] = pref.getFloat( "opacityB", 0.5f );
    	opacity[FRONT] = pref.getFloat( "opacityF", 0.5f );
    }
    public void loadBlinking(){
    	SharedPreferences pref = getContext().getSharedPreferences( "watermark", Context.MODE_PRIVATE );
    	blinking = pref.getBoolean( "blinking", false );
    }
    public void loadWatermarkPath(){
    	SharedPreferences pref = getContext().getSharedPreferences( "watermark", Context.MODE_PRIVATE );
 		path[FRONT] = pref.getString( "filenameF", null );
 		path[BACK] = pref.getString( "filenameB", null );
    }

    public void savePath(String p){
    	path[mode] = p;
    	
		SharedPreferences pref = getContext().getSharedPreferences( "watermark", Context.MODE_PRIVATE );
		Editor editor = pref.edit();
		if (mode == FRONT){
			editor.putString( "filenameF", p );
    	}else{
    		editor.putString( "filenameB", p );
    	}
		editor.commit();
    }

	public WatermarkView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		loadScaleFactor();
 		loadOpacity();
 		loadWatermarkPath();
 		loadBlinking();
 		
 		alpha.setDuration(800);
 	    alpha.setRepeatCount(Animation.INFINITE);
 	    alpha.setRepeatMode(Animation.REVERSE);
    }

    public void setView(){
    	if(path[mode] == null || activity.tuningStep == activity.TS_TUNING_BACK_TAKE || activity.tuningStep == activity.TS_TUNING_FRONT_TAKE){
    		Log.d("TAG","gone");
            this.clearAnimation();
    		this.setVisibility(GONE);
    	}else{
    		Log.d("TAG","visible "+path[mode]);
    		this.setVisibility(VISIBLE);
	        this.setImageBitmap(BitmapFactory.decodeFile(path[mode]));
	        this.setImageAlpha(Math.round(255 * opacity[mode]));
	
	        int mirror = 1;
	        if(mode == FRONT){
	        	mirror = -1;
	        }
	        //press読み込み
	        press = 1f;
	        
	        this.setScaleX(scaleFactor[mode] * mirror);
	        this.setScaleY(scaleFactor[mode] * press);
	        
	        //点滅
	        if(blinking){
	        	Log.d("TAG","aa");
	            this.startAnimation(alpha);
	        }else{
	        	Log.d("TAG","bb");
	            this.clearAnimation();
	        }
    	}
    }
    
    public OnSeekBarChangeListener scaleSeekBarListener = new OnSeekBarChangeListener() {
        // トラッキング開始時に呼び出されます
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.v("onStartTrackingTouch()",String.valueOf(seekBar.getProgress()));
        }
        // トラッキング中に呼び出されます
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            Log.v("onProgressChanged()",String.valueOf(progress) + ", " + String.valueOf(fromTouch));
        }
        // トラッキング終了時に呼び出されます
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        	changeScale(seekBar);
        }
    };
    public OnSeekBarChangeListener opacitySeekBarListener = new OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        }
        // トラッキング終了時に呼び出されます
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        	changeOpacity(seekBar);
        }
    };
    
    public void moveScale(SeekBar seekBar, int val){
		Log.d("TAG", "w"+seekBar.getProgress()+" : "+val);
    	seekBar.setProgress(val);
		Log.d("TAG", "e"+seekBar.getProgress());
    	changeScale(seekBar);
    }
    public void moveOpacity(SeekBar seekBar, int val){
    	seekBar.setProgress(val);
    	changeOpacity(seekBar);
    }
    private void changeScale(SeekBar seekBar){
        Log.v("onStopTrackingTouch()",String.valueOf(seekBar.getProgress()));
		scaleFactor[mode] = ((float)seekBar.getProgress() / SCALE_MAX) + 1;
		Log.d("TAG", "stopTouch : "+ (seekBar.getProgress()));
		setView();
		invalidate();
		
		SharedPreferences pref = getContext().getSharedPreferences( "watermark", Context.MODE_PRIVATE );
		Editor editor = pref.edit();
		if(mode == FRONT){
			editor.putFloat( "scaleFactorF", scaleFactor[FRONT] );
		}else{
			editor.putFloat( "scaleFactorB", scaleFactor[BACK] );
		}
		editor.commit();
		activity.printValue();
    }
    private void changeOpacity(SeekBar seekBar){
		opacity[mode] = (seekBar.getProgress() / (float)OPACITY_MAX);
		setView();
		invalidate();
		
		SharedPreferences pref = getContext().getSharedPreferences( "watermark", Context.MODE_PRIVATE );
		Editor editor = pref.edit();
		if(mode == FRONT){
			editor.putFloat( "opacityF", opacity[FRONT] );
		}else{
			editor.putFloat( "opacityB", opacity[BACK] );
		}
		editor.commit();
		activity.printValue();
    }
    public void changeBlinking(boolean b){
		blinking = b;
    	SharedPreferences pref = getContext().getSharedPreferences( "watermark", Context.MODE_PRIVATE );
		Editor editor = pref.edit();
		editor.putBoolean( "blinking", blinking );
		editor.commit();
        setView();
    }
    
    public void syncScaleFactor(SeekBar seekBar){
        seekBar.setProgress(Math.round((scaleFactor[mode] - 1) * SCALE_MAX));
    }
    public void syncOpacity(SeekBar seekBar){
        seekBar.setProgress(Math.round(opacity[mode] * OPACITY_MAX));
        Log.d("TAG",""+opacity[mode]);
    }

}