
package com.starkIndustry.spark.activity;








import com.starkIndustry.spark.R;
import com.starkIndustry.spark.gpuimage.GPUImage;
import com.starkIndustry.spark.gpuimage.GPUImageFilter;
import com.starkIndustry.spark.gpuimage.GPUImageSoftGlowFilter;
import com.starkIndustry.spark.utils.CameraHelper;
import com.starkIndustry.spark.utils.CameraHelper.CameraInfo2;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class ActivityCamera extends Activity implements OnClickListener {
	
	private GPUImage mGPUImage;
	private GPUImageFilter mFilter;
	
	
    private CameraHelper mCameraHelper;
    private CameraLoader mCamera;
    
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		findViewById(R.id.button_no_filter).setOnClickListener(this);
		findViewById(R.id.button_whiten_filter).setOnClickListener(this);
		
		 mGPUImage = new GPUImage(this);
	     mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surfaceView));
		
	     mCameraHelper = new CameraHelper(this);
	     mCamera = new CameraLoader();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        mCamera.onResume();
    }

    @Override
    protected void onPause() {
        mCamera.onPause();
        super.onPause();
    }
    
    public void onClick(final View v) {	
    	
    	switch (v.getId()) {
        case R.id.button_no_filter:
        	switchFilterTo(new GPUImageFilter());
            break;

        case R.id.button_whiten_filter:
        	switchFilterTo(new GPUImageSoftGlowFilter());
            break;

        case R.id.img_switch_camera:
            mCamera.switchCamera();
            break;
    }
    }
    	
        private void switchFilterTo(final GPUImageFilter filter) {
            if (mFilter == null
                    || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
                mFilter = filter;
                mGPUImage.setFilter(mFilter);
              
            }
        }
    
	
    private class CameraLoader {
        private int mCurrentCameraId = 0;
        private Camera mCameraInstance;

        public void onResume() {
            setUpCamera(mCurrentCameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
            setUpCamera(mCurrentCameraId);
        }

        private void setUpCamera(final int id) {
            mCameraInstance = getCameraInstance(id);
            Parameters parameters = mCameraInstance.getParameters();
            // TODO adjust by getting supportedPreviewSizes and then choosing
            // the best one for screen size (best fill screen)
            parameters.setPreviewSize(720, 480);
            if (parameters.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            mCameraInstance.setParameters(parameters);

            int orientation = mCameraHelper.getCameraDisplayOrientation(
                    ActivityCamera.this, mCurrentCameraId);
            CameraInfo2 cameraInfo = new CameraInfo2();
            mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT
                    ? true : false;
            mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
        }

        /** A safe way to get an instance of the Camera object. */
        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = mCameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            mCameraInstance.setPreviewCallback(null);
            mCameraInstance.release();
            mCameraInstance = null;
        }
    }

}
