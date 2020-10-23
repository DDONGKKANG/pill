package com.example.WhatDrug;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import com.example.WhatDrug.BuildConfig;
import com.example.WhatDrug.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class CameraActivity extends AppCompatActivity {

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};//권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101;//권한 동의 여부 문의 후 callback함수에 쓰일 변수

    private static final int PICK_FROM_ALBUM = 1; //앨범에서 사진 가져오기
    private static final int CROP_FROM_IMAGE = 2; //가져온 사진을 자르기 위한 변수
    private String mCurrentPhotoPath;   //사진파일 현재 경로

    String img_name ; //파일이 저장될 이름. 이름.png
    String cropImageDiretory;//크롭된 사진이 저장될 디렉토리

    private Uri photoUri;//촬영한, 크롭된 이미지 경로를 담는 변수
    ImageView imageView;
    CameraSurfaceView cameraView;
    boolean crop;   //크롭사진이 생성 되었는지 여부
    int usingCamera;//전면, 후면 중 어떤 카메라를 쓰고있는가 여부. Camera.CameraInfo.CAMERA_FACING_BACK, CAMERA_FACING_FRONT
    private View mLayout;

    private SurfaceView surfaceView;
    private SurfaceView mSurfaceView;
    MyThread myThread;
    //전송을위한 변수
    String[] c_result;
    AppCompatDialog progressDialog;

    // 지영 추가
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();

        // Custom Actionbar를 사용하기 위해 CustomEnabled을 true 시키고 필요 없는 것은 false 시킨다
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);            //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);        //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);            //홈 아이콘을 숨김처리합니다.


        //layout을 가지고 와서 actionbar에 포팅을 시킵니다.
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionbar = inflater.inflate(R.layout.custom_actionbar, null);

        actionBar.setCustomView(actionbar);

        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar)actionbar.getParent();
        parent.setContentInsetsAbsolute(0,0);

        // 뒤로가기 버튼 이벤트 처리
        ImageButton bt_close = (ImageButton) findViewById(R.id.btnBack);
        bt_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 상태바를 안보이도록 합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        surfaceView = findViewById(R.id.camera_preview_main);

        checkPermissions();
        init();
        // 뒤로가기 버튼 이벤트 처리
        ImageButton bt_close = (ImageButton) findViewById(R.id.btn_Back);
        bt_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    private boolean checkPermissions() {//사용권한 묻는 함수
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);//현재 컨텍스트가 pm 권한을 가졌는지 확인
            if (result != PackageManager.PERMISSION_GRANTED) {//사용자가 해당 권한을 가지고 있지 않을 경우
                permissionList.add(pm);//리스트에 해당 권한명을 추가한다
            }
        }
        if (!permissionList.isEmpty()) {//권한이 추가되었으면 해당 리스트가 empty가 아니므로, request 즉 권한을 요청한다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            init();

            return false;
        }
        return true;
    }

    //권한 요청의 콜백 함수. PERMISSION_GRANTED 로 권한을 획득하였는지를 확인할 수 있다.
    //아래에서는 !=를 사용했기에 권한 사용에 동의를 안했을 경우를 if문을 사용해서 코딩.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        }
                    }
                    init();
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }

    //권한 획득에 동의하지 않았을 경우, 아래 메시지를 띄우며 해당 액티비티를 종료.(첫 페이지니 앱 종료)
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }


    private void init(){
        cropImageDiretory = "/camtest/";//크롭된 사진이 저장될 디렉토리
        imageView = (ImageView)findViewById(R.id.cropimg);//크롭된 사진을 넣을 이미지 뷰
        crop = false;
        usingCamera = Camera.CameraInfo.CAMERA_FACING_BACK;//후면 카메라가 기본.

        initCamera();
    }

    //카메라 프리뷰 초기화
    private void initCamera(){
        cameraView = new CameraSurfaceView(getApplicationContext(), surfaceView, usingCamera);//카메라 프리뷰가 나올 서페이스뷰

    }
    //************ 서버 로딩 화면
    public void progressON(CameraActivity activity, String message) {

        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressSET(message);
        } else {
            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.progress_loading);
            progressDialog.show();
            System.out.println("progressON");
        }


        final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
        img_loading_frame.post(new Runnable() {
            @Override
            public void run() {
                frameAnimation.start();
            }
        });

        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }


    }

    public void progressSET(String message) {
        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }

        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }

    public void progressOFF() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    //촬영과 앨범 선택 두 버튼의 리스너
    public void onClickTakeImage(View v) throws InterruptedException {
        switch (v.getId()) {
            case R.id.btncamera:
                System.out.println("로딩 화면 시작");
                progressON(CameraActivity.this,"Loading...");
                takePhoto();
                break;
            case R.id.btnalbum:
                goToAlbum();//앨범이면 앨범에서 사진 가져오기
                break;
        }
    }

    private void takePhoto(){
        System.out.println("takePhoto start");
        cameraView.capture(new Camera.PictureCallback() {   //캡쳐 이벤트의 콜백함수
            public void onPictureTaken(byte[] data, Camera camera) {//사진 데이터와 카메라 객체
                try {
                    Bitmap bitmaporigin = BitmapFactory.decodeByteArray(data, 0, data.length);//원본 비트맵 파일. 왠지 90도 돌아가 있다

                    //왠지 90도 돌아가서 찍힘. 되돌려놓기. 전면 카메라의 경우 좌우반전(진짜 왤까)
                    Matrix matrix = new Matrix();
                    if (usingCamera == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        float[] mirrorY = {
                                -1, 0, 0,
                                0, 1, 0,
                                0, 0, 1
                        };
                        matrix.setValues(mirrorY);
                    }
                    matrix.postRotate(90);

                    Bitmap bitmap = Bitmap.createBitmap(bitmaporigin, 0, 0,
                            bitmaporigin.getWidth(), bitmaporigin.getHeight(), matrix, true);
                    bitmap = cropBitmap(bitmap); // 사진자르기

                    FileOutputStream fileStream;

                    try {
                        String filePath = "/sdcard/Pictures/image" + ".jpg";
                        File imageFile = new File(filePath);
                        fileStream = new FileOutputStream(imageFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileStream);
                        fileStream.flush();
                        fileStream.close();
                        Log.i("photoSave", filePath);
                    } catch (FileNotFoundException e) {
                        Log.e("Log", e.toString());
                    } catch (IOException e) {
                        Log.e("Log", e.toString());
                    }

                } catch (Exception e) {
                    Log.e("SampleCapture", "Failed to insert image.", e);
                }
                myThread = new MyThread();
                myThread.start();
                try {
                    myThread.join();

                    System.out.println("로딩 화면 끝! ");
                    progressOFF();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("작업 스레드 종료 후...Main 스레드 다시 동작");
                Intent intent = new Intent(getApplicationContext(), DrugInfomation.class);
                intent.putExtra("c_result",c_result);
                startActivity(intent);
            }
        });
        System.out.println("takePhoto end");

    }

    static public Bitmap cropBitmap(Bitmap original) {

        float left = (float)(original.getWidth()*0.15);
        float top = (float)(original.getHeight()*0.20);
        float len = (float)(original.getWidth()/1.5);

        Bitmap result = Bitmap.createBitmap(original
                , (int) left //X 시작위치 (원본의 4/1지점)
                , (int) top //Y 시작위치 (원본의 4/1지점)
                , (int) len // 넓이 (원본의 절반 크기)
                , (int) len); // 높이 (원본의 절반 크기)
        if (result != original) {
            original.recycle();
        }
        return result;
    }

    //앨범에서 이미지 선택
    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    //이미지 파일의 밑바탕 만들기
    private File createImageFile() throws IOException {
        String imageFileName = "mypicture";                    //파일명
        File storageDir = new File(Environment.getExternalStorageDirectory() + cropImageDiretory);//내장메모리/폴더명 에 저장
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();  //절대경로로 URI 작성, 저장
        return image;
    }

    //이미지 크롭 함수.
    //photoUri 의 경로에 있는 사진 파일을 정사각형 모양으로 크롭, 저장하고 photoUri에 경로를 담는다.
    public void cropImage() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + cropImageDiretory);
            File tempFile = new File(folder.toString(), croppedFileName.getName());


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//sdk 24 이상, 누가(7.0)
                photoUri = FileProvider.getUriForFile(getApplicationContext(),// 7.0에서 바뀐 부분은 여기다.
                        BuildConfig.APPLICATION_ID + ".provider", tempFile);
            } else {//sdk 23 이하, 7.0 미만
                photoUri = Uri.fromFile(tempFile);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                grantUriPermission(res.activityInfo.packageName, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_IMAGE);
        }
    }

    //사진 크롭이나 앨범에서 사진 가져오는것의 결과 처리함수.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == PICK_FROM_ALBUM) {//앨범에서 사진 가져오기
            if (data == null) {
                return;
            }
            photoUri = data.getData();
            cropImage();

        } else if (requestCode == CROP_FROM_IMAGE) {//크롭
            imageView.setImageURI(null);//초기화? 필요한 이유를 모르겠다
            imageView.setImageURI(photoUri);//이 photoUri가 크롭된 이미지 파일의 경로

            imageView.setVisibility(View.VISIBLE);
            cameraView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 카메라 미리보기를 위한 서피스뷰 정의
     */
    private class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera camera = null;
        private int usingCamera;


        public CameraSurfaceView(Context context, SurfaceView surfaceView, int cameraFacing) {
            super(context);

            mSurfaceView = surfaceView;

            mSurfaceView.setVisibility(View.VISIBLE);

            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);

            usingCamera = cameraFacing;
        }

        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open(usingCamera);

            try {

                //오토 포커싱. 이거 없으면 초점 안맞음
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(params);

                camera.setDisplayOrientation(90);//왠지 90도 돌아가있음

                camera.setPreviewDisplay(mHolder);
            } catch (Exception e) {
                Log.e("CameraSurfaceView", "Failed to set camera preview.", e);
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        public boolean capture(Camera.PictureCallback handler) {
            if (camera != null) {
                camera.takePicture(null, null, handler);
                return true;
            } else {
                return false;
            }
        }

    }

    class MyThread extends Thread {
        @Override
        public void run() {

            Client c;
            try {
                System.out.println("작업 스레드 start");
                c = new Client("/sdcard/Pictures/image.jpg");
                c_result = c.getResult();

                System.out.println("c_result from server : " + c_result);

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("작업 스레드 end");
        }
    }
}