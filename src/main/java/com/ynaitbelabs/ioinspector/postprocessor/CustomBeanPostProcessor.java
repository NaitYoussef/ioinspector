package com.ynaitbelabs.ioinspector.postprocessor;

import com.ynaitbelabs.ioinspector.configuration.MyCache;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.reactive.function.client.WebClient;

public class CustomBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof WebClient webClient) {
            WebClient mutatedWebClient = webClient.mutate()
                    .filter((request, next) -> {
                                MyCache.cache.add(request);
                                return next.exchange(request);
                            }
                    )
                    .build();
            return mutatedWebClient;
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }
}
