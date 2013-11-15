package com.resgain.dragon.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.resgain.dragon.util.bean.UploadBean;


@SuppressWarnings("unchecked")
public class UploadUtil
{
    private static final long TOTAL_SIZE_MAX = 0x9c4000L;
    private static final long EACH_SIZE_MAX = 0x4e2000L;
    
    private UploadUtil() {}

    public static Map<String, Object> getParameter(HttpServletRequest request)
    {
        try {
            return getParameter(request, TOTAL_SIZE_MAX, EACH_SIZE_MAX);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Map<String, Object> getParameter(HttpServletRequest request, long totalSizeMax) throws FileUploadException, IOException
    {
        return getParameter(request, totalSizeMax, EACH_SIZE_MAX);
    }

    public static Map<String, Object> getParameter(HttpServletRequest request, long totalSizeMax, long eachSizeMax) throws FileUploadException, IOException
    {
        Map<String, Object> uftHash = new HashMap<String, Object>();

        if(ServletFileUpload.isMultipartContent(request))
        {
        	FileItemFactory factory = new DiskFileItemFactory();
        	ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(totalSizeMax);
            List<?> items = upload.parseRequest(request);
            String charset = request.getCharacterEncoding();
            
            for(Iterator<?> iter = items.iterator(); iter.hasNext();)
            {
                FileItem item = (FileItem)iter.next();
                String key = item.getFieldName();
                if(item.isFormField())
                {
                	if (charset != null)
                		putMap(uftHash, key, item.getString(charset));
                	else
                		putMap(uftHash, key, item.getString());
                } else
                {
                    if(item.getSize() <= eachSizeMax)
                    	putMap(uftHash, key, new UploadBean(key, item.getName(), item.getContentType(), item.getSize(), item.getInputStream()));
                    else
                    	throw new FileUploadException(key+":"+item.getName()+"文件大小超出设定的范围，限制单个文件大小为"+eachSizeMax+"个字节");
                }
            	
            }
        } else {        	
        	for(@SuppressWarnings("rawtypes") Enumeration s = request.getParameterNames(); s.hasMoreElements(); )
        	{
        		String key = s.nextElement().toString();
        		for (String v : request.getParameterValues(key)) {
        			putMap(uftHash, key, v);
				}
        	}
        }
        
        for(@SuppressWarnings("rawtypes") Enumeration s = request.getAttributeNames(); s.hasMoreElements(); )
    	{
    		String key = s.nextElement().toString();
    		uftHash.put(key, request.getAttribute(key));
    	}
            
		uftHash.put("webapp", request.getContextPath());
		uftHash.put("request", request);
		uftHash.put("SERVER", request.getServerName()+(request.getServerPort()==80?"":(":"+request.getServerPort())));

        return uftHash;
    }
    
    private static <T extends Object> void putMap(Map<String, Object> m, String key, T value)
    { 
    	if(value==null)
    		return;
    	if(m.containsKey(key))
    	{
    		Object[] n = null;
    		if(m.get(key) instanceof Object[])
    		{
    			T[] o = (T[])m.get(key);
    			n = (T[])Array.newInstance(value.getClass(), o.length+1);
    			System.arraycopy(o, 0, n, 0, o.length);
    			n[o.length]=value;    			
    		} else {
    			n = (T[])Array.newInstance(value.getClass(), 2);
    			n[0]=m.get(key);
    			n[1]=value;
    		}    		
    		m.put(key, n);
    	} else m.put(key, value);
    }
}