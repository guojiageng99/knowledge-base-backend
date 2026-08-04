package com.knowledge.base.graph.service;

import com.knowledge.base.graph.entity.Person;
import com.knowledge.base.graph.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialService {

    private final PersonRepository personRepository;

    /**
     * Minimal service call from tutorial 21: persist a node and query the
     * common friends of two existing people.
     */
    public void demo() {
        Person alice = new Person();
        alice.setName("爱丽丝");
        alice.setAge(24);
        personRepository.save(alice);

        List<Person> friends = personRepository.findCommonFriends("张三", "李四");
        log.info("共同好友个数：{}", friends.size());
    }
}
