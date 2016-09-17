package cn.lessask.word.net;


import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class PostSingleImage {

    private final String TAG = PostSingleImage.class.getSimpleName();
    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();
    /**
     * 换行符
     */
    private final String NEW_LINE_STR = "\r\n";
    private final String CONTENT_TYPE = "Content-Type: ";
    private final String CONTENT_DISPOSITION = "Content-Disposition: ";
    /**
     * 文本参数和字符集
     */
    private final String TYPE_TEXT_CHARSET = "text/plain; charset=UTF-8";

    /**
     * 字节流参数
     */
    private final String TYPE_OCTET_STREAM = "application/octet-stream";
    /**
     * 二进制参数
     */
    private final byte[] BINARY_ENCODING = "Content-Transfer-Encoding: binary\r\n\r\n".getBytes();
    /**
     * 文本参数
     */
    private final byte[] BIT_ENCODING = "Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes();

    /**
     * 分隔符
     */
    private String mBoundary = null;

    private String url;
    private BufferedOutputStream netOutput;
    private HttpURLConnection con;

    public PostSingleImage(String url) throws Exception{
        this.mBoundary = generateBoundary();
        this.url = url;
        Log.e(TAG, "url:"+url);
        initNet();
    }

    public void initNet() throws Exception{
        URL url = new URL(this.url);
        con = (HttpURLConnection) url.openConnection();
        // 发送POST请求必须设置如下两行
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestMethod("POST");
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Charset", "UTF-8");
        con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + mBoundary);
        netOutput= new BufferedOutputStream(con.getOutputStream());
        Log.e(TAG, "initNet");
    }

    /**
     * 生成分隔符
     */
    private String generateBoundary() {
        StringBuilder buf = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buf.toString();
    }

    /**
     * 参数开头的分隔符
     *
     * @throws IOException
     */
    private void writeFirstBoundary() throws IOException {
        netOutput.write(("--" + mBoundary + "\r\n").getBytes());
    }


    public void addStringPart(final String paramName, final String value) throws Exception{
        Log.e(TAG, "addStringPart:"+ paramName+", "+value);
        writeToOutputStream(paramName, value.getBytes(), TYPE_TEXT_CHARSET, BIT_ENCODING, "");
    }

    /**
     * 将数据写入到输出流中
     *
     */
    private void writeToOutputStream(String paramName, byte[] rawData, String type,
                                     byte[] encodingBytes,
                                     String fileName)throws Exception{
        writeFirstBoundary();
        byte[] types = (CONTENT_TYPE + type + NEW_LINE_STR).getBytes();
        netOutput.write(types, 0, type.length());
        netOutput.write(getContentDisposition(paramName, fileName));
        netOutput.write(encodingBytes);
        netOutput.write(rawData);
        byte[] newLine = NEW_LINE_STR.getBytes();
        netOutput.write(newLine, 0, newLine.length);
    }

    /**
     * 添加二进制参数, 例如Bitmap的字节流参数
     */
    public void addBinaryPart(String paramName, final byte[] rawData) throws Exception{
        writeToOutputStream(paramName, rawData, TYPE_OCTET_STREAM, BINARY_ENCODING, "no-file");
    }

    /**
     * 添加文件参数,可以实现文件上传功能
     */
    public void addFilePart(final String key, final File file) throws Exception{
        Log.e(TAG, "addFilePart:" + file.getName());
        BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
        writeFirstBoundary();
        //Content-Type
        netOutput.write((CONTENT_TYPE + TYPE_OCTET_STREAM + NEW_LINE_STR).getBytes());
        //Content-Disposition
        netOutput.write(getContentDisposition(key, file.getName()));
        //Content-Transfer-Encoding
        netOutput.write(BINARY_ENCODING);

        byte[] tmp = new byte[4096];
        int len = 0;
        int totalLen = 0;
        while ((len = fin.read(tmp)) != -1) {
            netOutput.write(tmp, 0, len);
            totalLen+=len;
        }
        fin.close();
        Log.e(TAG, "totalLen:"+totalLen);
        netOutput.write(NEW_LINE_STR.getBytes());
        netOutput.flush();
    }

    private byte[] getContentDisposition(String paramName, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CONTENT_DISPOSITION + "form-data; name=\"" + paramName + "\"");
        // 文本参数没有filename参数,设置为空即可
        if (!TextUtils.isEmpty(fileName)) {
            stringBuilder.append("; filename=\""
                    + fileName + "\"");
        }
        stringBuilder.append(NEW_LINE_STR);

        return stringBuilder.toString().getBytes();
    }

    //最后一定要调用这个方法
    public void end() throws IOException {
        // 参数最末尾的结束符
        // 写入结束符
        netOutput.write(("--" + mBoundary + "--\r\n").getBytes());
        netOutput.flush();
        netOutput.close();
        Log.e(TAG, "end");
        // 定义BufferedReader输入流来读取URL的响应
        int res = con.getResponseCode();
        if(res == 200){
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                con.getInputStream()));
            String line = null;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            Log.e(TAG, "post end:"+builder.toString());
        }else if(res == 413) {
            Log.e(TAG, "请求体过大");
        }else {
            Log.e(TAG, "post code:"+res);
        }

    }
}