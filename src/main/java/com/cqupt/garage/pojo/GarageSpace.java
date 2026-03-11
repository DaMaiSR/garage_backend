package com.cqupt.garage.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("garage_space")
public class GarageSpace implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String areaName;

    private String floorNo;

    private String spaceNo;

    private String spaceType;

    private String status;

    private Long ownerUserId;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
