-- M6: CMS staff, evidence-governed content, catalog revision, bookmarks.

CREATE TABLE cms_staff (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE cms_staff_role (
    staff_id UUID NOT NULL REFERENCES cms_staff(id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL,
    PRIMARY KEY (staff_id, role)
);

CREATE TABLE content_source (
    id UUID PRIMARY KEY,
    citation_key VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    url VARCHAR(2000),
    publication_date DATE,
    last_checked_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE content_document (
    id UUID PRIMARY KEY,
    slug VARCHAR(200) NOT NULL,
    locale VARCHAR(16) NOT NULL,
    published_version_id UUID,
    withdrawn_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_content_document_slug_locale UNIQUE (slug, locale)
);

CREATE TABLE content_version (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES content_document(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    title VARCHAR(500) NOT NULL,
    summary VARCHAR(2000),
    body_richtext TEXT,
    content_type VARCHAR(64) NOT NULL,
    life_stage VARCHAR(64),
    gestational_week INT,
    tags_json TEXT,
    medical_risk_level VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    review_stage VARCHAR(32),
    effective_from TIMESTAMP WITH TIME ZONE,
    effective_to TIMESTAMP WITH TIME ZONE,
    source_publication_date DATE,
    source_last_checked_at TIMESTAMP WITH TIME ZONE,
    clinical_reviewer VARCHAR(200),
    clinical_reviewed_at TIMESTAMP WITH TIME ZONE,
    next_review_due_at TIMESTAMP WITH TIME ZONE,
    created_by_staff_id UUID REFERENCES cms_staff(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_content_version_document_version UNIQUE (document_id, version_number)
);

ALTER TABLE content_document
    ADD CONSTRAINT fk_content_document_published_version
    FOREIGN KEY (published_version_id) REFERENCES content_version(id);

CREATE TABLE content_version_source (
    version_id UUID NOT NULL REFERENCES content_version(id) ON DELETE CASCADE,
    source_id UUID NOT NULL REFERENCES content_source(id) ON DELETE CASCADE,
    PRIMARY KEY (version_id, source_id)
);

CREATE TABLE content_audit_event (
    id UUID PRIMARY KEY,
    actor_staff_id UUID,
    actor_email VARCHAR(320),
    action VARCHAR(64) NOT NULL,
    document_id UUID,
    version_id UUID,
    metadata_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_content_audit_document ON content_audit_event (document_id, created_at DESC);
CREATE INDEX idx_content_version_status ON content_version (status);
CREATE INDEX idx_content_version_gestational_week ON content_version (gestational_week);

CREATE TABLE content_catalog_state (
    id INT PRIMARY KEY CHECK (id = 1),
    publication_revision BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

INSERT INTO content_catalog_state (id, publication_revision, updated_at)
VALUES (1, 0, CURRENT_TIMESTAMP);

CREATE TABLE user_content_bookmark (
    user_id UUID NOT NULL,
    document_id UUID NOT NULL REFERENCES content_document(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, document_id)
);

UPDATE app_meta SET meta_value = 'm6' WHERE meta_key = 'schema_bootstrap';

-- M0 envelope document (non-medical contract fixture).
INSERT INTO content_document (
    id, slug, locale, published_version_id, withdrawn_at, created_at, updated_at
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    'm0-content-envelope',
    'fa-IR',
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO content_version (
    id, document_id, version_number, title, summary, body_richtext, content_type,
    life_stage, gestational_week, tags_json, medical_risk_level, status, review_stage,
    effective_from, effective_to, source_publication_date, source_last_checked_at,
    clinical_reviewer, clinical_reviewed_at, next_review_due_at, created_by_staff_id, created_at
) VALUES (
    '22222222-2222-2222-2222-222222222223',
    '22222222-2222-2222-2222-222222222222',
    1,
    'پوستهٔ محتوا — بدون متن پزشکی',
    'این رکورد فقط قرارداد CMS را نشان می‌دهد و محتوای پزشکی نیست.',
    NULL,
    'envelope',
    NULL,
    NULL,
    '[]',
    'none',
    'draft',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    CURRENT_TIMESTAMP
);
