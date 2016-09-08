package cn.lessask.word.word.util;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import cn.lessask.word.word.model.DownImageAsync;

/**
 * Created by huangji on 2016/1/4.
 */
public class ImageUtil {
    private static String TAG = ImageUtil.class.getSimpleName();
    private static int screenWidth=90;
    private static int screenHeight=90;

    /*
    * 如果加载原图非常容易内存溢出
    * */
    public static Bitmap getBitmapFromFile(File file) {
        Bitmap bitmap = null;
        try {
            /*
            BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
            int width = opts.outWidth;
            int height = opts.outHeight;

            opts.inJustDecodeBounds = false;
            */

            BitmapFactory.Options opts=new BitmapFactory.Options();
            /*
            opts.inJustDecodeBounds = true;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inTempStorage = new byte[100 * 1024];

            opts.inSampleSize = 2;
            */
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }
    public static Bitmap getOptimizeBitmapFromFile(File file) {
        return getOptimizeBitmapFromFile(file.getAbsolutePath(), screenWidth, screenHeight);
    }
    public static Bitmap getOptimizeBitmapFromFile(File file, int maxWidth, int maxHeight) {
        return getOptimizeBitmapFromFile(file.getAbsolutePath(), maxWidth, maxHeight);
    }
    public static Bitmap getOptimizeBitmapFromFile(String pathName) {
        return getOptimizeBitmapFromFile(pathName, screenWidth, screenHeight);
    }
    public static Bitmap getOptimizeBitmapFromFile(Bitmap bitmap) {
        return comp(bitmap);
    }
    /*
    * 限制大小获取bitmap
    * */
    public static Bitmap getOptimizeBitmapFromFile(String pathName, int maxWidth, int maxHeight) {
        File file = new File(pathName);
        if(!file.exists() || !file.isFile()) {
            throw new Resources.NotFoundException();
        }
        Log.e(TAG, "w:" + maxWidth + ", h:" + maxHeight);
		Bitmap result = null;
		try {
            // 图片配置对象，该对象可以配置图片加载的像素获取个数
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 表示加载图像的原始宽高
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pathName, options);
            double calSize = 2*options.outWidth*options.outHeight/1024.0;
            Log.e(TAG, "calSize:"+calSize+"Kb");

            // Math.ceil表示获取与它最近的整数（向上取值 如：4.1->5 4.9->5）
            int widthRatio = (int) Math.ceil(((float)options.outWidth) / maxWidth);
            int heightRatio = (int) Math.ceil(((float)options.outHeight) / maxHeight);
            // 设置最终加载的像素比例，表示最终显示的像素个数为总个数的
            if (widthRatio > 1 || heightRatio > 1) {
                if (widthRatio > heightRatio) {
                    options.inSampleSize = widthRatio;
                } else {
                    options.inSampleSize = heightRatio;
                }
            }
            Log.e(TAG, "inSampleSize:"+options.inSampleSize);
            // 解码像素的模式，在该模式下可以直接按照option的配置取出像素点
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = false;
            result = BitmapFactory.decodeFile(pathName, options);
            double calOptiSize = 2*options.outWidth*options.outHeight/1024.0;
            Log.e(TAG, "opti calSize:"+calOptiSize+"Kb");
            Log.e(TAG, "bit rate:"+calSize/calOptiSize);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
		return result;
	}

    public static InputStream getOptimizeBitmapInputStream(File file) throws IOException{
		long fileSize = file .length();
        if(!file.exists()){
            throw new IOException("file "+file.getAbsolutePath() +" is not exists");
        }
        Log.e(TAG, "scal compress before:"+fileSize);
		final long fileMaxSize = 100 * 1024;
        Bitmap bitmap=null;
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
		if (fileSize >= fileMaxSize) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            int height = options.outHeight;
            int width = options.outWidth;

            double scale = Math.sqrt((float) fileSize / fileMaxSize);
            options.outHeight = (int) (height / scale);
            options.outWidth = (int) (width / scale);
            options.inSampleSize = (int) (scale + 0.5);
            Log.e(TAG, "scal outWidth:"+options.inSampleSize);
            options.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            if(!bitmap.isRecycled()){
                bitmap.recycle();
            }
            //Log.e(TAG, "scal compress after: " + fos.toByteArray().length/1024);
        }else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
        }
		return new BufferedInputStream(new ByteArrayInputStream(fos.toByteArray()));
	}

    private static Bitmap compressImageBySize(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;

        while ( baos.toByteArray().length / 1024>100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    private static Bitmap comp(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int size = baos.toByteArray().length / 1024;
        Log.e(TAG, "before size:"+size);
        while (baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
            size = baos.toByteArray().length / 1024;
            Log.e(TAG, "after size:"+size);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImageBySize(bitmap);//压缩好比例大小后再进行质量压缩
    }

    public static BitmapFactory.Options getImageSize(String pathName) {
        File file = new File(pathName);
        if (!file.exists() || !file.isFile()) {
            throw new Resources.NotFoundException();
        }
        Bitmap result = null;
        // 图片配置对象，该对象可以配置图片加载的像素获取个数
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 表示加载图像的原始宽高
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        return  options;
    }

    public static void setBitmap2File(File file,Bitmap mBitmap) throws IOException{
        setBitmap2File(file, mBitmap, 80);
    }
    /*
    * 将压缩bitmap到文件
    * */
    public static void setBitmap2File(File file,Bitmap mBitmap, int quality) throws IOException{
        file.createNewFile();
        FileOutputStream fOut = null;
        fOut = new FileOutputStream(file);

        mBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
        fOut.flush();
        fOut.close();
    }

    public static Bitmap getThumbnail(File originFile,ContentResolver cr,int width, int height){

        //获取缩略图
        //获取原图id
        String columns[] = new String[] { MediaStore.Images.Media._ID};
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, "_data=?", new String[]{originFile.getAbsolutePath()}, null);
        int originImgId = 0;
        if(cursor.moveToFirst()) {
            originImgId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        }
        cursor.close();
        //根据原图id查找缩略图
        String[] projection = { MediaStore.Images.Thumbnails.DATA};
        cursor = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, "image_id=?", new String[]{originImgId+""}, null);
        String thumbnailPath = "";
        String thumbData = "";
        Bitmap thumbnailBitmap = null;
        if(cursor.moveToFirst()) {
            thumbnailPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            thumbnailBitmap = getBitmapFromFile(new File(thumbnailPath));
        }
        if(thumbnailBitmap==null){
            //不存在缩略图,自己进行压缩
            //Log.e(TAG, "originFile:"+originFile.getAbsolutePath());
            thumbnailBitmap = getOptimizeBitmapFromFile(originFile.getAbsolutePath(), width, height);
        }
        long oSize = originFile.length()/1024;
        int aSize = thumbnailBitmap.getByteCount()/1024;
        float rate = 1f*(oSize-aSize)/oSize;
        //Log.e(TAG, "getThumbnail:"+oSize+", compressSize:"+aSize+", rate:"+rate);
        cursor.close();
        return thumbnailBitmap;
    }
    public static Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            //bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            bitmap = BitmapFactory.decodeFile(uri.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }
    public static Bitmap getImgFromLonowOrNet(File file, String url, ImageView view) {
        Bitmap bitmap = null;

        if (file.exists()) {
            bitmap = getBitmapFromFile(file);
            if (bitmap != null) {
                view.setImageBitmap(bitmap);
            }
        } else {
            new DownImageAsync(url, view).execute();
        }

        return bitmap;
    }
    public static int getImageMainColor(String path){
        Bitmap bitmap = ImageUtil.getOptimizeBitmapFromFile(new File(path));
        Palette palette = Palette.from(bitmap).generate();
        int color = palette.getMutedColor(0x000000);
        if(color==0){
            color = palette.getLightMutedColor(0x000000);
            if(color==0){
                color = palette.getDarkMutedColor(0x000000);
                if(color==0){
                    color  = palette.getVibrantColor(0x000000);
                    if(color==0){
                        color = palette.getLightVibrantColor(0x000000);
                        if(color==0){
                            color = palette.getDarkVibrantColor(0x000000);
                        }
                    }
                }
            }
        }
        return color;
    }
    public static String getImageUrlWithWH(String url,int w,int h){
        StringBuilder builder = new StringBuilder(url);
        builder.append("!");
        builder.append(w);
        builder.append("_");
        builder.append(h);
        return builder.toString();
    }

    //矩形a根据矩形b的大小进行自动适配
    public static ArrayList<Integer> getRecAFitB(int aWidth,int aHeight,int bWidth,int bHeight){
        ArrayList<Integer> fitSize = new ArrayList<>();
        float wrate = bWidth/(float)aWidth;
        float hrate = bHeight/(float)aHeight;
        if(aWidth>bWidth && aHeight<=bHeight){
            fitSize.add(bWidth);
            fitSize.add((int)(wrate*aHeight));
        }else if(aWidth<=bWidth && aHeight>bHeight){
            fitSize.add((int)(hrate*aWidth));
            fitSize.add(bHeight);
        }else if(aWidth>bWidth && aHeight>bHeight){
            if(wrate>hrate){
                fitSize.add((int)(hrate*aWidth));
                fitSize.add(bHeight);
            }else if(wrate<hrate){
                fitSize.add(bWidth);
                fitSize.add((int)(wrate*aHeight));
            }else {
                fitSize.add(bWidth);
                fitSize.add(bHeight);
            }
        }else {
            fitSize.add(aWidth);
            fitSize.add(aHeight);
        }
        return fitSize;
    }
}
