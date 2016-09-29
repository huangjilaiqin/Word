package cn.lessask.word.util;

import java.util.ArrayList;

/**
 * Created by laiqin on 16/9/29.
 */
public class StringUtil {
    public static String join(ArrayList array,String connect){
        StringBuilder builder = new StringBuilder();
        for(int i=0,size=array.size(),last=size-1;i<size;i++){
            builder.append(array.get(i));
            if(i<last){
                builder.append(connect);
            }
        }
        return builder.toString();
    }
}
