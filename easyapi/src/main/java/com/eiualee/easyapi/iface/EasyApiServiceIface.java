package com.eiualee.easyapi.iface;

/**
  * @作者 liweihua
  * @创建日期 2021/6/3 5:46 下午
  * @描述 取得定义的API接口
  */
public  interface EasyApiServiceIface<T> {

    /**
     *  获取API接口
     * @return
     */
    T getEasyService();
}
