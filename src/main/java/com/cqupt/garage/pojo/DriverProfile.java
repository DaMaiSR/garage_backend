package com.cqupt.garage.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("driver_profile")
public class DriverProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String driverName;

    private String licenseNo;

    private String licenseType;

    private String validUntil;

    private String phone;

    private String status;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
