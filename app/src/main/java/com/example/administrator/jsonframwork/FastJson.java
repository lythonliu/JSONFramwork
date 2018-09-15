package com.example.administrator.jsonframwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Administrator on 2017/2/22 0022.
 */

public class FastJson {
    public static final int JSON_ARRAY=1;

    public static  final int JSON_OBJECT=2;

    public static final int JSON_ERRO=3;
    /**
     * 暴露API  给调用层调用
     * @param json
     * @param clazz
     * @return
     */
    public  static Object pareseObject(String json,Class clazz)
    {
        Object object=null;
        Class<?> jsonClass=null;
        //JSONArray 类型
        if(json.charAt(0)=='[')
        {
            try {
                object=toList(json,clazz);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if(json.charAt(0)=='{')
        {
            try {
                JSONObject jsonObject=new  JSONObject(json);
                //反射得到最外层的model   作为返回值返回  一定要有空的构造方法 User
                object=clazz.newInstance();
                /*
                得到的最外层的key集合

                 */
                Iterator<?> iterator=jsonObject.keys();
                //遍历集合
                while (iterator.hasNext())
                {
                    String key= (String) iterator.next();
                    Object fieldValue=null;
                    //得到当前clazz类型的所有成员变量
                    List<Field>  fields=getAllFields(clazz,null);
                    for (Field field:fields)
                    {
                        //将key和成员变量进行匹配
                        if(field.getName().equalsIgnoreCase(key))
                        {
                              field.setAccessible(true);
                            //得到 key所对应的值   值 可以基本类型  类类型
                            fieldValue=getFieldValue(field,jsonObject,key);
                            if(fieldValue!=null)
                            {
                                field.set(object,fieldValue);
                            }
                            field.setAccessible(false);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * 得到当前的value值
     * @param field
     * @param jsonObject
     * @param key
     * @return
     */
    private static Object getFieldValue(Field field, JSONObject jsonObject, String key) throws JSONException {
        Object fieldValue=null;
        //得到当前成员变量类型
        Class<?> fieldClass=field.getType();
        if(fieldClass.getSimpleName().toString().equals("int")
                ||fieldClass.getSimpleName().toString().equals("Integer"))
        {
             fieldValue=jsonObject.getInt(key);
        }else  if(fieldClass.getSimpleName().toString().equals("String"))
        {
            fieldValue=jsonObject.getString(key);
        }else  if(fieldClass.getSimpleName().toString().equals("double"))
        {
            fieldValue=jsonObject.getDouble(key);
        }else  if(fieldClass.getSimpleName().toString().equals("boolean")
               )
        {
            fieldValue=jsonObject.getBoolean(key);
        }else  if(fieldClass.getSimpleName().toString().equals("long")
               )
        {
            fieldValue=jsonObject.getLong(key);
        }else
        {
            //判断集合类型 和对象类型 jsonValue 代表完整的json字符串  里面一层
            String jsonValue=jsonObject.getString(key);
            switch (getJSONType(jsonValue))
            {
                case JSON_ARRAY:
                    //List<User>
                    Type fieldType=field.getGenericType();
                    if(fieldType instanceof ParameterizedType)
                    {
                        ParameterizedType parameterizedType= (ParameterizedType) fieldType;
                        //List 当前类 所实现的泛型  User
                        Type[]  fieldArgType=parameterizedType.getActualTypeArguments();
                        for(Type type:fieldArgType)
                        {
                            //fieldArgClass  代表着User.class
                            Class<?> fieldArgClass= (Class<?>) type;
                            fieldValue=toList(jsonValue,fieldArgClass);
                        }
                    }
                    break;
                case JSON_OBJECT:
                    //剥下来的json字符串   fieldClass 成员变量类型
                    fieldValue=pareseObject(jsonValue,fieldClass);
                    break;
                case JSON_ERRO:

                    break;
            }

        }
        return fieldValue;
    }

    /**
     * 获取当前json字符串的类型
     *
     * @param jsonValue
     * @return
     */
    private static int getJSONType(String jsonValue) {
        char firstChar=jsonValue.charAt(0);
        if(firstChar=='{')
        {
            return JSON_OBJECT;
        }else if(firstChar=='[')
        {
            return JSON_ARRAY;
        }else
        {
            return JSON_ERRO;
        }
    }

    /**
     * 解析JsonArray数组
     * @param json
     * @param clazz
     * @return
     */
    private static Object toList(String json, Class clazz) throws JSONException {
        List<Object> list=null;
        JSONArray jsonArray=new JSONArray(json);
        list=new ArrayList<>();
        for (int i=0;i<jsonArray.length();i++)
        {
            //拿到JSON字符串
            String jsonValue=jsonArray.getJSONObject(i).toString();
            switch (getJSONType(jsonValue))
            {
                case JSON_ARRAY:
                    //外层JSONArray 嵌套里面JSONArray
                    List<?> infoList=(List<?>) toList(jsonValue,clazz);
                    list.add(infoList);
                    break;
                case JSON_OBJECT:
                     list.add(pareseObject(jsonValue,clazz));
                    break;
                case JSON_ERRO:

                    break;


            }
        }
        return list;
    }

    public static String toJson(Object object)
    {
        StringBuffer jsonBuffer=new StringBuffer();
        if(object instanceof List<?>)
        {
            jsonBuffer.append("[");
            List list= (List) object;
            for (int i=0;i<list.size();i++)
            {
                //解析成JSONObject类型  {"name":"lisi"}
                //把JsonObject类型全部抽取出成一个方法  方便递归调用
                addObjectToJson(jsonBuffer,list.get(i));
                /**
                 * JSONArray数组 最后一个元素后面没有，  所以在这里要判断是不是最后一个元素
                 */
                if(i<list.size()-1)
                {
                    jsonBuffer.append(",");
                }
            }
        }
        else
        {
            addObjectToJson(jsonBuffer,object);
        }
        return jsonBuffer.toString();
    }

    /**
     * 传入的肯定是 类类型
     * @param jsonBuffer
     * @param o
     */
    private static void addObjectToJson(StringBuffer jsonBuffer, Object o) {
        jsonBuffer.append("{");
        List<Field>fields=new ArrayList<>();
        getAllFields(o.getClass() ,fields);
        for (int i=0;i<fields.size();i++)
        {
            Method method=null;
            //反射拿到成员变量的值
            Object filedValue=null;
            Field field=fields.get(i);
            String fieldName=field.getName();
            //拼接时拿到反射的Method对象
            String methodName="get"+((char)(fieldName.charAt(0)-32))+fieldName.substring(1);

            try {
                method=o.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e) {
                methodName="is"+((char)(fieldName.charAt(0)-32))+fieldName.substring(1);

                try {
                    method=o.getClass().getMethod(methodName);
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                }
            }
            //方法不为空
            if(method!=null)
            {
                try {
                    filedValue=method.invoke(o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(filedValue!=null)
            {
                jsonBuffer.append("\"");
                jsonBuffer.append(fieldName);
                jsonBuffer.append("\":");

                //判断modle成员变的类型
                if(filedValue instanceof
                        Integer||filedValue instanceof Long||
                        filedValue instanceof Double||
                        filedValue instanceof  Boolean)
                {
                    jsonBuffer.append(filedValue);
                }else if(filedValue instanceof String)
                {
                    jsonBuffer.append("\"");
                    jsonBuffer.append(filedValue.toString());
                    jsonBuffer.append("\"");
                }else if(filedValue instanceof List<?>)
                {
                    //集合类型
                    addListToBuffer(jsonBuffer,filedValue);
                }else
                {
                    //对象类型  递归调用
                    addObjectToJson(jsonBuffer,filedValue);
                }
                jsonBuffer.append(",");
            }
            if(i==fields.size()-1&&jsonBuffer.charAt(jsonBuffer.length()-1)==',')
            {
                jsonBuffer.deleteCharAt(jsonBuffer.length()-1);
            }
        }
        jsonBuffer.append("}");

    }

    private static void addListToBuffer(StringBuffer jsonBuffer, Object filedValue) {
        jsonBuffer.append("[");
        List<?> list= (List<?>) filedValue;
        for (int i=0;i<list.size();i++)
        {
            addObjectToJson(jsonBuffer,list.get(i));
            if(i<list.size()-1)
            {
                jsonBuffer.append(",");
            }
        }
        jsonBuffer.append("]");
    }

    /**
     * 将当前类类型 的所有成员变量转换成list集合中
     * @param fields
     */
    private static List<Field> getAllFields(Class<?> aClass, List<Field> fields) {
        if(fields==null)
        {
            fields=new ArrayList<>();
        }
        /**
         * 递归时排除Object类型
         */
        if(aClass.getSuperclass()!=null)
        {
            //得到类上的成员变量
            Field[] fieldSelf=aClass.getDeclaredFields();
            for(Field field:fieldSelf)
            {
                //排除final修饰的成员变量  final成员变量表示常量，只能被赋值一次，赋值后值不再改变。
                if(!Modifier.isFinal(field.getModifiers()))
                {
                    fields.add(field);
                }
            }
            //当前类型遍历完成之后 开始遍历父类型成员变量
            getAllFields(aClass.getSuperclass(),fields);

        }
        return fields;
    }

}
