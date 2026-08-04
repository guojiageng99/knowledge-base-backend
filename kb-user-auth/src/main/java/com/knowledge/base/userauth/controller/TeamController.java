package com.knowledge.base.userauth.controller;

import com.knowledge.base.common.annotation.OperationLog;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.userauth.dto.TeamCreateDTO;
import com.knowledge.base.userauth.dto.TeamQueryDTO;
import com.knowledge.base.userauth.dto.TeamUpdateDTO;
import com.knowledge.base.userauth.service.TeamService;
import com.knowledge.base.userauth.vo.TeamMemberVO;
import com.knowledge.base.userauth.vo.TeamVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Team management", description = "Team space management APIs")
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @OperationLog(module = "Team management", operation = "Create team", description = "Create a team space")
    public Result<Long> createTeam(@Valid @RequestBody TeamCreateDTO dto) { return Result.success(teamService.createTeam(dto)); }

    @PutMapping
    @OperationLog(module = "Team management", operation = "Update team", description = "Update a team space")
    public Result<Boolean> updateTeam(@Valid @RequestBody TeamUpdateDTO dto) { return Result.success(teamService.updateTeam(dto)); }

    @DeleteMapping("/{teamId}")
    @OperationLog(module = "Team management", operation = "Delete team", description = "Delete a team space")
    public Result<Boolean> deleteTeam(@PathVariable Long teamId) { return Result.success(teamService.deleteTeam(teamId)); }

    @GetMapping("/{teamId}")
    public Result<TeamVO> getTeamDetail(@PathVariable Long teamId) { return Result.success(teamService.getTeamDetail(teamId)); }

    @PostMapping("/page")
    public Result<PageResult<TeamVO>> pageTeams(@RequestBody TeamQueryDTO dto) { return Result.success(teamService.pageTeams(dto)); }

    @GetMapping("/tree")
    public Result<List<TeamVO>> getTeamTree() { return Result.success(teamService.getTeamTree()); }

    @PostMapping("/{teamId}/members")
    @OperationLog(module = "Team management", operation = "Add members", description = "Add members to a team space")
    public Result<Boolean> addTeamMembers(@PathVariable Long teamId, @RequestBody List<Long> userIds) { return Result.success(teamService.addTeamMembers(teamId, userIds)); }

    @DeleteMapping("/{teamId}/members")
    @OperationLog(module = "Team management", operation = "Remove members", description = "Remove members from a team space")
    public Result<Boolean> removeTeamMembers(@PathVariable Long teamId, @RequestBody List<Long> userIds) { return Result.success(teamService.removeTeamMembers(teamId, userIds)); }

    @GetMapping("/{teamId}/members")
    public Result<List<TeamMemberVO>> getTeamMembers(@PathVariable Long teamId) { return Result.success(teamService.getTeamMembers(teamId)); }
}
