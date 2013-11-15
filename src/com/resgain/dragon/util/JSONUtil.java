package com.resgain.dragon.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JSONUtil
{
    public static String toJson(Object obj)
    {
		return JSON.toJSONString(obj, SerializerFeature.DisableCircularReferenceDetect);
		// return JSON.toJSONString(obj);
    }

    public static <T> T toObject(Class<T> clazz, String json)
    {
        return JSON.parseObject(json, clazz);
    }
}
