package com.eiualee.easyapi.iface;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
  * @作者 eiualee
  * @创建日期 2021/6/1 2:08 下午
  * @描述 刷新Token接口
  */
public interface EasyRefreshTokenIface<T extends EasyRespIface> {
    /**
     * 刷新token具体实现
     * @param <T>
     * @return
     */
   Function<Flowable<Throwable>, Flowable<T>> doRefresh();
}
