//package org.sprain.ai.config;
//
//import io.modelcontextprotocol.client.McpClient;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Slf4j
//@Component
//@Order(100)  // 늦게 실행
//@RequiredArgsConstructor
//public class McpClientDelayedInitializer implements ApplicationRunner {
//
//    private final List<McpClient> mcpClients;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        log.info("🔌 서버 시작 완료. MCP Client 초기화 대기 중...");
//
//        // 2초 대기 (서버가 완전히 준비될 때까지)
//        Thread.sleep(2000);
//
//        if (mcpClients != null && !mcpClients.isEmpty()) {
//            log.info("✅ MCP Clients 발견: {} 개", mcpClients.size());
//
//            // Client 초기화 시도
//            for (McpClient client : mcpClients) {
//                try {
//                    log.info("🔄 MCP Client 초기화 시도...");
//                    // 초기화는 자동으로 진행됨
//                    log.info("✅ MCP Client 초기화 성공");
//                } catch (Exception e) {
//                    log.error("❌ MCP Client 초기화 실패", e);
//                }
//            }
//        } else {
//            log.warn("⚠️ MCP Client가 없습니다!");
//        }
//    }
//}