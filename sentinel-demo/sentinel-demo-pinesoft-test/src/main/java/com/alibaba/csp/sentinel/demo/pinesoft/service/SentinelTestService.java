/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.pinesoft.service;

import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.demo.pinesoft.common.ExceptionCustomHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author YS
 */
@Service
@Slf4j
public class SentinelTestService {
    /**
     *
     * @SentinelResource注解参数
     * 1）value 配置资源名称，如果不配置则使用方法全路径名
     * 2）entryType 指定规则作用于入口流量还是出口流量。默认为EntryType.OUT
     * 3）blockHandler 对应处理 BlockException 的函数名称。
     *          该函数的返回类型需要与原方法相匹配，参数类型需要和原方法相匹配并且最后加一个额外的类型为BlockException的参数；
     *          默认需要和原方法在同一个类中，也可以配置blockHandlerClass来指定处理的类，但类里的方法必须是static。
     * 4）fallback 失败回调的函数名称（优先级低于blockHandler），可以处理除了在参数exceptionsToIgnore中指定排除的所有异常类型，
     *    该函数默认与原函数在同一个类型中，也可以配置fallbackClass 来指定处理的类，但类里的方法必须是static，其函数的签名要求如下：
     *      - 返回类型与原函数类型一致
     *      - 参数列表与原函数保持一致，同时可以额外添加一个Throwable类型参数来接收对应的异常
     * 5）defaultFallback 默认的 fallback 函数名称，可以处理除了在参数exceptionsToIgnore中指定排除的所有异常类型，在未配置fallback的情况下生效。
     *    该函数默认与原函数在同一个类型中，也可以配置fallbackClass 来指定处理的类，但类里的方法必须是static，其函数的签名要求如下：
     *      - 返回类型与原函数类型一致
     *      - 参数列表为空，同时可以额外添加一个Throwable类型参数来接收对应的异常
     * 6）exceptionsToIgnore 用于指定哪些异常被排除掉，不会计入异常统计中，也不会进入 fallback 逻辑中，而是会原样抛出。
     * */


    /**
     * 1.对触发限流或降级的资源进行处理
     * 2.blockHandler 函数访问范围需要是 public，
     * 3.返回类型需要与原方法相匹配，参数类型需要和原方法相匹配并且增加BlockException参数。
     */
    @SentinelResource(value = "test1", blockHandler = "blockHandlerMethod")
    public void SentinelResource1() {
        log.info("SentinelResource test1 Pass");
    }

    //运行异常

    /**
     * 1.用于在抛出异常的时候提供 fallback 处理逻辑。fallback 函数可以针对所有类型的异常进行处理
     * 2.除了 exceptionsToIgnore 里面排除掉的异常类型
     */
    @SentinelResource(value = "test2", fallback = "fallbackMethod")
    public void SentinelResource2(String value) {
        if (value == null || "".equals(value)) {
            throw new IllegalArgumentException("invalid arg");
        }
        log.info("SentinelResource Pass:" + value);
    }


    /**
     * 1.exceptionsToIgnore 指定哪些异常被排除掉，不会计入异常统计中，也不会进入 fallback 逻辑中，会原样抛出
     * 2.同时配置了 fallback 和 defaultFallback，则只有 fallback 会生效。
     */

    @SentinelResource(value = "test3", fallback = "fallbackMethod",
            exceptionsToIgnore = {ArithmeticException.class})
    public void SentinelResource3(String value) {
        if (value == null) {
            throw new IllegalArgumentException("未排除的异常IllegalArgumentException");
        }
        if ("error".equals(value)) {
            throw new ArithmeticException("已排除的异常RuntimeException");
        }
        log.info("SentinelResource Pass:" + value);
    }

    //将限流和降级方法外置到单独的类
    @SentinelResource(value = "test4",
            blockHandlerClass = ExceptionCustomHandler.class, blockHandler = "blockHandlerMethod",
            fallbackClass = ExceptionCustomHandler.class, fallback = "fallbackMethod")
    public void SentinelResource4(Integer value) throws Exception {
        if (value > 5 && value <= 10) {
            throw new Exception("我是Exception");
        }
        if (value > 15 && value <= 20) {
            throw new RuntimeException("我是RuntimeException");
        }
        log.info("SentinelResource Pass:" + value);
    }


    /**************************异常测试方法******************************************/

    /**
     * fallbackMethod
     *
     * @param value value
     * @param ex    非必须参数
     * @return String
     */
    public void fallbackMethod(String value, Throwable ex) {
        log.error("fallback异常:{},value:{}", ex.getMessage(), value);
    }

    /**
     * blockHandlerMethod
     *
     * @param ex 必传参数
     * @return String
     */
    public void blockHandlerMethod(BlockException ex) {

        if (ex instanceof FlowException) {
            log.warn("BlockException，接口被限流");
            Tracer.trace(ex);
        } else if (ex instanceof DegradeException) {
            log.warn("BlockException，接口被降级");
        } else if (ex instanceof SystemBlockException) {
            log.warn("BlockException，系统保护");
        }
//      else  if(ex instanceof AuthorityException){
//            return "BlockException，鉴权保护";
//        }
//       else if(ex instanceof ParamFlowException){
//            return "BlockException，热点方式";
//        }
        else {
            log.error("BlockException，未确定类型");
        }
    }

    /**
     * defaultFallback
     *
     * @return String
     */
    public void defaultFallback(Throwable ex) {
        log.error("默认Fallback处理:{}", ex.getMessage());
    }

}
