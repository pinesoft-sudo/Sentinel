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
package com.alibaba.csp.sentinel.demo.pinesoft.common;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author YS
 */
@Slf4j
public class ExceptionCustomHandler {

    /**
     * fallbackMethod
     *
     * @param value value
     * @param ex    非必须参数
     * @return String
     */
    public static void fallbackMethod(Integer value, Throwable ex) {
        log.error("fallback异常:{},value:{}", ex.getMessage(), value);
    }

    /**
     * blockHandlerMethod
     *
     * @param value value
     * @param ex    必传参数
     * @return String
     */
    public static void blockHandlerMethod(Integer value, BlockException ex) {
        if (ex instanceof FlowException) {
            log.warn("BlockException，接口被限流");
        } else if (ex instanceof DegradeException) {
            log.warn("BlockException，接口被降级");
        } else if (ex instanceof SystemBlockException) {
            log.warn("BlockException ，系统保护");
        } else if (ex instanceof AuthorityException) {
            log.warn("BlockException，认证保护");
        } else if (ex instanceof ParamFlowException) {
            log.warn("BlockException，热点方式");
        } else {
            log.error("BlockException，未确定类型");
        }
    }
}
