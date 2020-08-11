CREATE DATABASE IF NOT EXISTS maurodatamapper_test;

USE maurodatamapper_test;

CREATE SCHEMA core;

CREATE SCHEMA datamodel;

CREATE TABLE core.annotation (
    id                         BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    path                       TEXT         NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    depth                      INTEGER      NOT NULL,
    catalogue_item_id          BINARY(16),
    parent_annotation_id       BINARY(16)
        REFERENCES core.annotation,
    created_by                 VARCHAR(255) NOT NULL,
    label                      TEXT         NOT NULL,
    description                TEXT,
    child_annotations_idx      INTEGER
);

CREATE INDEX annotation_parent_annotation_idx
    ON core.annotation(parent_annotation_id);

CREATE INDEX annotation_created_by_idx
    ON core.annotation(created_by);

CREATE TABLE core.api_property (
    id              BINARY(16)   NOT NULL
        PRIMARY KEY,
    version         BIGINT       NOT NULL,
    last_updated_by VARCHAR(255) NOT NULL,
    date_created    TIMESTAMP    NOT NULL,
    last_updated    TIMESTAMP    NOT NULL,
    value           TEXT         NOT NULL,
    created_by      VARCHAR(255) NOT NULL,
    `key`           VARCHAR(255) NOT NULL
);

CREATE INDEX apiproperty_created_by_idx
    ON core.api_property(created_by);

CREATE TABLE core.breadcrumb_tree (
    id                  BINARY(16)   NOT NULL
        PRIMARY KEY,
    version             BIGINT       NOT NULL,
    domain_type         VARCHAR(255) NOT NULL,
    finalised           BOOLEAN,
    domain_id           BINARY(16),
    tree_string         TEXT         NOT NULL,
    top_breadcrumb_tree BOOLEAN      NOT NULL,
    label               VARCHAR(255),
    parent_id           BINARY(16)
        REFERENCES core.breadcrumb_tree
);

CREATE TABLE core.classifier (
    id                              BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                         BIGINT       NOT NULL,
    date_created                    TIMESTAMP    NOT NULL,
    last_updated                    TIMESTAMP    NOT NULL,
    path                            TEXT         NOT NULL,
    depth                           INTEGER      NOT NULL,
    parent_classifier_id            BINARY(16)
        REFERENCES core.classifier,
    readable_by_authenticated_users BOOLEAN      NOT NULL,
    created_by                      VARCHAR(255) NOT NULL,
    readable_by_everyone            BOOLEAN      NOT NULL,
    label                           TEXT         NOT NULL,
    description                     TEXT
);

CREATE INDEX classifier_parent_classifier_idx
    ON core.classifier(parent_classifier_id);

CREATE INDEX classifier_created_by_idx
    ON core.classifier(created_by);

CREATE TABLE core.edit (
    id                   BINARY(16)   NOT NULL
        PRIMARY KEY,
    version              BIGINT       NOT NULL,
    date_created         TIMESTAMP    NOT NULL,
    last_updated         TIMESTAMP    NOT NULL,
    resource_domain_type VARCHAR(255) NOT NULL,
    resource_id          BINARY(16)   NOT NULL,
    created_by           VARCHAR(255) NOT NULL,
    description          VARCHAR(255) NOT NULL
);

CREATE INDEX edit_created_by_idx
    ON core.edit(created_by);

CREATE TABLE core.email (
    id                    BINARY(16)   NOT NULL
        PRIMARY KEY,
    version               BIGINT       NOT NULL,
    sent_to_email_address VARCHAR(255) NOT NULL,
    successfully_sent     BOOLEAN      NOT NULL,
    body                  TEXT         NOT NULL,
    date_time_sent        TIMESTAMP    NOT NULL,
    email_service_used    VARCHAR(255) NOT NULL,
    failure_reason        TEXT,
    subject               TEXT         NOT NULL
);

CREATE TABLE core.folder (
    id                              BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                         BIGINT       NOT NULL,
    date_created                    TIMESTAMP    NOT NULL,
    last_updated                    TIMESTAMP    NOT NULL,
    path                            TEXT         NOT NULL,
    deleted                         BOOLEAN      NOT NULL,
    depth                           INTEGER      NOT NULL,
    readable_by_authenticated_users BOOLEAN      NOT NULL,
    parent_folder_id                BINARY(16)
        REFERENCES core.folder,
    created_by                      VARCHAR(255) NOT NULL,
    readable_by_everyone            BOOLEAN      NOT NULL,
    label                           TEXT         NOT NULL,
    description                     TEXT
);

CREATE INDEX folder_parent_folder_idx
    ON core.folder(parent_folder_id);

