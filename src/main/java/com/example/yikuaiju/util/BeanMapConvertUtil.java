package com.example.yikuaiju.util;

import org.thymeleaf.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** 
 * @author  lifei 	
 * @date 创建时间：2017年10月20日 下午4:21:54 
 * @version 1.0 
 * @Description TODO
 */

/**
 * @author lifei
 *
 */
public class BeanMapConvertUtil {

    /**
     * bean 转化为实体
     * 
     * @param bean
     * @return
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static HashMap<String, Object> beanToMap(Object bean)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (null == bean) {
            return map;
        }
        Class<?> clazz = bean.getClass();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : descriptors) {
            String propertyName = descriptor.getName();
            if (!"class".equals(propertyName)) {
                Method method = descriptor.getReadMethod();
                Object result;
                result = method.invoke(bean);
                if (null != result && !"".equals(result)) {
                    map.put(propertyName, result);
                }
            }
        }

        return map;
    }

    /**
     * map 转化为 bean
     * 
     * @param clazz
     * @param map
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    public static Object mapToBean(Class<?> clazz, Map<String, Object> map)
            throws InstantiationException, IllegalAccessException,
            IntrospectionException, IllegalArgumentException,
            InvocationTargetException {
        Object object = null;
        if(map != null && map.size()>0){
            object = clazz.newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                String propertyName = descriptor.getName().toUpperCase();
                if (map.containsKey(propertyName)) {
                    Object value = map.get(propertyName);
                    Object[] args = new Object[1];
                    args[0] = value;
                    descriptor.getWriteMethod().invoke(object, args);
                }
            }
        }
        return object;
    }

    public static List<Map<String, Object>> mapListToLowerCase(
            List<Map<String, Object>> list) throws UnsupportedEncodingException {
        List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>(
                list.size());
        for (Map<String, Object> map : list) {
            Map<String, Object> newMap = mapToLowerCase(map);
            newList.add(newMap);
        }
        return newList;
    }
    
    public static Map<String, Object> mapToLowerCase(Map<String, Object> map) throws UnsupportedEncodingException {
        if(map != null){
            Map<String, Object> newMap = new HashMap<String, Object>(map.size());
            Iterator<Entry<String, Object>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, Object> entry = iter.next();
                String key = entry.getKey().toLowerCase();
                Object value = entry.getValue();
                newMap.put(key, value);
            }
            return newMap;
        }else
            return null;
    }

    public static <T> List<Map<String, Object>> beanListToMapList(List<T> beanList)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (int i = 0, n = beanList.size(); i < n; i++)
        {
            Object bean = beanList.get(i);
            Map<String, Object> map = beanToMap(bean);
            mapList.add(map);
        }
        return mapList;
    }

    public static Object mapListToBeanList(Class<?> clazz,
            List<Map<String, Object>> mapList) throws InstantiationException,
            IllegalAccessException, IntrospectionException,
            IllegalArgumentException, InvocationTargetException {
        List<Class<?>> beanList = new ArrayList<Class<?>>();
        for (Map<String, Object> map : mapList) {
            beanList.add((Class<?>) mapToBean(clazz, map));
        }

        return beanList;
    }

}
