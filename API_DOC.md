# API_DOC

统一返回结构：
```json
{
  "flag": true,
  "message": "optional",
  "data": {}
}
```

鉴权头：
- `token: <JWT>`
- 开放集成接口额外使用：`X-Integration-Key: <integration.access-key>`

## 1. 用户
- `GET /user/login?username=&password=`
- `POST /user/register`
- `GET /user/logout`
- `GET /user/profileSummary`
- `POST /user/updateProfile`

## 2. 车位
- `GET /garageSpace/listGarageSpacePage`
- `GET /garageSpace/listMyGarageSpacePage`
- `POST /garageSpace/addGarageSpace`（admin）
- `POST /garageSpace/updateGarageSpace`（admin）
- `GET /garageSpace/delGarageSpace?id=`（admin）

## 3. 车辆
- `GET /garageVehicle/listGarageVehiclePage`
- `GET /garageVehicle/listGarageVehicle`
- `POST /garageVehicle/addGarageVehicle`
- `POST /garageVehicle/updateGarageVehicle`
- `GET /garageVehicle/delGarageVehicle?id=`

## 4. 驾驶档案
- `GET /driverProfile/listDriverProfilePage`
- `POST /driverProfile/addDriverProfile`
- `POST /driverProfile/updateDriverProfile`
- `GET /driverProfile/delDriverProfile?id=`

## 5. 停车记录
- `GET /garageRecord/listGarageRecordPage`
- `POST /garageRecord/addGarageInRecord`
- `POST /garageRecord/updateGarageOutRecord`

## 6. 预约
- `GET /garageReservation/listGarageReservationPage`
- `POST /garageReservation/createReservation`
- `POST /garageReservation/cancelReservation`
- `POST /garageReservation/checkInReservation`（admin）

## 7. 支付编排（扩展）
- `POST /payment/createCheckoutOrder`
- `GET /payment/queryCheckoutOrder?orderNo=`
- `POST /payment/completeCheckout`

## 8. 开放集成（扩展）
- `POST /open/integration/plate/analyze`
- `POST /open/integration/payment/mock/paid`

## 9. 实时监控
- `GET /garageMonitor/realtime`（admin）
- `WS /ws/realtime?token=JWT`（admin）
