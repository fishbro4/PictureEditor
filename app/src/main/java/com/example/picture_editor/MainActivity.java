package com.example.picture_editor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;




public class MainActivity extends AppCompatActivity{

    private Bitmap bitmap;
    private Bitmap newBitmap;
    private Bitmap bitmap2;
    private ImageView imageView;
    private static int choose_image=1;
    private static int joint_image=2;
    //private static int cut_image=3;
    private View containerView;
    private TextView textView;
    private float imageWidth, imageHeight, imagePositionX, imagePositionY;//view的尺寸

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.imageView);
        containerView=findViewById(R.id.writeText);
        textView=findViewById(R.id.writeText_tv);
//        button=findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, choose_image);
//            }
//        });
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                imagePositionX = imageView.getX();
                imagePositionY = imageView.getY();
                imageWidth = imageView.getWidth();
                imageHeight = imageView.getHeight();
                //设置文本大小
                textView.setMaxWidth((int)imageWidth);
            }
        });
        imageView.setImageBitmap(bitmap);
        //移动
        final GestureDetector gestureDetector = new GestureDetector(this,new SimpleGestureListenerImpl());
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.item_load:
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, choose_image);
                return true;
            case R.id.item_save:
                saveImage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if ((requestCode == choose_image||requestCode == joint_image )&& resultCode == Activity.RESULT_OK && data != null) {
            Uri chooseImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(chooseImage, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String path = c.getString(columnIndex);
            /**根据不同的请求返回不同的函数**/
            switch (requestCode){
                case 1:
                    try {
                        loadImage(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    loadNewImage(path);
                    break;
            }
            c.close();
        }
    }

    private void loadImage(String path) throws IOException {
        if(path != null){
            //获取图片头信息
            ExifInterface exifInterface=new ExifInterface(path);
            //获取图片的宽度
            int width=exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH,0);
            int heigth=exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH,0);
            //获取屏幕的宽高
            Point windowSize=new Point();
            WindowManager windowManager=(WindowManager)getSystemService(WINDOW_SERVICE);
            int winWidth=windowManager.getDefaultDisplay().getWidth();
            int winheigth=windowManager.getDefaultDisplay().getHeight();
            int widthPercent=width/winWidth;
            int heigthPercent=heigth/winheigth;
            //加载图片到内存并显示到控件上
            BitmapFactory.Options options=new BitmapFactory.Options();
            if(widthPercent>heigthPercent){
                options.inSampleSize=widthPercent;
            }else{
                options.inSampleSize=heigthPercent;
            }
            bitmap=BitmapFactory.decodeFile(path,options);
            imageView.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this,"加载图片失败",Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNewImage(String path){
        if (path!=null){
            bitmap2=BitmapFactory.decodeFile(path);
        }else {
            Toast.makeText(this,"选择图片失败",Toast.LENGTH_SHORT).show();
        }
    }

//    public void jointDialog(){
//        final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
//        final View view= LayoutInflater.from(MainActivity.this).inflate(R.layout.waytojoint,null);
//        Button land=view.findViewById(R.id.land);
//        Button port=view.findViewById(R.id.port);
//        land.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                jointByLand(bitmap,bitmap2);
//                imageView.setImageBitmap(newBitmap);
//                bitmap=newBitmap;
//            }
//        });
//        port.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                jointByPort(bitmap,bitmap2);
//                imageView.setImageBitmap(newBitmap);
//                bitmap=newBitmap;
//            }
//        });
//    }

    private void saveImage(){
        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());

        Bitmap save_bm = loadBitmapFromView(containerView);
        try {
            OutputStream os = getContentResolver().openOutputStream(imageUri);
            //compress方法将图片转换成JPG或者PNG格式
            save_bm.compress(Bitmap.CompressFormat.JPEG, 90, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "保存：" + imageUri.toString(), Toast.LENGTH_LONG).show();
    }

    public void writeImage(View view){
         addTextDialog();
    }

    public void addTextDialog(){
        final AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        final View view= LayoutInflater.from(MainActivity.this).inflate(R.layout.addtext,null);
        final EditText editText=view.findViewById(R.id.editView);
        Button add=view.findViewById(R.id.add);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().equals("")) {
                    textView.setVisibility(View.INVISIBLE);
                } else {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(charSequence);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"添加文字",Toast.LENGTH_LONG).show();
            }
        });
        builder.setView(view).show();
    }

    //以图片形式获取View显示的内容（类似于截图）
    public static Bitmap loadBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private int count = 0;
    //textView的x方向和y方向移动量
    private float mDx, mDy;

    //移动
    private class SimpleGestureListenerImpl extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //向右移动时，distanceX为负；向左移动时，distanceX为正
            //向下移动时，distanceY为负；向上移动时，distanceY为正
            count++;
            mDx -= distanceX;
            mDy -= distanceY;
            //边界检查
            mDx = calPosition(imagePositionX - textView.getX(), imagePositionX + imageWidth - (textView.getX() + textView.getWidth()), mDx);
            mDy = calPosition(imagePositionY - textView.getY(), imagePositionY + imageHeight - (textView.getY() + textView.getHeight()), mDy);
            //控制刷新频率
            if (count % 5 == 0) {
                textView.setX(textView.getX() + mDx);
                textView.setY(textView.getY() + mDy);
            }
            return true;
        }
    }

    //计算正确的显示位置（不能超出边界）
    private float calPosition(float min, float max, float current) {
        if (current < min) {
            return min;
        }
        if (current > max) {
            return max;
        }
        return current;
    }

    public void rotateImage(View view){
        Matrix matrix=new Matrix();
        matrix.postRotate(90);
        newBitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        imageView.setImageBitmap(newBitmap);
        bitmap=newBitmap;
    }


    public void blackImage(View view){
        int w=bitmap.getWidth();
        int h=bitmap.getHeight();
        newBitmap=Bitmap.createBitmap(w,h,Bitmap.Config.RGB_565);
        int color=0;
        int a, r, g, b, r1, g1, b1;
        int[] oldPx = new int[w * h];
        int[] newPx = new int[w * h];

        bitmap.getPixels(oldPx, 0, w, 0, 0, w, h);
        for (int i = 0; i < w * h; i++) {
            color = oldPx[i];
            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);
            a = Color.alpha(color);
            //黑白矩阵
            r1 = (int) (0.33 * r + 0.59 * g + 0.11 * b);
            g1 = (int) (0.33 * r + 0.59 * g + 0.11 * b);
            b1 = (int) (0.33 * r + 0.59 * g + 0.11 * b);
            //检查各像素值是否超出范围
            if (r1 > 255) {
                r1 = 255;
            }
            if (g1 > 255) {
                g1 = 255;
            }
            if (b1 > 255) {
                b1 = 255;
            }
            newPx[i] = Color.argb(a, r1, g1, b1);
        }
        newBitmap.setPixels(newPx, 0, w, 0, 0, w, h);
        imageView.setImageBitmap(newBitmap);
        bitmap=newBitmap;
    }

