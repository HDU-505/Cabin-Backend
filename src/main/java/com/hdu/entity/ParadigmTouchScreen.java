package com.hdu.entity;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author DZL
 * @since 2024-05-28
 */
public class ParadigmTouchScreen implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 范式名称
     */
    private String name;

    /**
     * 范式描述
     */
    private String description;

    /**
     * 封面路径
     */
    private String coverPath;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Paradigm{" +
            "id = " + id +
            ", name = " + name +
            ", coverPath = " + coverPath +
            ", filePath = " + filePath +
            ", createTime = " + createTime +
            ", updateTime = " + updateTime +
        "}";
    }
}
