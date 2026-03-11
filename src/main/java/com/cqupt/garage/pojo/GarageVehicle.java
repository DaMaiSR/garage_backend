package com.cqupt.garage.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("garage_vehicle")
public class GarageVehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String plateNo;

    private String ownerName;

    private String ownerPhone;

    private String vehicleType;

    private String memberType;

    private String bindSpaceNo;

    private String expireDate;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