//    public void WayToJoint2(View view){
//        Intent intent2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent2,joint_image);
//        jointDialog();
//    }

    public void WayToJoint(View view){
        PopupMenu popupMenu=new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.waytojoint,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent intent2= new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);;
                    switch (menuItem.getItemId()){
                    case R.id.landscope:
                        startActivityForResult(intent2,joint_image);
                        if(bitmap2!=null){
                            //startActivityForResult(intent2,joint_image);
                            jointByLand(bitmap,bitmap2);
                            imageView.setImageBitmap(newBitmap);
                            bitmap=newBitmap;
                        }
                        break;
                    case R.id.port:
                        startActivityForResult(intent2,joint_image);
                        if(bitmap2!=null){
                            jointByPort(bitmap,bitmap2);
                            imageView.setImageBitmap(newBitmap);
                            bitmap=newBitmap;
                        }
                        break;
                }
                return true;
            }
        });
        //关闭
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu popupMenu) {

            }
        });
        popupMenu.show();
    }

    public void jointByLand(Bitmap bmp1,Bitmap bmp2){
        int h=Math.min(bmp1.getHeight(),bmp2.getHeight());
        int w=bmp1.getWidth()+bmp2.getWidth();
        newBitmap=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(newBitmap);
        canvas.drawBitmap(bmp1,0,0,null);
        canvas.drawBitmap(bmp2,bmp1.getWidth(),0,null);
    }

    public void jointByPort(Bitmap bmp1,Bitmap bmp2){
        int w=Math.min(bmp1.getWidth(),bmp2.getWidth());
        int h=bmp1.getHeight()+bmp2.getHeight();
        newBitmap=Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(newBitmap);
        canvas.drawBitmap(bmp1,0,0,null);
        canvas.drawBitmap(bmp2,0,bmp1.getHeight(),null);
    }
//裁剪功能：有问题
//    public void cutImage(){
//        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        intent.putExtra("crop", "true");
//        //width:height
//        intent.putExtra("aspectX", 1);
//        intent.putExtra("aspectY", 1);
//        intent.putExtra("output", imageUri);
//        intent.putExtra("outputFormat", "JPEG");
//        startActivityForResult(Intent.createChooser(intent, "Choose Image"), SELECT_IMAGE_CROP);
//        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(intent,cut_image);
//    }
}
