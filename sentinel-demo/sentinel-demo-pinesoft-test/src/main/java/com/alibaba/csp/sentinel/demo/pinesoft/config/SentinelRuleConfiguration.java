package com.alibaba.csp.sentinel.demo.pinesoft.config;


import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


/**
 * @author YS
 */
@Component//使用Configuration 存在问题，暂未找到原因
public class SentinelRuleConfiguration {

    @PostConstruct
    public void init() {
        //限流规则
        initFlowRule("test1");

        //熔断降级规则
        //initDegradeRule("test2");

        //熔断降级规则
        initDegradeRule("test3");


        //熔断降级规则
        //initFlowRule("test4");
        //熔断降级规则
        //initDegradeRule("test4");
    }

    /**
     * 1.流量控制（flow control），其原理是监控应用流量的 QPS 或并发线程数等指标，
     * 2.当达到指定的阈值时对流量进行控制，以避免被瞬时的流量高峰冲垮，从而保障应用的高可用性。
     * 3.流量控制规则 (FlowRule).
     * 4.同一个资源可以同时有多个限流规则，检查规则时会依次检查。
     *
     * @param resourceName 资源名，资源名是限流规则的作用对象
     */
    private static void initFlowRule(String resourceName) {
        List<FlowRule> rules = new ArrayList<>();

        /*
         * 流量控制的效果
         * 1）直接拒绝（RuleConstant.CONTROL_BEHAVIOR_DEFAULT）方式是默认的流量控制方式(该方式适用于对系统处理能力确切已知的情况下);
         * 2）Warm Up（RuleConstant.CONTROL_BEHAVIOR_WARM_UP）方式，即预热/冷启动方式。
         * 3）匀速排队（RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER）让请求以均匀的速度通过，对应的是漏桶算法（该方式主要用于处理间隔性突发的流量，暂不支持 QPS > 1000 的场景）。
         * */

        //QPS 模式(默认)
        FlowRule qpsRule = new FlowRule(resourceName);
        qpsRule.setCount(1);  //限流阈值
        qpsRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        qpsRule.setLimitApp("default");  //流控针对的调用来源。默认：default，代表不区分调用来源
        qpsRule.setClusterMode(false); //是否集群限流。默认为false
        qpsRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        //qpsRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        //qpsRule.setMaxQueueingTimeMs(20 * 1000);//这里设置的等待处理时间让系统能平稳的处理所有的请求.表示每一个请求的最长等待时间20s
        rules.add(qpsRule);

        //并发线程数控制模式,并发数控制用于保护业务线程池不被慢调用耗尽
//        FlowRule thRule = new FlowRule(resourceName);
//        thRule.setCount(1);  //限流阈值
//        thRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
//        thRule.setLimitApp("default");  //流控针对的调用来源。默认：default，代表不区分调用来源
//        thRule.setClusterMode(false); //是否集群限流。默认为false
//        thRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
//        rules.add(thRule);

        //定义流量控制规则
        FlowRuleManager.loadRules(rules);
    }

    /**
     * 熔断降级规则 (DegradeRule)
     * <p>
     * 1：慢调用比例 (SLOW_REQUEST_RATIO)：选择以慢调用比例作为阈值，需要设置允许的慢调用 RT（即最大的响应时间），
     * 请求的响应时间大于该值则统计为慢调用。当单位统计时长（statIntervalMs）内请求数目大于设置的最小请求数目，
     * 并且慢调用的比例大于阈值，则接下来的熔断时长内请求会自动被熔断。 经过熔断时长后熔断器会进入探测恢复状态（HALF-OPEN 状态），
     * 若接下来的一个请求响应时间小于设置的慢调用 RT 则结束熔断，若大于设置的慢调用 RT 则会再次被熔断。
     * <p>
     * 2：异常比例 (ERROR_RATIO)：当单位统计时长（statIntervalMs）内请求数目大于设置的最小请求数目，
     * 并且异常的比例大于阈值，则接下来的熔断时长内请求会自动被熔断。经过熔断时长后熔断器会进入探测恢复状态（HALF-OPEN 状态），
     * 若接下来的一个请求成功完成（没有错误）则结束熔断，否则会再次被熔断。异常比率的阈值范围是 [0.0, 1.0]，代表 0% - 100%。
     * <p>
     * 3：异常数 (ERROR_COUNT)：当单位统计时长内的异常数目超过阈值之后会自动进行熔断。
     * 经过熔断时长后熔断器会进入探测恢复状态（HALF-OPEN 状态），若接下来的一个请求成功完成（没有错误）则结束熔断，否则会再次被熔断。
     * 注意异常降级仅针对业务异常，对 Sentinel 限流降级本身的异常（BlockException）不生效。
     * *****为了统计异常比例或异常数，需要通过 Tracer.trace(ex) 记录业务异常****
     */
    private static void initDegradeRule(String resourceName) {

        List<DegradeRule> rules = new ArrayList<>();


        //配置策略1：慢调用比例
//        DegradeRule srule = new DegradeRule(resourceName);
//        srule.setGrade(RuleConstant.DEGRADE_GRADE_RT);//熔断策略，支持慢调用比例/异常比例/异常数策略, 默认：慢调用比例
//        srule.setCount(10);//慢调用比例模式下为慢调用临界 RT（超出该值计为慢调用，单位ms），该值要小于“统计时长”，否则不会生效。
//        srule.setTimeWindow(10);//熔断时长，单位为 s
//        srule.setMinRequestAmount(5); //熔断触发的最小请求数，请求数小于该值时即使异常比率超出阈值也不会熔断。
//        srule.setStatIntervalMs(60 * 1000);//单位统计时长（单位为 ms）
//        srule.setSlowRatioThreshold(0.6);//慢调用比例阈值，仅慢调用比例模式有效
//        rules.add(srule);

        //配置策略2：异常比例
//        DegradeRule erule = new DegradeRule(resourceName);
//        erule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
//        erule.setCount(0.1);   //异常比率阈值,阈值范围是 [0.0, 1.0]，代表 0% - 100%。
//        erule.setTimeWindow(10);  //熔断时长，单位为 s
//        erule.setMinRequestAmount(5); //熔断触发的最小请求数
//        erule.setStatIntervalMs(1000); //单位统计时长 （单位为 ms）
//        rules.add(erule);


        //配置策略3：异常数
        DegradeRule ecrule = new DegradeRule(resourceName);
        ecrule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        ecrule.setCount(2);   //异常数阈值
        ecrule.setTimeWindow(5);  //熔断时长，单位为 s
        ecrule.setMinRequestAmount(1); //熔断触发的最小请求数
        ecrule.setRtSlowRequestAmount(1000 * 60); //单位统计时长
        rules.add(ecrule);

        //加载配置的熔断策略
        DegradeRuleManager.loadRules(rules);
    }
}