CREATE INDEX folder_created_by_idx
    ON core.folder(created_by);

CREATE TABLE core.metadata (
    id                         BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    namespace                  TEXT         NOT NULL,
    catalogue_item_id          BINARY(16),
    value                      TEXT         NOT NULL,
    created_by                 VARCHAR(255) NOT NULL,
    `key`                      TEXT         NOT NULL
);

CREATE INDEX metadata_catalogue_item_idx
    ON core.metadata(catalogue_item_id);

CREATE INDEX metadata_created_by_idx
    ON core.metadata(created_by);

CREATE TABLE core.reference_file (
    id                         BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    file_size                  BIGINT       NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    file_type                  VARCHAR(255) NOT NULL,
    file_name                  VARCHAR(255) NOT NULL,
    file_contents              BLOB         NOT NULL,
    catalogue_item_id          BINARY(16),
    created_by                 VARCHAR(255) NOT NULL
);

CREATE INDEX referencefile_created_by_idx
    ON core.reference_file(created_by);

CREATE TABLE core.semantic_link (
    id                                BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                           BIGINT       NOT NULL,
    date_created                      TIMESTAMP    NOT NULL,
    target_catalogue_item_id          BINARY(16)   NOT NULL,
    last_updated                      TIMESTAMP    NOT NULL,
    catalogue_item_domain_type        VARCHAR(255) NOT NULL,
    target_catalogue_item_domain_type VARCHAR(255) NOT NULL,
    link_type                         VARCHAR(255) NOT NULL,
    catalogue_item_id                 BINARY(16),
    created_by                        VARCHAR(255) NOT NULL
);

CREATE INDEX semantic_link_target_catalogue_item_idx
    ON core.semantic_link(target_catalogue_item_id);

CREATE INDEX semantic_link_catalogue_item_idx
    ON core.semantic_link(catalogue_item_id);

CREATE INDEX semanticlink_created_by_idx
    ON core.semantic_link(created_by);

CREATE TABLE core.user_image_file (
    id            BINARY(16)   NOT NULL
        PRIMARY KEY,
    version       BIGINT       NOT NULL,
    file_size     BIGINT       NOT NULL,
    date_created  TIMESTAMP    NOT NULL,
    last_updated  TIMESTAMP    NOT NULL,
    file_type     VARCHAR(255) NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    user_id       BINARY(16)   NOT NULL,
    file_contents BLOB         NOT NULL,
    created_by    VARCHAR(255) NOT NULL
);

CREATE INDEX userimagefile_created_by_idx
    ON core.user_image_file(created_by);

CREATE TABLE core.version_link (
    id                         BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    target_model_domain_type   VARCHAR(255) NOT NULL,
    link_type                  VARCHAR(255) NOT NULL,
    target_model_id            BINARY(16)   NOT NULL,
    catalogue_item_id          BINARY(16),
    created_by                 VARCHAR(255) NOT NULL
);

CREATE INDEX version_link_target_model_idx
    ON core.version_link(target_model_id);

CREATE INDEX version_link_catalogue_item_idx
    ON core.version_link(catalogue_item_id);

CREATE INDEX versionlink_created_by_idx
    ON core.version_link(created_by);

CREATE TABLE datamodel.data_model (
    id                              BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                         BIGINT       NOT NULL,
    date_created                    TIMESTAMP    NOT NULL,
    finalised                       BOOLEAN      NOT NULL,
    readable_by_authenticated_users BOOLEAN      NOT NULL,
    date_finalised                  TIMESTAMP,
    documentation_version           VARCHAR(255) NOT NULL,
    readable_by_everyone            BOOLEAN      NOT NULL,
    model_type                      VARCHAR(255) NOT NULL,
    last_updated                    TIMESTAMP    NOT NULL,
    organisation                    VARCHAR(255),
    deleted                         BOOLEAN      NOT NULL,
    author                          VARCHAR(255),
    breadcrumb_tree_id              BINARY(16)   NOT NULL
        REFERENCES core.breadcrumb_tree,
    folder_id                       BINARY(16)   NOT NULL
        REFERENCES core.folder,
    created_by                      VARCHAR(255) NOT NULL,
    aliases_string                  TEXT,
    label                           TEXT         NOT NULL,
    description                     TEXT
);

