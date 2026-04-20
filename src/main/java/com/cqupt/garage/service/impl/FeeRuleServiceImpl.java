package com.cqupt.garage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cqupt.garage.mapper.FeeRuleConfigMapper;
import com.cqupt.garage.pojo.FeeRuleConfig;
import com.cqupt.garage.pojo.User;
import com.cqupt.garage.service.FeeRuleService;
import com.cqupt.garage.service.UserService;
import com.cqupt.garage.utils.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class FeeRuleServiceImpl implements FeeRuleService {

    private static final int DEFAULT_FREE_MINUTES = 0;
    private static final BigDecimal DEFAULT_HOURLY_RATE = new BigDecimal("5.00");

    @Autowired
    private FeeRuleConfigMapper feeRuleConfigMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private volatile boolean feeRuleTableChecked = false;

    @Override
    public ResultVo<Object> getCurrentFeeRule() {
        return ResultVo.ok(toViewMap(loadEffectiveRule()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> updateCurrentFeeRule(Integer freeMinutes, String hourlyRate, String capAmount, String remark) {
        ensureFeeRuleTable();

        User currentUser = userService.getCurrentLoginUser();
        if (!userService.isAdmin(currentUser)) {
            return ResultVo.fail("仅管理员可配置费率");
        }

        Integer normalizedFreeMinutes = freeMinutes == null ? DEFAULT_FREE_MINUTES : freeMinutes;
        if (normalizedFreeMinutes < 0) {
            return ResultVo.fail("免费时长不能小于0");
        }

        BigDecimal normalizedHourlyRate = isBlank(hourlyRate) ? DEFAULT_HOURLY_RATE : parseDecimal(hourlyRate);
        if (normalizedHourlyRate == null) {
            return ResultVo.fail("每小时收费格式不正确");
        }
        if (normalizedHourlyRate.compareTo(BigDecimal.ZERO) < 0) {
            return ResultVo.fail("每小时收费不能小于0");
        }
        normalizedHourlyRate = normalizedHourlyRate.setScale(2, RoundingMode.HALF_UP);

        BigDecimal normalizedCapAmount = null;
        if (!isBlank(capAmount)) {
            normalizedCapAmount = parseDecimal(capAmount);
            if (normalizedCapAmount == null) {
                return ResultVo.fail("封顶金额格式不正确");
            }
            if (normalizedCapAmount.compareTo(BigDecimal.ZERO) < 0) {
                return ResultVo.fail("封顶金额不能小于0");
            }
            normalizedCapAmount = normalizedCapAmount.setScale(2, RoundingMode.HALF_UP);
        }

        if (!isBlank(remark) && remark.trim().length() > 255) {
            return ResultVo.fail("备注长度不能超过255");
        }

        FeeRuleConfig dbRule = null;
        try {
            QueryWrapper<FeeRuleConfig> query = new QueryWrapper<FeeRuleConfig>();
            query.eq("status", "1").orderByDesc("id").last("limit 1");
            dbRule = feeRuleConfigMapper.selectOne(query);
        } catch (Exception ignored) {
            // table may not exist yet
        }

        LocalDateTime now = LocalDateTime.now();
        if (dbRule == null) {
            dbRule = new FeeRuleConfig();
            dbRule.setFreeMinutes(normalizedFreeMinutes);
            dbRule.setHourlyRate(normalizedHourlyRate);
            dbRule.setCapAmount(normalizedCapAmount);
            dbRule.setStatus("1");
            dbRule.setRemark(isBlank(remark) ? null : remark.trim());
            dbRule.setCreateTime(now);
            dbRule.setUpdateTime(now);
            feeRuleConfigMapper.insert(dbRule);
        } else {
            dbRule.setFreeMinutes(normalizedFreeMinutes);
            dbRule.setHourlyRate(normalizedHourlyRate);
            dbRule.setCapAmount(normalizedCapAmount);
            dbRule.setStatus("1");
            dbRule.setRemark(isBlank(remark) ? null : remark.trim());
            dbRule.setUpdateTime(now);
            feeRuleConfigMapper.updateById(dbRule);
        }

        return ResultVo.ok(toViewMap(loadEffectiveRule()), "费率配置更新成功");
    }

    @Override
    public String calcFeeByMinutes(long minutes) {
        if (minutes <= 0) {
            return "0";
        }
        FeeRuleConfig rule = loadEffectiveRule();
        int freeMinutes = rule.getFreeMinutes() == null ? DEFAULT_FREE_MINUTES : Math.max(0, rule.getFreeMinutes());
        if (minutes <= freeMinutes) {
            return "0";
        }
        long billableMinutes = minutes - freeMinutes;
        long hours = (billableMinutes + 59) / 60;

        BigDecimal hourlyRate = rule.getHourlyRate() == null ? DEFAULT_HOURLY_RATE : rule.getHourlyRate();
        if (hourlyRate.compareTo(BigDecimal.ZERO) <= 0) {
            return "0";
        }

        BigDecimal fee = hourlyRate.multiply(BigDecimal.valueOf(hours)).setScale(2, RoundingMode.HALF_UP);
        if (rule.getCapAmount() != null && rule.getCapAmount().compareTo(BigDecimal.ZERO) >= 0
                && fee.compareTo(rule.getCapAmount()) > 0) {
            fee = rule.getCapAmount();
        }
        return fee.stripTrailingZeros().toPlainString();
    }

    private FeeRuleConfig loadEffectiveRule() {
        ensureFeeRuleTable();

        try {
            QueryWrapper<FeeRuleConfig> query = new QueryWrapper<FeeRuleConfig>();
            query.eq("status", "1").orderByDesc("id").last("limit 1");
            FeeRuleConfig dbRule = feeRuleConfigMapper.selectOne(query);
            if (dbRule != null) {
                if (dbRule.getFreeMinutes() == null || dbRule.getFreeMinutes() < 0) {
                    dbRule.setFreeMinutes(DEFAULT_FREE_MINUTES);
                }
                if (dbRule.getHourlyRate() == null || dbRule.getHourlyRate().compareTo(BigDecimal.ZERO) < 0) {
                    dbRule.setHourlyRate(DEFAULT_HOURLY_RATE);
                }
                if (dbRule.getCapAmount() != null && dbRule.getCapAmount().compareTo(BigDecimal.ZERO) < 0) {
                    dbRule.setCapAmount(null);
                }
                return dbRule;
            }
        } catch (Exception ignored) {
            // keep backward compatible when table does not exist
        }

        FeeRuleConfig fallback = new FeeRuleConfig();
        fallback.setId(0L);
        fallback.setFreeMinutes(DEFAULT_FREE_MINUTES);
        fallback.setHourlyRate(DEFAULT_HOURLY_RATE);
        fallback.setCapAmount(null);
        fallback.setStatus("1");
        fallback.setRemark("default rule");
        return fallback;
    }

    private Map<String, Object> toViewMap(FeeRuleConfig rule) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("id", rule.getId());
        map.put("freeMinutes", rule.getFreeMinutes());
        map.put("hourlyRate", rule.getHourlyRate() == null ? DEFAULT_HOURLY_RATE.stripTrailingZeros().toPlainString()
                : rule.getHourlyRate().stripTrailingZeros().toPlainString());
        map.put("capAmount", rule.getCapAmount() == null ? null : rule.getCapAmount().stripTrailingZeros().toPlainString());
        map.put("status", rule.getStatus());
        map.put("remark", rule.getRemark());
        return map;
    }

    private BigDecimal parseDecimal(String text) {
        if (isBlank(text)) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void ensureFeeRuleTable() {
        if (feeRuleTableChecked) {
            return;
        }
        synchronized (this) {
            if (feeRuleTableChecked) {
                return;
            }
            boolean createSuccess = false;
            try {
                jdbcTemplate.execute(
                        "CREATE TABLE IF NOT EXISTS fee_rule_config ("
                                + "id BIGINT PRIMARY KEY AUTO_INCREMENT,"
                                + "free_minutes INT DEFAULT 0,"
                                + "hourly_rate DECIMAL(10,2) NOT NULL DEFAULT 5.00,"
                                + "cap_amount DECIMAL(10,2) DEFAULT NULL,"
                                + "status VARCHAR(16) DEFAULT '1',"
                                + "remark VARCHAR(255),"
                                + "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                                + "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                                + "INDEX idx_fee_rule_status (status)"
                                + ")"
                );
                createSuccess = true;
            } catch (Exception ignored) {
                // keep backward compatible when CREATE TABLE fails
            }
            if (createSuccess) {
                feeRuleTableChecked = true;
            }
        }
    }
}
