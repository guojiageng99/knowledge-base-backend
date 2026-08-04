package com.knowledge.base.graph.entity;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * Tutorial 21's minimal Neo4j node model. It is kept separate from future
 * knowledge-graph domain nodes introduced in the next tutorial.
 */
@Data
@Node("Person")
public class Person {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private Integer age;

    @Relationship(type = "KNOWS", direction = Relationship.Direction.OUTGOING)
    private Set<Person> friends = new HashSet<>();
}
