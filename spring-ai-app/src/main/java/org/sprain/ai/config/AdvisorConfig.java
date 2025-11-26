package org.sprain.ai.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.hibernate.annotations.Bag;
import org.sprain.ai.global.advisor.McpPromptAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class AdvisorConfig {

    @Bean
    public Map<String, Advisor> advisorFactory(ApplicationContext applicationContext) {
        // BaseAdvisor와 CallAdvisor 둘 다 가져옴
        Map<String, Advisor> allAdvisors = new HashMap<>();

        // BaseAdvisor 타입 수집
        allAdvisors.putAll(applicationContext.getBeansOfType(BaseAdvisor.class));

        // CallAdvisor 타입 수집 (중복 제거)
        allAdvisors.putAll(applicationContext.getBeansOfType(CallAdvisor.class));

        // 빈 이름에 "Advisor"가 포함된 것만 필터링
        return allAdvisors.entrySet().stream()
                .filter(entry -> entry.getKey().contains("Advisor"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    @Bean(name = "mcpPromptAdvisor")
    public McpPromptAdvisor mcpPromptAdvisor(List<McpSyncClient> mcpClients) {
        return McpPromptAdvisor.builder()  // 일반 클래스의 인스턴스 생성
                .mcpClients(mcpClients)
                .enableCache(true)
                .cacheTtl(10 * 60 * 1000L)
                .order(0)
                .build();  // 이 반환값이 Spring Bean으로 등록됨
    }

    @Bean(name = "simpleLoggerAdvisor")
    public SimpleLoggerAdvisor simpleLoggerAdvisor() {
        return new SimpleLoggerAdvisor();
    }


}
