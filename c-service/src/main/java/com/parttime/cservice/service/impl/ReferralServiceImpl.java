package com.parttime.cservice.service.impl;

import com.parttime.cservice.mapper.ReferralCodeMapper;
import com.parttime.cservice.mapper.ReferralRecordMapper;
import com.parttime.cservice.mapper.ReferralRewardMapper;
import com.parttime.cservice.mapper.ReferralConfigMapper;
import com.parttime.cservice.mapper.WorkerMapper;
import com.parttime.cservice.pojo.entity.ReferralCode;
import com.parttime.cservice.pojo.entity.ReferralRecord;
import com.parttime.cservice.pojo.entity.ReferralReward;
import com.parttime.cservice.pojo.entity.ReferralConfig;
import com.parttime.cservice.pojo.entity.Worker;
import com.parttime.cservice.pojo.vo.ReferralLinkVO;
import com.parttime.cservice.pojo.vo.ReferralStatsVO;
import com.parttime.cservice.pojo.vo.RefereeVO;
import com.parttime.cservice.pojo.vo.PageVO;
import com.parttime.cservice.pojo.vo.ReferralRewardVO;
import com.parttime.cservice.pojo.vo.InviteInfoVO;
import com.parttime.cservice.service.ReferralService;
import com.parttime.cservice.service.WeChatSchemeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReferralServiceImpl implements ReferralService {

    @Resource
    private ReferralCodeMapper referralCodeMapper;

    @Resource
    private ReferralRecordMapper referralRecordMapper;

    @Resource
    private ReferralRewardMapper referralRewardMapper;

    @Resource
    private ReferralConfigMapper referralConfigMapper;

    @Resource
    private WeChatSchemeService weChatSchemeService;

    @Resource
    private WorkerMapper workerMapper;

    private static final Logger log = LoggerFactory.getLogger(ReferralServiceImpl.class);

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final DateTimeFormatter BEIJING_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ReferralLinkVO getReferralLink(Long workerId) {
        ReferralCode code = referralCodeMapper.findByWorkerId(workerId);
        if (code == null) {
            code = generateReferralCode(workerId);
        }
        ReferralLinkVO vo = new ReferralLinkVO();
        vo.setCode(code.getCode());
        try {
            String scheme = weChatSchemeService.generateScheme("/pages/register/register", "code=" + code.getCode());
            vo.setLink(scheme);
        } catch (Exception e) {
            log.warn("Failed to generate WeChat scheme, fallback to fake URL: {}", e.getMessage());
            vo.setLink("weixin://dl/business/?t=fallback_" + code.getCode());
        }
        String inviterName = workerMapper.findById(workerId)
                .map(Worker::getName)
                .orElse("用户" + workerId);
        vo.setInviterName(inviterName);
        return vo;
    }

    @Override
    public String getReferralPoster(Long workerId) {
        return null;
    }

    @Override
    @Transactional
    public void bindReferral(Long refereeId, String code) {
        if (code == null || code.isEmpty()) {
            return;
        }

        ReferralCode referralCode = referralCodeMapper.findByCode(code);
        if (referralCode == null) {
            throw new RuntimeException("邀请码无效");
        }

        if (referralCode.getWorkerId().equals(refereeId)) {
            throw new RuntimeException("不能邀请自己");
        }

        ReferralRecord existing = referralRecordMapper.findByRefereeId(refereeId);
        if (existing != null) {
            throw new RuntimeException("您已绑定邀请人");
        }

        ReferralRecord record = new ReferralRecord();
        record.setReferrerId(referralCode.getWorkerId());
        record.setRefereeId(refereeId);
        record.setReferralCode(code);
        referralRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public void checkAndGrantReward(Long refereeId) {
        ReferralRecord record = referralRecordMapper.findByRefereeId(refereeId);
        if (record == null) {
            return;
        }

        ReferralReward existing = referralRewardMapper.findByReferralRecordId(record.getId());
        if (existing != null) {
            return;
        }

        List<ReferralConfig> configs = referralConfigMapper.findAll();
        int validDays = getConfigValue(configs, "valid_days", 30);
        int minWorkCount = getConfigValue(configs, "min_work_count", 3);
        BigDecimal minIncome = getConfigDecimalValue(configs, "min_income", BigDecimal.ZERO);
        boolean needAudit = Boolean.parseBoolean(getConfigStringValue(configs, "need_audit", "true"));

        if (record.getBoundAt().plusDays(validDays).isBefore(LocalDateTime.now())) {
            return;
        }

        // TODO: 查询被邀请人打工次数和收入
        int workCount = 0;
        BigDecimal income = BigDecimal.ZERO;

        if (workCount < minWorkCount || income.compareTo(minIncome) < 0) {
            return;
        }

        BigDecimal rewardAmount = getConfigDecimalValue(configs, "reward_amount", new BigDecimal("20"));

        ReferralReward reward = new ReferralReward();
        reward.setReferralRecordId(record.getId());
        reward.setAmount(rewardAmount);
        reward.setStatus(needAudit ? "PENDING" : "GRANTED");
        if (!needAudit) {
            reward.setGrantedAt(LocalDateTime.now());
        }
        referralRewardMapper.insert(reward);
    }

    @Override
    public ReferralStatsVO getReferralStats(Long workerId) {
        int totalReferees = referralRecordMapper.countByReferrerId(workerId);
        List<ReferralRecord> records = referralRecordMapper.findByReferrerId(workerId);

        List<Long> recordIds = records.stream().map(ReferralRecord::getId).collect(Collectors.toList());
        Map<Long, ReferralReward> rewardByRecordId = recordIds.isEmpty()
                ? Map.of()
                : referralRewardMapper.findByReferralRecordIds(recordIds).stream()
                        .collect(Collectors.toMap(ReferralReward::getReferralRecordId, r -> r));

        BigDecimal totalRewardAmount = BigDecimal.ZERO;
        BigDecimal pendingRewardAmount = BigDecimal.ZERO;

        for (ReferralRecord record : records) {
            ReferralReward reward = rewardByRecordId.get(record.getId());
            if (reward != null) {
                totalRewardAmount = totalRewardAmount.add(reward.getAmount());
                if ("PENDING".equals(reward.getStatus()) || "AUDITING".equals(reward.getStatus())) {
                    pendingRewardAmount = pendingRewardAmount.add(reward.getAmount());
                }
            }
        }

        ReferralStatsVO vo = new ReferralStatsVO();
        vo.setTotalReferees(totalReferees);
        vo.setTotalRewardAmount(totalRewardAmount);
        vo.setPendingRewardAmount(pendingRewardAmount);
        return vo;
    }

    @Override
    public PageVO<RefereeVO> getReferees(Long workerId, int page, int pageSize) {
        int currentPage = page < 1 ? 1 : page;
        int currentPageSize = pageSize < 1 ? 20 : Math.min(pageSize, 100);
        int offset = (currentPage - 1) * currentPageSize;
        List<ReferralRecord> records = referralRecordMapper.findByReferrerIdPage(workerId, offset, currentPageSize);
        long total = referralRecordMapper.countByReferrerId(workerId);

        List<Long> recordIds = records.stream().map(ReferralRecord::getId).collect(Collectors.toList());
        Map<Long, ReferralReward> rewardByRecordId = recordIds.isEmpty()
                ? Map.of()
                : referralRewardMapper.findByReferralRecordIds(recordIds).stream()
                        .collect(Collectors.toMap(ReferralReward::getReferralRecordId, r -> r));

        List<RefereeVO> list = records.stream()
                .map(record -> {
                    RefereeVO vo = new RefereeVO();
                    vo.setId(record.getRefereeId());
                    vo.setName("用户" + record.getRefereeId());
                    vo.setPhone("138****" + (record.getRefereeId() % 10000));
                    vo.setWorkCount(0);
                    vo.setWorkHours(BigDecimal.ZERO);

                    ReferralReward reward = rewardByRecordId.get(record.getId());
                    vo.setRewardStatus(reward != null ? reward.getStatus() : "NOT_QUALIFIED");

                    vo.setBoundAt(record.getBoundAt() != null ? record.getBoundAt().format(BEIJING_FMT) : "");
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageVO<>(list, total);
    }

    @Override
    public PageVO<ReferralRewardVO> getReferralRewards(Long workerId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ReferralReward> rewards = referralRewardMapper.findByReferrerIdPage(workerId, offset, pageSize);
        long total = referralRewardMapper.countByReferrerId(workerId);

        List<ReferralRewardVO> list = rewards.stream().map(reward -> {
            ReferralRewardVO vo = new ReferralRewardVO();
            vo.setId(reward.getId());
            vo.setAmount(reward.getAmount());
            vo.setStatus(reward.getStatus());
            vo.setCreatedAt(reward.getCreatedAt() != null ? reward.getCreatedAt().format(BEIJING_FMT) : "");
            vo.setGrantedAt(reward.getGrantedAt() != null ? reward.getGrantedAt().format(BEIJING_FMT) : "");
            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(list, total);
    }

    @Override
    public List<ReferralConfig> getConfig() {
        return referralConfigMapper.findAll();
    }

    @Override
    public InviteInfoVO getInviteInfo(String code) {
        ReferralCode referralCode = referralCodeMapper.findByCode(code);
        InviteInfoVO vo = new InviteInfoVO();
        if (referralCode == null) {
            vo.setCode(code);
            vo.setInviterName("零工平台");
            vo.setInviterAvatar("");
            vo.setMiniProgramScheme("");
            return vo;
        }
        Worker worker = workerMapper.findById(referralCode.getWorkerId()).orElse(null);
        vo.setCode(code);
        vo.setInviterName(worker != null ? worker.getName() : "零工平台用户");
        vo.setInviterAvatar(worker != null && worker.getAvatarUrl() != null ? worker.getAvatarUrl() : "");
        try {
            String scheme = weChatSchemeService.generateScheme(
                    "pages/index/index",
                    "inviteCode=" + code
            );
            vo.setMiniProgramScheme(scheme);
        } catch (Exception e) {
            log.warn("生成小程序Scheme失败: " + e.getMessage());
            vo.setMiniProgramScheme("");
        }
        try {
            String urlLink = weChatSchemeService.generateUrlLink(
                    "pages/index/index",
                    "inviteCode=" + code
            );
            vo.setMiniProgramUrlLink(urlLink);
        } catch (Exception e) {
            log.warn("生成小程序UrlLink失败: " + e.getMessage());
            vo.setMiniProgramUrlLink("");
        }
        return vo;
    }

    @Override
    @Transactional
    public void updateConfig(List<ReferralConfig> configs) {
        for (ReferralConfig config : configs) {
            referralConfigMapper.upsert(config.getConfigKey(), config.getConfigValue(), config.getDescription());
        }
    }

    private ReferralCode generateReferralCode(Long workerId) {
        String code;
        do {
            code = generateRandomCode();
        } while (referralCodeMapper.findByCode(code) != null);

        ReferralCode referralCode = new ReferralCode();
        referralCode.setWorkerId(workerId);
        referralCode.setCode(code);
        referralCodeMapper.insert(referralCode);
        return referralCode;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private int getConfigValue(List<ReferralConfig> configs, String key, int defaultValue) {
        return configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .map(c -> Integer.parseInt(c.getConfigValue()))
                .findFirst()
                .orElse(defaultValue);
    }

    private BigDecimal getConfigDecimalValue(List<ReferralConfig> configs, String key, BigDecimal defaultValue) {
        return configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .map(c -> new BigDecimal(c.getConfigValue()))
                .findFirst()
                .orElse(defaultValue);
    }

    private String getConfigStringValue(List<ReferralConfig> configs, String key, String defaultValue) {
        return configs.stream()
                .filter(c -> key.equals(c.getConfigKey()))
                .map(ReferralConfig::getConfigValue)
                .findFirst()
                .orElse(defaultValue);
    }
}
