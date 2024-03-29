package jp.xdomain.katsura131.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

@SuppressLint("NewApi")
public class PhotoView extends SurfaceView implements Callback, PictureCallback{
	private Camera camera;
	private Toast newestText = null;

	public WatermarkView watermarkView;

	public ImageButton shutter;
	public ImageButton switchButton;
	public SeekBar scaleSeekBar;
	public SeekBar opacitySeekBar;

	private int cameraNo = CameraInfo.CAMERA_FACING_BACK;
	SurfaceHolder holder;

	final public int BACK = 0;
	final public int FRONT = 1;

	boolean latest = true;

	final private MainActivity activity = ((MainActivity)this.getContext());

	public PhotoView(Context context, AttributeSet attrs){
		super(context, attrs);
		holder = getHolder();
		holder.addCallback(this);
		//holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public View.OnClickListener switchCameraButtonClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        if(cameraNo == CameraInfo.CAMERA_FACING_FRONT){
		        	changeCamera(BACK);
		        }else{
		        	changeCamera(FRONT);
		        }
		        watermarkView.setView();
			}
		};
	public void changeCamera(int cameraType){
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }

        CameraInfo caminfo = new CameraInfo();
        cameraNo = 0;
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
        	Camera.getCameraInfo(i, caminfo);
        	if (((caminfo.facing == CameraInfo.CAMERA_FACING_FRONT)&&(cameraType == FRONT))
        			||((caminfo.facing != CameraInfo.CAMERA_FACING_FRONT)&&(cameraType == BACK))) {
        		cameraNo = i;
        		break;
        	}
        }
		switch(cameraType){
		case BACK:
        	watermarkView.mode = BACK;
			break;
		case FRONT:
        	watermarkView.mode = FRONT;
			break;
		}
    	activity.printValue();
        watermarkView.syncScaleFactor(scaleSeekBar);
        watermarkView.syncOpacity(opacitySeekBar);
	    camera = Camera.open(cameraNo);
        resizePreview();
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
		}
	}

	public void aaa(View v){
		Log.d("TAG","bbb");
	}
	private Camera.PictureCallback self = this;
	View.OnClickListener shutterClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d("TAG","PUSH");
			shutter.setEnabled(false);
			camera.takePicture(shutterCallback,null,self);
		}
	};
	private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
	    @Override
	    public void onShutter() {
	    }
	};

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        if(cameraNo == CameraInfo.CAMERA_FACING_FRONT){
        	watermarkView.mode = watermarkView.FRONT;
        }else{
        	watermarkView.mode = watermarkView.BACK;
        }
		try {
			camera = Camera.open(cameraNo);
			camera.setPreviewDisplay(holder);
		} catch(IOException e) {
		}
        if(activity.freezePreview){
        	camera.startPreview();
        }
        watermarkView.setView();
	}

	private void resizePreview(){
        Parameters mParam = camera.getParameters();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            // 2.1 and before
            mParam.set("orientation", "landscape");
        } else {
            camera.setDisplayOrientation(0);
        }

        WindowManager wm = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        Point maxSize = new Point();
        disp.getSize(maxSize);

        int previewWidth = maxSize.x;
        int previewHeight = maxSize.y;

        List<Size> sizes = mParam.getSupportedPreviewSizes();
        int tmpHeight = 0;
        int tmpWidth = 0;
        for (Size size : sizes) {
            if ((size.width > previewWidth) || (size.height > previewHeight)) {
                continue;
            }
            if (tmpHeight < size.height) {
                tmpWidth = size.width;
                tmpHeight = size.height;
            }
        }
        previewWidth = tmpWidth;
        previewHeight = tmpHeight;

        mParam.setPreviewSize(previewWidth, previewHeight);

        // Adjust SurfaceView size
        float layoutHeight, layoutWidth;
        layoutHeight = previewHeight;
        layoutWidth = previewWidth;

        float factH, factW, fact;
        factH = maxSize.y / layoutHeight;
        factW = maxSize.x / layoutWidth;
        // Select smaller factor, because the surface cannot be set to the size larger than display metrics.
        if (factH < factW) {
            fact = factH;
            activity.gradationVertical();
        } else {
            fact = factW;
            activity.gradationHorizontal();
        }
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        layoutParams.height = (int)(layoutHeight * fact);
        layoutParams.width = (int)(layoutWidth * fact);
        this.setLayoutParams(layoutParams);
        Log.d("TAG","prevW"+layoutParams.width);
        Log.d("TAG","prevH"+layoutParams.height);

        camera.setParameters(mParam);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
        Log.d("TAG","surfaceChange: "+holder+"/"+format+"/"+width+"/"+height);
        camera.stopPreview();
        resizePreview();
    	if(! activity.freezePreview){
    		camera.startPreview();
    	}
        shutter.setEnabled(true);
	}


	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d("TAG","onPictureTaken");
        if (data != null) {
            FileOutputStream myFOS = null;
            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/");
                if(!file.exists()){
                	file.mkdir();
                }
            	String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/"+ makeFileName();
            	myFOS = new FileOutputStream(path);
                myFOS.write(data);
                myFOS.close();
                if(newestText != null){
                	newestText.cancel();
                }
                newestText = Toast.makeText(getContext(), path + getResources().getString(R.string.toastSavedAs), Toast.LENGTH_LONG);
    			newestText.show();

    			String[] paths = {path};
    			MediaScannerConnection.scanFile(activity,paths,null,null);

    			if((latest)||(activity.tuningStep != activity.TS_NORMAL)){
    				watermarkView.savePath(path);
    				watermarkView.setView();
    			}
            } catch (Exception e) {
                e.printStackTrace();
                if(newestText != null){
                    newestText.cancel();
                }
                newestText = Toast.makeText(getContext(), getResources().getString(R.string.toastException), Toast.LENGTH_LONG);
    		    newestText.show();
            }

            if(activity.tuningStep == activity.TS_NORMAL){
            	camera.startPreview();
            }else{
            	activity.advanceTuningStep();
            }
        }
		shutter.setEnabled(true);

	}
    protected boolean isPortrait() {
        return (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
    }

	protected String makeFileName(){
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
    	return String.format("%1$tY%1$tm%1$td_%1$TH%1$TM%1$TS%1$TL_",cal) + TimeZone.getDefault().getID().replaceAll("/", "_") +".jpg";
	}

}