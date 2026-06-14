package com.parttime.enterprise.service.impl;

import com.parttime.enterprise.exception.BusinessException;
import com.parttime.enterprise.mapper.CompanyWorkerMapper;
import com.parttime.enterprise.mapper.WorkerSyncMapper;
import com.parttime.enterprise.pojo.entity.CompanyWorker;
import com.parttime.enterprise.pojo.vo.WorkerListVO;
import com.parttime.enterprise.service.CompanyWorkerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CompanyWorkerServiceImpl implements CompanyWorkerService {

    @Resource
    private CompanyWorkerMapper companyWorkerMapper;
    @Resource
    private WorkerSyncMapper workerSyncMapper;

    @Override
    public List<WorkerListVO> list(Long companyId, String keyword) {
        List<CompanyWorker> list = companyWorkerMapper.findByCompanyId(companyId, keyword);
        List<Long> workerIds = list.stream().map(CompanyWorker::getWorkerId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> nameMap = new HashMap<>();
        Map<Long, String> phoneMap = new HashMap<>();
        Map<Long, String> genderMap = new HashMap<>();
        Map<Long, Integer> ageMap = new HashMap<>();
        Map<Long, String> realNameStatusMap = new HashMap<>();
        if (!workerIds.isEmpty()) {
            workerSyncMapper.findWorkerNamesByIds(workerIds).forEach(m -> nameMap.put((Long) m.get("id"), (String) m.get("name")));
            workerSyncMapper.findWorkerPhonesByIds(workerIds).forEach(m -> phoneMap.put((Long) m.get("id"), (String) m.get("phone")));
            workerSyncMapper.findWorkerGendersByIds(workerIds).forEach(m -> genderMap.put((Long) m.get("worker_id"), (String) m.get("gender")));
            workerSyncMapper.findWorkerRealNameStatusesByIds(workerIds).forEach(m -> realNameStatusMap.put((Long) m.get("worker_id"), (String) m.get("status")));
            workerSyncMapper.findWorkerBirthdaysByIds(workerIds).forEach(m -> {
                Long workerId = (Long) m.get("worker_id");
                LocalDate birthday = toLocalDate(m.get("birthday"));
                if (birthday != null) ageMap.put(workerId, LocalDate.now().getYear() - birthday.getYear());
            });
        }
        return list.stream().map(cw -> toVO(cw, nameMap, phoneMap, genderMap, ageMap, realNameStatusMap)).collect(Collectors.toList());
    }

    @Override
    public WorkerListVO detail(Long id) {
        CompanyWorker cw = companyWorkerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("CompanyWorker not found: " + id));
        return toVO(cw);
    }

    @Override
    public void addWorker(Long companyId, Long workerId) {
        companyWorkerMapper.upsert(companyId, workerId);
    }

    @Override
    public void blacklist(Long id) {
        CompanyWorker cw = companyWorkerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("CompanyWorker not found: " + id));
        companyWorkerMapper.updateStatus(id, "BLACKLISTED");
    }

    @Override
    public void unblacklist(Long id) {
        CompanyWorker cw = companyWorkerMapper.findById(id)
                .orElseThrow(() -> new BusinessException("CompanyWorker not found: " + id));
        companyWorkerMapper.updateStatus(id, "ACTIVE");
    }

    private WorkerListVO toVO(CompanyWorker cw) {
        WorkerListVO vo = baseVO(cw);
        String name = workerSyncMapper.findWorkerNameById(cw.getWorkerId());
        vo.setName(name);
        String phone = workerSyncMapper.findWorkerPhoneById(cw.getWorkerId());
        vo.setPhone(phone);
        vo.setWorkerGender(workerSyncMapper.findWorkerGenderById(cw.getWorkerId()));
        vo.setRealNameStatus(workerSyncMapper.findWorkerRealNameStatusById(cw.getWorkerId()));
        LocalDate birthday = workerSyncMapper.findWorkerBirthdayById(cw.getWorkerId());
        if (birthday != null) {
            vo.setWorkerAge(LocalDate.now().getYear() - birthday.getYear());
        }
        return vo;
    }

    private WorkerListVO toVO(CompanyWorker cw, Map<Long, String> nameMap, Map<Long, String> phoneMap, Map<Long, String> genderMap, Map<Long, Integer> ageMap, Map<Long, String> realNameStatusMap) {
        WorkerListVO vo = baseVO(cw);
        Long workerId = cw.getWorkerId();
        vo.setName(nameMap.get(workerId));
        vo.setPhone(phoneMap.get(workerId));
        vo.setWorkerGender(genderMap.get(workerId));
        vo.setWorkerAge(ageMap.get(workerId));
        vo.setRealNameStatus(realNameStatusMap.getOrDefault(workerId, "NONE"));
        return vo;
    }

    private WorkerListVO baseVO(CompanyWorker cw) {
        WorkerListVO vo = new WorkerListVO();
        vo.setId(cw.getId());
        vo.setWorkerId(cw.getWorkerId());
        vo.setStatus(cw.getStatus());
        vo.setFirstContactAt(cw.getFirstContactAt());
        vo.setLastContactAt(cw.getLastContactAt());
        return vo;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Date date) return date.toLocalDate();
        if (value instanceof LocalDate date) return date;
        return null;
    }
}
