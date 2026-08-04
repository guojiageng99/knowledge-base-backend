package com.knowledge.base.userauth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberVO implements Serializable {
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
    private String role;
    private LocalDateTime joinedAt;
}
