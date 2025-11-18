-- ================================================
-- 데이터베이스 생성 (필요한 경우)
-- ================================================
CREATE
DATABASE sprain_ai
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    TEMPLATE template0;

-- 데이터베이스 연결
\c
sprain_ai;

-- ================================================
-- messages 테이블 생성
-- ================================================
DROP TABLE IF EXISTS messages CASCADE;

CREATE TABLE messages
(
    id                BIGSERIAL PRIMARY KEY,
    conversation_id   VARCHAR(255) NOT NULL,
    role              varchar      NOT NULL,
    content           TEXT         NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    prompt_tokens     INTEGER,
    completion_tokens INTEGER,
    total_tokens      INTEGER,

    -- 제약 조건
    CONSTRAINT messages_content_not_empty CHECK (LENGTH(TRIM(content)) > 0),
    CONSTRAINT messages_tokens_positive CHECK (
        (prompt_tokens IS NULL OR prompt_tokens >= 0) AND
        (completion_tokens IS NULL OR completion_tokens >= 0) AND
        (total_tokens IS NULL OR total_tokens >= 0)
        )
);

-- ================================================
-- 인덱스 생성
-- ================================================
-- conversation_id로 메시지 조회 (가장 많이 사용됨)
CREATE INDEX idx_messages_conversation_id ON messages (conversation_id);

-- conversation_id + created_at 복합 인덱스 (시간순 조회)
CREATE INDEX idx_messages_conversation_created ON messages (conversation_id, created_at ASC);

-- created_at 인덱스 (전체 메시지 시간순 조회)
CREATE INDEX idx_messages_created_at ON messages (created_at DESC);

-- role 인덱스 (역할별 필터링)
CREATE INDEX idx_messages_role ON messages (role);

-- 전체 텍스트 검색 인덱스 (선택사항, 메시지 내용 검색용)
CREATE INDEX idx_messages_content_search ON messages USING gin(to_tsvector('english', content));

-- ================================================
-- 테이블 및 컬럼 코멘트
-- ================================================
COMMENT
ON TABLE messages IS '대화 메시지 저장 테이블';
COMMENT
ON COLUMN messages.id IS '메시지 고유 식별자 (자동 증가)';
COMMENT
ON COLUMN messages.conversation_id IS '대화 세션 식별자';
COMMENT
ON COLUMN messages.role IS '메시지 발신자 역할 (USER/ASSISTANT/SYSTEM)';
COMMENT
ON COLUMN messages.content IS '메시지 내용';
COMMENT
ON COLUMN messages.created_at IS '메시지 생성 시간 (자동 생성)';
COMMENT
ON COLUMN messages.prompt_tokens IS '입력 프롬프트 토큰 수';
COMMENT
ON COLUMN messages.completion_tokens IS 'AI 응답 생성 토큰 수';
COMMENT
ON COLUMN messages.total_tokens IS '총 사용 토큰 수 (prompt + completion)';


-- UUID 확장 활성화 (UUID 자동 생성용)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================
-- conversations 테이블 생성
-- ================================================
DROP TABLE IF EXISTS conversations CASCADE;

CREATE TABLE conversations (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               user_id VARCHAR(255) NOT NULL,
                               title VARCHAR(100) NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               last_message_at TIMESTAMP WITH TIME ZONE,
                               summary TEXT,

    -- 제약 조건
                               CONSTRAINT conversations_title_length CHECK (LENGTH(title) BETWEEN 1 AND 100),
                               CONSTRAINT conversations_title_not_empty CHECK (LENGTH(TRIM(title)) > 0)
);

-- ================================================
-- 인덱스 생성
-- ================================================
-- user_id로 대화 조회 (가장 많이 사용됨)
CREATE INDEX idx_conversations_user_id ON conversations(user_id);

-- user_id + last_message_at 복합 인덱스 (사용자의 최근 대화 조회)
CREATE INDEX idx_conversations_user_last_message ON conversations(user_id, last_message_at DESC NULLS LAST);

-- last_message_at 인덱스 (최근 대화 조회)
CREATE INDEX idx_conversations_last_message_at ON conversations(last_message_at DESC NULLS LAST);

-- created_at 인덱스 (생성 시간순 조회)
CREATE INDEX idx_conversations_created_at ON conversations(created_at DESC);

-- title 부분 일치 검색 인덱스 (LIKE 검색용)
CREATE INDEX idx_conversations_title_pattern ON conversations(title text_pattern_ops);

-- 전체 텍스트 검색 인덱스 (선택사항, title 검색용)
CREATE INDEX idx_conversations_title_search ON conversations USING gin(to_tsvector('english', title));

-- 전체 텍스트 검색 인덱스 (선택사항, summary 검색용)
CREATE INDEX idx_conversations_summary_search ON conversations USING gin(to_tsvector('english', COALESCE(summary, '')));

-- ================================================
-- 테이블 및 컬럼 코멘트
-- ================================================
COMMENT ON TABLE conversations IS '사용자 대화 세션 정보 저장 테이블';
COMMENT ON COLUMN conversations.id IS '대화 고유 식별자 (UUID 자동 생성)';
COMMENT ON COLUMN conversations.user_id IS '사용자 식별자';
COMMENT ON COLUMN conversations.title IS '대화 제목 (최대 100자)';
COMMENT ON COLUMN conversations.created_at IS '대화 생성 시간 (자동 생성)';
COMMENT ON COLUMN conversations.last_message_at IS '마지막 메시지 시간';
COMMENT ON COLUMN conversations.summary IS 'AI 생성 대화 요약';
