package org.sprain.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.sprain.ai.dto.MessageRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;  // USER, ASSISTANT, SYSTEM

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;
}
