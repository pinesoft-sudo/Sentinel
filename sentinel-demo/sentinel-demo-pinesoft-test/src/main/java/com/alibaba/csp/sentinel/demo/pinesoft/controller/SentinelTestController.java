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
package com.alibaba.csp.sentinel.demo.pinesoft.controller;

import com.alibaba.csp.sentinel.demo.pinesoft.service.SentinelTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author YS
 */
@RequestMapping(value = "/sentinel")
@RestController
@Slf4j
public class SentinelTestController {

    private int i = 0;
    @Autowired
    private SentinelTestService testService;

    @GetMapping("/test1")
    public void test1() throws InterruptedException {
        for (int i = 1; i <= 30; i++) {
            log.info("SentinelResource test1 requestIndex:{}", i);
            testService.SentinelResource1();
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    @GetMapping("/test2")
    public void test2(@RequestParam(required = false) String value) throws InterruptedException {
        for (int i = 1; i <= 30; i++) {
            log.info("SentinelResource test2 requestIndex:{}", i);

            if (i % 5 == 0) {
                testService.SentinelResource2(null);
            } else {
                testService.SentinelResource2(value);
            }
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    @GetMapping("/test3")
    public void test3(@RequestParam(required = false) String value) throws InterruptedException {

        for (int i = 1; i <= 30; i++) {
            log.info("SentinelResource test3 requestIndex:{}", i);

            if (i % 2 == 0) {
                testService.SentinelResource3(null);
            } else if (i % 3 == 0) {
                try {
                    testService.SentinelResource3("error");
                } catch (ArithmeticException ex) {
                    log.info("抛出的异常，略过");
                }

            } else {
                testService.SentinelResource3(value);
            }
            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }

    @GetMapping("/test4")
    public void test4() throws Exception {
        ++i;
        log.info("SentinelResource test4 requestIndex:{}", i);
        testService.SentinelResource4(i);
    }

}