CREATE TABLE datamodel.data_class (
    id                   BINARY(16)   NOT NULL
        PRIMARY KEY,
    version              BIGINT       NOT NULL,
    date_created         TIMESTAMP    NOT NULL,
    last_updated         TIMESTAMP    NOT NULL,
    path                 TEXT         NOT NULL,
    depth                INTEGER      NOT NULL,
    min_multiplicity     INTEGER,
    max_multiplicity     INTEGER,
    parent_data_class_id BINARY(16)
        REFERENCES datamodel.data_class,
    breadcrumb_tree_id   BINARY(16)   NOT NULL
        REFERENCES core.breadcrumb_tree,
    data_model_id        BINARY(16)   NOT NULL
        REFERENCES datamodel.data_model,
    idx                  INTEGER      NOT NULL,
    created_by           VARCHAR(255) NOT NULL,
    aliases_string       TEXT,
    label                TEXT         NOT NULL,
    description          TEXT
);

CREATE INDEX data_class_parent_data_class_idx
    ON datamodel.data_class(parent_data_class_id);

CREATE INDEX data_class_data_model_idx
    ON datamodel.data_class(data_model_id);

CREATE INDEX dataclass_created_by_idx
    ON datamodel.data_class(created_by);

CREATE INDEX datamodel_created_by_idx
    ON datamodel.data_model(created_by);

CREATE TABLE datamodel.data_type (
    id                 BINARY(16)   NOT NULL
        PRIMARY KEY,
    version            BIGINT       NOT NULL,
    date_created       TIMESTAMP    NOT NULL,
    domain_type        VARCHAR(15)  NOT NULL,
    last_updated       TIMESTAMP    NOT NULL,
    path               TEXT         NOT NULL,
    depth              INTEGER      NOT NULL,
    breadcrumb_tree_id BINARY(16)   NOT NULL
        REFERENCES core.breadcrumb_tree,
    data_model_id      BINARY(16)   NOT NULL
        REFERENCES datamodel.data_model,
    idx                INTEGER      NOT NULL,
    created_by         VARCHAR(255) NOT NULL,
    aliases_string     TEXT,
    label              TEXT         NOT NULL,
    description        TEXT,
    class              VARCHAR(255) NOT NULL,
    units              VARCHAR(255),
    reference_class_id BINARY(16)
        REFERENCES datamodel.data_class
);

CREATE TABLE datamodel.data_element (
    id                 BINARY(16)   NOT NULL
        PRIMARY KEY,
    version            BIGINT       NOT NULL,
    date_created       TIMESTAMP    NOT NULL,
    data_class_id      BINARY(16)   NOT NULL
        REFERENCES datamodel.data_class,
    last_updated       TIMESTAMP    NOT NULL,
    path               TEXT         NOT NULL,
    depth              INTEGER      NOT NULL,
    min_multiplicity   INTEGER,
    max_multiplicity   INTEGER,
    breadcrumb_tree_id BINARY(16)   NOT NULL
        REFERENCES core.breadcrumb_tree,
    data_type_id       BINARY(16)   NOT NULL
        REFERENCES datamodel.data_type,
    idx                INTEGER      NOT NULL,
    created_by         VARCHAR(255) NOT NULL,
    aliases_string     TEXT,
    label              TEXT         NOT NULL,
    description        TEXT
);

CREATE INDEX data_element_data_class_idx
    ON datamodel.data_element(data_class_id);

CREATE INDEX data_element_data_type_idx
    ON datamodel.data_element(data_type_id);

CREATE INDEX dataelement_created_by_idx
    ON datamodel.data_element(created_by);

CREATE INDEX data_type_data_model_idx
    ON datamodel.data_type(data_model_id);

CREATE INDEX datatype_created_by_idx
    ON datamodel.data_type(created_by);

CREATE INDEX reference_type_reference_class_idx
    ON datamodel.data_type(reference_class_id);

CREATE TABLE datamodel.enumeration_value (
    id                  BINARY(16)   NOT NULL
        PRIMARY KEY,
    version             BIGINT       NOT NULL,
    date_created        TIMESTAMP    NOT NULL,
    enumeration_type_id BINARY(16)   NOT NULL
        REFERENCES datamodel.data_type,
    value               TEXT         NOT NULL,
    last_updated        TIMESTAMP    NOT NULL,
    path                TEXT         NOT NULL,
    depth               INTEGER      NOT NULL,
    breadcrumb_tree_id  BINARY(16)   NOT NULL
        REFERENCES core.breadcrumb_tree,
    idx                 INTEGER      NOT NULL,
    category            TEXT,
    created_by          VARCHAR(255) NOT NULL,
    aliases_string      TEXT,
    `key`               TEXT         NOT NULL,
    label               TEXT         NOT NULL,
    description         TEXT
);

CREATE INDEX enumeration_value_enumeration_type_idx
    ON datamodel.enumeration_value(enumeration_type_id);

CREATE INDEX enumerationvalue_created_by_idx
    ON datamodel.enumeration_value(created_by);

