-- ================================================
-- pgvector extension 활성화
-- ================================================
CREATE
EXTENSION IF NOT EXISTS vector;

-- ================================================
-- vector_store 테이블 생성
-- ================================================
CREATE TABLE IF NOT EXISTS vector_store
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content    TEXT NOT NULL,
    metadata JSONB,
    embedding vector(2048), -- dimensions 확인!
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ================================================
-- 성능 최적화 인덱스
-- ================================================
CREATE INDEX idx_vector_store_embedding
    ON vector_store
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX IF NOT EXISTS idx_vector_store_metadata
    ON vector_store
    USING gin (metadata jsonb_path_ops);


-- ================================================
-- documents 테이블 생성
-- ================================================
CREATE TABLE IF NOT EXISTS documents
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename     VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    content_type VARCHAR(50)  NOT NULL,
    uploaded_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    chunk_count  INTEGER      NOT NULL,
    metadata     TEXT,

    -- 인덱스를 위한 컬럼 (선택사항)
    CONSTRAINT chk_chunk_count_positive CHECK (chunk_count >= 0)
);

-- ================================================
-- 성능 최적화 인덱스
-- ================================================
-- 파일명으로 검색할 때
CREATE INDEX IF NOT EXISTS idx_documents_filename
    ON documents(filename);

-- 업로드 날짜로 정렬/검색할 때
CREATE INDEX IF NOT EXISTS idx_documents_uploaded_at
    ON documents(uploaded_at DESC);

-- 컨텐츠 타입으로 필터링할 때
CREATE INDEX IF NOT EXISTS idx_documents_content_type
    ON documents(content_type);

-- 복합 인덱스 (컨텐츠 타입 + 업로드 날짜)
CREATE INDEX IF NOT EXISTS idx_documents_type_date
    ON documents(content_type, uploaded_at DESC);

-- ================================================
-- 코멘트 추가 (선택사항)
-- ================================================
COMMENT ON TABLE documents IS '업로드된 문서 정보를 저장하는 테이블';
COMMENT ON COLUMN documents.id IS '문서 고유 식별자 (UUID)';
COMMENT ON COLUMN documents.filename IS '원본 파일명';
COMMENT ON COLUMN documents.content IS '문서 전체 내용';
COMMENT ON COLUMN documents.content_type IS '문서 타입 (PDF, DOCX, TXT 등)';
COMMENT ON COLUMN documents.uploaded_at IS '업로드 일시';
COMMENT ON COLUMN documents.chunk_count IS '문서가 나뉜 청크 개수';
COMMENT ON COLUMN documents.metadata IS 'JSON 형태의 추가 메타데이터';

-- ================================================
-- 확인 메시지
-- ================================================
DO $$
BEGIN RAISE NOTICE 'Vector store initialized successfully';
END $$;

