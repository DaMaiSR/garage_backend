package com.cqupt.garage.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("garage_record")
public class GarageRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String plateNo;

    private String spaceNo;

    private String inTime;

    private String outTime;

    private String parkingMinutes;

    private String totalFee;

    private String payStatus;

    @TableField(exist = false)
    private String payMethod;

    private String recordStatus;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
