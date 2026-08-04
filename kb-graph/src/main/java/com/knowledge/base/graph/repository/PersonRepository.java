package com.knowledge.base.graph.repository;

import com.knowledge.base.graph.entity.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends Neo4jRepository<Person, Long> {

    Optional<Person> findByName(String name);

    @Query("MATCH (p1:Person {name: $name1})-[:KNOWS]-(common)-[:KNOWS]-(p2:Person {name: $name2}) RETURN common")
    List<Person> findCommonFriends(@Param("name1") String name1, @Param("name2") String name2);
}
