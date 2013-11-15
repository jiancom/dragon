/*
 *      Copyright (C) 2003 memphis.guo
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation.
 *                                          Created on 2000-5-26
 */
package com.resgain.dragon.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedHashMap;

/**
 * 读取配置文件的JAVA类
 * @author (memphis.guo)
 * @version (V.0.6)
 * @modified 1999/08/02
 */
public final class IniUtil
{
	public static Hashtable<String, LinkedHashMap<String, String>> getTitle(InputStream... iss) {
		Hashtable<String, LinkedHashMap<String, String>> ret = new Hashtable<String, LinkedHashMap<String, String>>();
		try {
			for (int x = 0; x < iss.length; x++) {
				InputStream is = iss[x];
				LinkedHashMap<String, String> htconf = new LinkedHashMap<String, String>();
				String title = "";
				boolean flag = false;
				BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String s1;
				while ((s1 = bufferedreader.readLine()) != null) {
					s1 = s1.replaceAll("\\s", "");
					if (s1.length() > 0) {
						if (s1.startsWith("[") && s1.endsWith("]")) {
							if (flag)
								ret.put(title, htconf);
							title = s1.substring(1, s1.length() - 1);
							htconf = ret.containsKey(title) ? ret.get(title) : new LinkedHashMap<String, String>();
							flag = true;
						} else {
							int i = s1.indexOf("=");
							if (i > 0 && i < s1.length() - 1 && s1.charAt(0) != '#' && !s1.startsWith("//"))
							{
								String key = s1.substring(0, i).replaceAll("\\s", "");
								String value = s1.substring(i + 1).trim();
								htconf.put(key, value);
							}
						}
					}
				}
				ret.put(title, htconf);
				bufferedreader.close();
			}
		} catch (Exception _ex) {
			_ex.fillInStackTrace();
		}
		return ret;
	}
}
