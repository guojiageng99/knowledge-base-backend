CREATE DATABASE IF NOT EXISTS kb_graph
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS kb_graph.kb_graph_node (
    id BIGINT NOT NULL AUTO_INCREMENT,
    node_id VARCHAR(255) NOT NULL COMMENT 'Neo4j node identifier',
    node_type VARCHAR(50) NOT NULL COMMENT 'Node type',
    node_name VARCHAR(500) NOT NULL COMMENT 'Node name',
    properties JSON DEFAULT NULL COMMENT 'Node properties',
    labels VARCHAR(500) DEFAULT NULL COMMENT 'Neo4j labels',
    weight DOUBLE DEFAULT 1.0 COMMENT 'Node weight',
    PRIMARY KEY (id),
    UNIQUE KEY uk_node_id (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS kb_graph.kb_graph_edge (
    id BIGINT NOT NULL AUTO_INCREMENT,
    edge_id VARCHAR(255) NOT NULL COMMENT 'Neo4j relationship identifier',
    source_id VARCHAR(255) NOT NULL COMMENT 'Source node identifier',
    target_id VARCHAR(255) NOT NULL COMMENT 'Target node identifier',
    relationship_type VARCHAR(50) NOT NULL COMMENT 'Relationship type',
    relationship_name VARCHAR(255) DEFAULT NULL COMMENT 'Relationship name',
    properties JSON DEFAULT NULL COMMENT 'Relationship properties',
    weight DOUBLE DEFAULT 1.0 COMMENT 'Relationship weight',
    PRIMARY KEY (id),
    UNIQUE KEY uk_edge_id (edge_id),
    KEY idx_source_id (source_id),
    KEY idx_target_id (target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
