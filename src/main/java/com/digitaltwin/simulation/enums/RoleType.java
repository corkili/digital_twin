package com.digitaltwin.simulation.enums;

/**
 * 试验步骤角色类型枚举
 */
public enum RoleType {
    DATA_OPERATOR("data_operator"),
    COMMANDER("commander");

    private final String displayName;

    RoleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据显示名称获取角色类型
     * @param displayName 显示名称
     * @return 角色类型，如果没有匹配则返回null
     */
    public static RoleType fromDisplayName(String displayName) {
        for (RoleType roleType : values()) {
            if (roleType.displayName.equals(displayName)) {
                return roleType;
            }
        }
        return null;
    }
}