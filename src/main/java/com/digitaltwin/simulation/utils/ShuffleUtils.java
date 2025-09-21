package com.digitaltwin.simulation.utils;

import com.digitaltwin.simulation.dto.ExperimentStepDto;
import com.digitaltwin.simulation.dto.ExperimentStepsDto;
import com.digitaltwin.simulation.dto.ExperimentStepsResponseDto;
import com.digitaltwin.simulation.dto.RoleDto;
import com.digitaltwin.simulation.dto.SimulationStepNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 试验步骤乱序工具类
 * 提供对试验步骤数据进行随机排列的功能
 */
public class ShuffleUtils {

    /**
     * 对试验步骤响应数据进行乱序处理
     *
     * @param response 原始响应数据
     * @return 乱序后的响应数据
     */
    public static ExperimentStepsResponseDto shuffleExperimentStepsResponse(ExperimentStepsResponseDto response) {
        if (response == null) {
            return null;
        }

        ExperimentStepsResponseDto shuffledResponse = new ExperimentStepsResponseDto();
        shuffledResponse.setExperimentName(response.getExperimentName());

        // 乱序手动模式数据
        if (response.getManualSteps() != null) {
            shuffledResponse.setManualSteps(shuffleExperimentSteps(response.getManualSteps()));
        }

        return shuffledResponse;
    }

    /**
     * 对手动模式试验步骤进行乱序处理
     *
     * @param stepsDto 原始步骤数据
     * @return 乱序后的步骤数据
     */
    public static ExperimentStepsDto shuffleExperimentSteps(ExperimentStepsDto stepsDto) {
        if (stepsDto == null || stepsDto.getSteps() == null) {
            return stepsDto;
        }

        ExperimentStepsDto shuffledStepsDto = new ExperimentStepsDto();
        shuffledStepsDto.setExperimentName(stepsDto.getExperimentName());
        shuffledStepsDto.setTotalSteps(stepsDto.getTotalSteps());

        // 创建步骤的副本并乱序
        List<ExperimentStepDto> shuffledSteps = new ArrayList<>();
        for (ExperimentStepDto step : stepsDto.getSteps()) {
            shuffledSteps.add(shuffleExperimentStep(step));
        }

        // 对大步骤进行随机排列
        Collections.shuffle(shuffledSteps);
        shuffledStepsDto.setSteps(shuffledSteps);

        return shuffledStepsDto;
    }

    /**
     * 对单个试验步骤进行乱序处理
     *
     * @param stepDto 原始步骤
     * @return 乱序后的步骤
     */
    private static ExperimentStepDto shuffleExperimentStep(ExperimentStepDto stepDto) {
        if (stepDto == null || stepDto.getRoles() == null) {
            return stepDto;
        }

        ExperimentStepDto shuffledStep = new ExperimentStepDto();
        shuffledStep.setStepId(stepDto.getStepId());
        shuffledStep.setStepName(stepDto.getStepName());

        // 对每个角色的任务进行乱序，但保持角色顺序不变
        List<RoleDto> shuffledRoles = new ArrayList<>();
        for (RoleDto role : stepDto.getRoles()) {
            shuffledRoles.add(shuffleRole(role));
        }
        shuffledStep.setRoles(shuffledRoles);

        return shuffledStep;
    }

    /**
     * 对角色的任务进行乱序处理（保持角色本身顺序不变）
     *
     * @param roleDto 原始角色
     * @return 乱序后的角色
     */
    private static RoleDto shuffleRole(RoleDto roleDto) {
        if (roleDto == null || roleDto.getTasks() == null) {
            return roleDto;
        }

        RoleDto shuffledRole = new RoleDto();
        shuffledRole.setRoleId(roleDto.getRoleId());
        shuffledRole.setRoleName(roleDto.getRoleName());

        // 对任务进行乱序（仅第一层，不影响子任务）
        List<SimulationStepNode> shuffledTasks = shuffleSimulationStepNodeList(roleDto.getTasks());
        shuffledRole.setTasks(shuffledTasks);

        return shuffledRole;
    }

    /**
     * 对SimulationStepNode列表进行乱序处理
     *
     * @param nodeList 原始节点列表
     * @return 乱序后的节点列表
     */
    public static List<SimulationStepNode> shuffleSimulationStepNodeList(List<SimulationStepNode> nodeList) {
        if (nodeList == null || nodeList.isEmpty()) {
            return nodeList;
        }

        List<SimulationStepNode> shuffledNodes = new ArrayList<>();
        for (SimulationStepNode node : nodeList) {
            shuffledNodes.add(shuffleSimulationStepNode(node));
        }

        // 对节点列表进行随机排列
        Collections.shuffle(shuffledNodes);

        return shuffledNodes;
    }

    /**
     * 对单个SimulationStepNode进行乱序处理
     * 只对第一层子节点进行乱序，不递归处理更深层次的子节点
     *
     * @param node 原始节点
     * @return 乱序后的节点
     */
    private static SimulationStepNode shuffleSimulationStepNode(SimulationStepNode node) {
        if (node == null) {
            return null;
        }

        SimulationStepNode shuffledNode = new SimulationStepNode();
        shuffledNode.setName(node.getName());
        shuffledNode.setUe(node.getUe());
        shuffledNode.setSetting(node.getSetting());
        shuffledNode.setCheckBoxs(node.getCheckBoxs());
        shuffledNode.setInputList(node.getInputList());

        // 仅对第一层子节点进行乱序，不递归处理
        if (node.getChild() != null && !node.getChild().isEmpty()) {
            List<SimulationStepNode> shuffledChildren = new ArrayList<>(node.getChild());
            Collections.shuffle(shuffledChildren);
            shuffledNode.setChild(shuffledChildren);
        }

        return shuffledNode;
    }
}