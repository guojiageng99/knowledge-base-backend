package com.knowledge.base.document.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthorVO implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String position;
}
