package com.liangmayong.apidesigner;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.liangmayong.apidesigner.entity.APIParameter;
import com.liangmayong.apidesigner.entity.APIResponse;
import com.liangmayong.apidesigner.exception.APIErrorException;
import com.liangmayong.apidesigner.interfaces.APIConstructor;
import com.liangmayong.apidesigner.interfaces.APIConstructor.APIMethod;
import com.liangmayong.apidesigner.listener.OnApiDefaultListener;
import com.liangmayong.apidesigner.listener.OnApiEntityListListener;
import com.liangmayong.apidesigner.listener.OnApiEntityListener;
import com.liangmayong.apidesigner.listener.OnApiRequestListener;
import com.liangmayong.apidesigner.listener.OnApiStringListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

/**
 * API
 *
 * @author LiangMaYong
 * @version 1.0
 */
@SuppressLint("DefaultLocale")
public final class API {
    private static Map<String, API> connectorMap = new HashMap<String, API>();

    public final static API constructor(Class<? extends APIConstructor> constructorClass) {
        if (constructorClass == null) {
            return null;
        }
        if (connectorMap.containsKey(constructorClass.getName())) {
            API connector = connectorMap.get(constructorClass.getName());
            return connector;
        } else {
            try {
                API connector = new API(constructorClass.newInstance());
                connectorMap.put(constructorClass.getName(), connector);
                return connector;
            } catch (Exception e) {
            }
            return null;
        }
    }

    private APIConstructor constructor = null;

    protected String getTag() {
        return "API";
    }

    /**
     * create
     *
     * @param clazz clazz
     * @param <T>   T
     * @return APIModule
     */
    public final <T extends APIModule> T create(Class<T> clazz) {
        try {
            Constructor<T> classConstructor = clazz.getDeclaredConstructor();
            classConstructor.setAccessible(true);
            T t = (T) classConstructor.newInstance();
            t.attachAPI(this);
            return t;
        } catch (Exception e) {
        }
        return null;
    }

    private API(APIConstructor constructor) {
        this.constructor = constructor;
    }

    /**
     * request
     *
     * @param context   context
     * @param method    method
     * @param url       url
     * @param parameter parameter
     * @param listener  listener
     * @param <T>       t
     */
    protected <T> void request(Context context, APIMethod method, String url, final APIParameter parameter,
                               final OnApiEntityListener<T> listener) {
        request(context, method, url, parameter, new OnApiRequestListener() {
            @Override
            public void onResponse(APIResponse response) {
                if (listener != null) {
                    Class<T> entityClass = null;
                    Type t = listener.getClass().getGenericSuperclass();
                    if (t instanceof ParameterizedType) {
                        Type[] p = ((ParameterizedType) t).getActualTypeArguments();
                        entityClass = (Class<T>) p[0];
                    }
                    listener.result(constructor.parseCode(response), constructor.parseMessage(response),
                            constructor.parseEntity(entityClass, response), response);
                }
            }

            @Override
            public void onFailure(APIErrorException e) {
                if (listener != null) {
                    listener.failure(e);
                }
            }
        });
    }

    /**
     * request
     *
     * @param context   context
     * @param method    method
     * @param url       url
     * @param parameter parameter
     * @param listener  listener
     * @param <T>       t
     */
    protected <T> void request(Context context, APIMethod method, String url, final APIParameter parameter,
                               final OnApiEntityListListener<T> listener) {
        request(context, method, url, parameter, new OnApiRequestListener() {

            @Override
            public void onResponse(APIResponse response) {
                if (listener != null) {
                    Class<T> entityClass = null;
                    Type t = listener.getClass().getGenericSuperclass();
                    if (t instanceof ParameterizedType) {
                        Type[] p = ((ParameterizedType) t).getActualTypeArguments();
                        entityClass = (Class<T>) p[0];
                    }
                    listener.result(constructor.parseCode(response), constructor.parseMessage(response),
                            constructor.parseEntitys(entityClass, response), response);
                }
            }

            @Override
            public void onFailure(APIErrorException e) {
                if (listener != null) {
                    listener.failure(e);
                }
            }
        });
    }

    /**
     * request
     *
     * @param context   context
     * @param method    method
     * @param url       url
     * @param parameter parameter
     * @param listener  listener
     */
    protected void request(Context context, APIMethod method, String url, final APIParameter parameter,
                           final OnApiStringListener listener) {
        request(context, method, url, parameter, new OnApiRequestListener() {

            @Override
            public void onResponse(APIResponse response) {
                if (listener != null) {
                    listener.result(constructor.parseCode(response), constructor.parseMessage(response),
                            constructor.parseData(response), response);
                }
            }

            @Override
            public void onFailure(APIErrorException e) {
                if (listener != null) {
                    listener.failure(e);
                }
            }
        });
    }

    /**
     * request
     *
     * @param context   context
     * @param method    method
     * @param url       url
     * @param parameter parameter
     * @param listener  listener
     */
    protected void request(Context context, APIMethod method, String url, final APIParameter parameter,
                           final OnApiDefaultListener listener) {
        request(context, method, url, parameter, new OnApiRequestListener() {

            @Override
            public void onResponse(APIResponse response) {
                if (listener != null) {
                    listener.result(constructor.parseCode(response), constructor.parseMessage(response), response);
                }
            }

            @Override
            public void onFailure(APIErrorException e) {
                if (listener != null) {
                    listener.failure(e);
                }
            }
        });
    }

    /**
     * syncRequest
     *
     * @param context   context
     * @param method    method
     * @param url       url
     * @param parameter parameter
     * @return APIResponse
     * @throws APIErrorException error
     */
    protected APIResponse syncRequest(Context context, APIMethod method, String url, APIParameter parameter)
            throws APIErrorException {
        return constructor.synchronizationRequest(context, method, url, parameter);
    }

    /**
     * request
     *
     * @param context   context
     * @param method    method
     * @param url       url
     * @param parameter parameter
     * @param listener  listener
     */
    protected void request(Context context, APIMethod method, String url, APIParameter parameter,
                           final OnApiRequestListener listener) {
        constructor.asynchronousRequest(context, method, url, parameter, new OnApiRequestListener() {
            @Override
            public void onResponse(APIResponse response) {
                if (listener != null) {
                    listener.onResponse(response);
                }
                APILog.d("onResponse:" + response.toString());
            }

            @Override
            public void onFailure(APIErrorException e) {
                if (listener != null) {
                    listener.onFailure(e);
                }
                APILog.d("onFailure", e);
            }
        });
    }

    /**
     * getBaseUrl
     *
     * @return base url
     */
    protected String getBaseUrl() {
        return constructor.getBaseUrl();
    }

    /**
     * destroy
     *
     * @param context context
     */
    protected void destroy(Context context) {
        constructor.destroy(context);
    }

}