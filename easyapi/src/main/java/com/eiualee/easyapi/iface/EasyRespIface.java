package com.eiualee.easyapi.iface;

import java.util.List;

/**
 * 返回数据的获取接口
 */
public interface EasyRespIface<T> {
    /**
     * 获取请求返回的data数据
     * @return 返回传入的泛型
     */
    T getRespData();

    /**
     * 获取请求后返回的ListData数据
     * @return 返回一个集合
     */
    List<T> getRespListData();
}
