package com.cqupt.garage.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("garage_reservation")
public class GarageReservation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long vehicleId;

    private String plateNo;

    private String spaceNo;

    private String reservationTime;

    private String reservationStatus;

    private String checkInTime;

    private String cancelTime;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
