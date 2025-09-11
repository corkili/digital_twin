package com.digitaltwin.simulation.dto.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.digitaltwin.simulation.dto.SimulationStepNode;
import com.digitaltwin.simulation.dto.StepSetting;
import com.digitaltwin.simulation.dto.CheckBoxItem;
import com.digitaltwin.simulation.dto.InputItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SimulationStepNode 自定义反序列化器
 * 处理字符串值被错误传递到对象字段的情况
 */
public class SimulationStepNodeDeserializer extends JsonDeserializer<SimulationStepNode> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public SimulationStepNode deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        JsonNode node = p.getCodec().readTree(p);
        
        // 如果是字符串值，创建一个只有name属性的节点
        if (node.isTextual()) {
            SimulationStepNode stepNode = new SimulationStepNode();
            stepNode.setName(node.asText());
            return stepNode;
        }
        
        // 如果不是对象，返回空节点
        if (!node.isObject()) {
            return new SimulationStepNode();
        }
        
        // 正常的对象反序列化
        SimulationStepNode stepNode = new SimulationStepNode();
        
        // 基本字段
        if (node.has("name")) {
            stepNode.setName(node.get("name").asText());
        }
        if (node.has("ue")) {
            stepNode.setUe(node.get("ue").asText());
        }
        
        // 子节点数组
        if (node.has("child")) {
            JsonNode childNode = node.get("child");
            if (childNode.isArray()) {
                List<SimulationStepNode> children = new ArrayList<>();
                for (JsonNode child : childNode) {
                    // 递归反序列化子节点
                    SimulationStepNode childStepNode = deserialize(child.traverse(p.getCodec()), ctxt);
                    children.add(childStepNode);
                }
                stepNode.setChild(children);
            }
        }
        
        // 设置配置
        if (node.has("setting")) {
            JsonNode settingNode = node.get("setting");
            if (settingNode.isObject()) {
                StepSetting setting = objectMapper.treeToValue(settingNode, StepSetting.class);
                stepNode.setSetting(setting);
            }
        }
        
        // 复选框组
        if (node.has("checkBoxs")) {
            JsonNode checkBoxsNode = node.get("checkBoxs");
            if (checkBoxsNode.isArray()) {
                List<CheckBoxItem> checkBoxs = new ArrayList<>();
                for (JsonNode checkBox : checkBoxsNode) {
                    CheckBoxItem item = objectMapper.treeToValue(checkBox, CheckBoxItem.class);
                    checkBoxs.add(item);
                }
                stepNode.setCheckBoxs(checkBoxs);
            }
        }
        
        // 输入项组
        if (node.has("inputList")) {
            JsonNode inputListNode = node.get("inputList");
            if (inputListNode.isArray()) {
                List<InputItem> inputList = new ArrayList<>();
                for (JsonNode input : inputListNode) {
                    InputItem item = objectMapper.treeToValue(input, InputItem.class);
                    inputList.add(item);
                }
                stepNode.setInputList(inputList);
            }
        }
        
        return stepNode;
    }
}