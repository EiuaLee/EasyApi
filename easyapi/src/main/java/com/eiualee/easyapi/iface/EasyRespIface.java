package com.eiualee.easyapi.iface;

import java.util.List;

/**
 * 返回数据的获取接口
 * @param <T>
 */
public interface EasyRespIface<T> {
    /**
     * 获取请求返回的data数据
     * @return
     */
    T getRespData();

    /**
     * 获取请求后返回的ListData数据
     * @return
     */
    List<T> getRespListData();
}