CREATE TABLE datamodel.join_enumerationvalue_to_facet (
    enumerationvalue_id BINARY(16) NOT NULL
        REFERENCES datamodel.enumeration_value,
    classifier_id       BINARY(16)
        REFERENCES core.classifier,
    annotation_id       BINARY(16)
        REFERENCES core.annotation,
    semantic_link_id    BINARY(16)
        REFERENCES core.semantic_link,
    reference_file_id   BINARY(16)
        REFERENCES core.reference_file,
    metadata_id         BINARY(16)
        REFERENCES core.metadata
);

CREATE TABLE datamodel.summary_metadata (
    id                         BINARY(16)   NOT NULL
        PRIMARY KEY,
    version                    BIGINT       NOT NULL,
    summary_metadata_type      VARCHAR(255) NOT NULL,
    date_created               TIMESTAMP    NOT NULL,
    last_updated               TIMESTAMP    NOT NULL,
    catalogue_item_domain_type VARCHAR(255) NOT NULL,
    catalogue_item_id          BINARY(16),
    created_by                 VARCHAR(255) NOT NULL,
    label                      TEXT         NOT NULL,
    description                TEXT
);

CREATE TABLE datamodel.join_dataclass_to_facet (
    dataclass_id        BINARY(16) NOT NULL
        REFERENCES datamodel.data_class,
    classifier_id       BINARY(16)
        REFERENCES core.classifier,
    annotation_id       BINARY(16)
        REFERENCES core.annotation,
    semantic_link_id    BINARY(16)
        REFERENCES core.semantic_link,
    reference_file_id   BINARY(16)
        REFERENCES core.reference_file,
    metadata_id         BINARY(16)
        REFERENCES core.metadata,
    summary_metadata_id BINARY(16)
        REFERENCES datamodel.summary_metadata
);

CREATE TABLE datamodel.join_dataelement_to_facet (
    dataelement_id      BINARY(16) NOT NULL
        REFERENCES datamodel.data_element,
    classifier_id       BINARY(16)
        REFERENCES core.classifier,
    annotation_id       BINARY(16)
        REFERENCES core.annotation,
    semantic_link_id    BINARY(16)
        REFERENCES core.semantic_link,
    reference_file_id   BINARY(16)
        REFERENCES core.reference_file,
    metadata_id         BINARY(16)
        REFERENCES core.metadata,
    summary_metadata_id BINARY(16)
        REFERENCES datamodel.summary_metadata
);

CREATE TABLE datamodel.join_datamodel_to_facet (
    datamodel_id        BINARY(16) NOT NULL
        REFERENCES datamodel.data_model,
    classifier_id       BINARY(16)
        REFERENCES core.classifier,
    annotation_id       BINARY(16)
        REFERENCES core.annotation,
    semantic_link_id    BINARY(16)
        REFERENCES core.semantic_link,
    version_link_id     BINARY(16)
        REFERENCES core.version_link,
    reference_file_id   BINARY(16)
        REFERENCES core.reference_file,
    metadata_id         BINARY(16)
        REFERENCES core.metadata,
    summary_metadata_id BINARY(16)
        REFERENCES datamodel.summary_metadata
);

CREATE TABLE datamodel.join_datatype_to_facet (
    datatype_id         BINARY(16) NOT NULL
        REFERENCES datamodel.data_type,
    classifier_id       BINARY(16)
        REFERENCES core.classifier,
    annotation_id       BINARY(16)
        REFERENCES core.annotation,
    semantic_link_id    BINARY(16)
        REFERENCES core.semantic_link,
    reference_file_id   BINARY(16)
        REFERENCES core.reference_file,
    metadata_id         BINARY(16)
        REFERENCES core.metadata,
    summary_metadata_id BINARY(16)
        REFERENCES datamodel.summary_metadata
);

CREATE INDEX summarymetadata_created_by_idx
    ON datamodel.summary_metadata(created_by);

CREATE TABLE datamodel.summary_metadata_report (
    id                  BINARY(16)   NOT NULL
        PRIMARY KEY,
    version             BIGINT       NOT NULL,
    date_created        TIMESTAMP    NOT NULL,
    last_updated        TIMESTAMP    NOT NULL,
    report_date         TIMESTAMP    NOT NULL,
    created_by          VARCHAR(255) NOT NULL,
    report_value        TEXT         NOT NULL,
    summary_metadata_id BINARY(16)   NOT NULL
        REFERENCES datamodel.summary_metadata
);

CREATE INDEX summarymetadatareport_created_by_idx
    ON datamodel.summary_metadata_report(created_by);

CREATE INDEX summary_metadata_report_summary_metadata_idx
    ON datamodel.summary_metadata_report(summary_metadata_id);

