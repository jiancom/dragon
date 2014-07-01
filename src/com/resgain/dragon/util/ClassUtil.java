package com.resgain.dragon.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import org.apache.commons.lang.ClassUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;
import org.mvel2.PropertyAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resgain.dragon.annotation.HtmlParameter;
import com.resgain.dragon.annotation.JsonParameter;
import com.resgain.dragon.util.bean.MethodAndParameter;

public class ClassUtil
{
	 private static Logger logger = LoggerFactory.getLogger(ClassUtil.class);

	static {
		DataConversion.addConversionHandler(Date.class, new ConversionHandler() {
			private SimpleDateFormat sdf;

			@SuppressWarnings("rawtypes")
			public boolean canConvertFrom(Class arg0) {
				if (arg0 == String.class)
					return true;
				return false;
			}

			public Object convertFrom(Object value) {
				if (value == null || value.toString().trim().length() < 1)
					return null;
				if (value.toString().trim().length() > 10)
					sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				else
					sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					return sdf.parse((String) value);
				} catch (ParseException e) {
					logger.error("日期格式错误", e);
				}
				return null;
			}

		});
	}

	public static MethodAndParameter getMethodParamNames(String c, String m, String method) throws NotFoundException, MissingLVException, ClassNotFoundException {
		ClassPool pool = ClassPool.getDefault();
		ClassClassPath classPath = new ClassClassPath(ClassUtil.class);
		pool.insertClassPath(classPath);
		CtClass cc = pool.get(c);
		CtMethod cm = null;
		CtMethod cms[] = cc.getDeclaredMethods();
		for (CtMethod ctm : cms) {
			if (ctm.getName().equals(m)) {
				// Get get = (Get) ctm.getAnnotation(Get.class);
				cm = ctm;
				break;
			}
		}
		if (cm == null)
			return null;

		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
		if (attr == null)
			throw new MissingLVException(cc.getName());
		Map<String, String> ret = new LinkedHashMap<String, String>();
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;

		for (int i = 0; i < cm.getParameterTypes().length; i++) {
			ret.put(attr.variableName(i + pos), cm.getParameterTypes()[i].getName());
		}
		return new MethodAndParameter(cm, ret);
	}

	/**
	 * 在class中未找到局部变量表信息<br>
	 * 使用编译器选项 javac -g:{vars}来编译源文件
	 * @author gyl
	 */
	@SuppressWarnings("serial")
	private static class MissingLVException extends Exception
	{
		static String msg = "class:%s 不包含局部变量表信息，请使用编译器选项 javac -g:{vars}来编译源文件。";

		public MissingLVException(String clazzName) {
			super(String.format(msg, clazzName));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object getValue(String name, String type, Map<String, Object> context, Annotation ans[]) throws Exception {

		if (inObjectProp(name, context)) {
			Class c = ClassUtils.getClass(type);
			Object o = c.newInstance();
			for (Field field : ClassUtil.getAllFields(c)) {
				String k = name + "." + field.getName();
				Object v = context.get(k);
				if (v == null || v.toString().length() < 1)
					continue;
				setPropValue(o, field, field.getName(), v);
			}
			return o;
		}

		Object value = context.get(name);
		if (value == null || value.toString().length() < 1)
			value = null;
		if ("int".equals(type)) {
			return value == null ? 0 : Integer.valueOf(value.toString()).intValue();
		} else if ("short".equals(type)) {
			return value == null ? 0 : Short.valueOf(value.toString()).shortValue();
		} else if ("long".equals(type)) {
			return value == null ? 0 : Long.valueOf(value.toString()).longValue();
		} else if ("byte".equals(type)) {
			return value == null ? 0 : Byte.valueOf(value.toString()).byteValue();
		} else if ("float".equals(type)) {
			return value == null ? 0.0 : Float.valueOf(value.toString()).floatValue();
		} else if ("double".equals(type)) {
			return value == null ? 0.0 : Double.valueOf(value.toString()).doubleValue();
		} else if ("char".equals(type)) {
			return value == null ? 0 : value.toString().charAt(0);
		} else if ("boolean".equals(type)) {
			return value == null ? false : Boolean.valueOf(value.toString()).booleanValue();
		} else if ("com.resgain.dragon.util.bean.UploadBean".equals(type) || "com.resgain.dragon.util.bean.UploadBean[]".equals(type)) {
			return value == null ? false : value;
		} else if(ans!=null && ans.length==1 && (ans[0] instanceof HtmlParameter || ans[0] instanceof JsonParameter)){ //目前只支持2个注解 FIXME 一个参数可以有多个注解
			if(ans[0] instanceof HtmlParameter){
				return value == null ? null : getValue(value.toString(), (HtmlParameter)ans[0]);
			} else if(ans[0] instanceof JsonParameter){
				return value == null ? null : JSONUtil.toObject(ClassUtils.getClass(type), (String)value);
			} else {
				return value;
			}
		} else {
			return value == null ? null : DataConversion.convert(value, ClassUtils.getClass(type));
		}
	}

	private static boolean inObjectProp(String name, Map<String, Object> context) {
		if(!context.containsKey(name)) {
			String prefix = name + ".";
			for (Entry<String, Object> entry : context.entrySet()) {
				if(entry.getKey().startsWith(prefix))
					return true;
			}
		}
		return false;
	}

    public static void setPropValue(Object obj, Field field, String key, Object value)
    {
        try {
        	if(field!=null && field.getType().isAssignableFrom(String.class) && value!=null && (value.toString().indexOf('<')>=0 || value.toString().indexOf('>')>=0))
        	{
        		if(field.isAnnotationPresent(HtmlParameter.class))
        		{
        			HtmlParameter hp = field.getAnnotation(HtmlParameter.class);
        			PropertyAccessor.set(obj, key, getValue(value.toString(), hp));
        		} else {
        			String v = Jsoup.clean(value.toString(), Whitelist.none());
        			PropertyAccessor.set(obj, key, v);
        		}
        	} else {
				if ("serialVersionUID".equals(key))
					return;
        		if(field.isAnnotationPresent(JsonParameter.class) && value instanceof String)
        		{
        			PropertyAccessor.set(obj, key, JSONUtil.toObject(field.getType(), (String)value));
        		} else {
        			PropertyAccessor.set(obj, key, value);
        		}
        	}

        } catch (Exception e) {
            logger.warn("BEAN({})设置属性值发生错误:{}={},原因:{}", new Object[] { obj.getClass().getName(), key, value, e.getMessage() });
        }
    }

    private static String getValue(String value, HtmlParameter hp){
		if (HtmlParameter.LEVEL.Free.equals(hp.value()))
			return value;
		else if (HtmlParameter.LEVEL.Normal.equals(hp.value()))
			return Jsoup.clean(value, Whitelist.basic().addTags("h3","h4","h5","h6"));
		else
			return Jsoup.clean(value, Whitelist.simpleText());
    }

	private static Map<String, Map<String, Field>> classFieldCache = new Hashtable<String, Map<String, Field>>();

	/**
	 * 取得指定Class的属性信息
	 * @param clazz
	 * @param flag
	 *            如果是true则map里存储的key转为小写
	 * @return
	 */
	public static Map<String, Field> getFields(Class<?> clazz, boolean flag) {
		String key = clazz.getName() + "-" + flag;
		if (!classFieldCache.containsKey(key)) {
			Field fields[] = getAllFields(clazz).toArray(new Field[0]);
			Map<String, Field> fieldCache = new HashMap<String, Field>();
			for (int i = 0; i < fields.length; i++)
				fieldCache.put(flag ? fields[i].getName().toLowerCase() : fields[i].getName(), fields[i]);
			classFieldCache.put(key, fieldCache);
		}
		return classFieldCache.get(key);
	}

	public static Field getField(Class<?> clazz, String key) {
		return getFields(clazz, false).get(key);
	}

	private static List<Field> getAllFields(Class<?> clazz) {
		List<Field> ret = new ArrayList<Field>();
		if (clazz != Object.class) {
			ret.addAll(getAllFields(clazz.getSuperclass()));
			ret.addAll(Arrays.asList(clazz.getDeclaredFields()));
		}
		return ret;
	}
}
