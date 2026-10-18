package com.pation.awardnomination.dispute;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service simulating the System Administrator role providing activity logs.
 */
@Service
public class SystemLogService {

    private final SystemActivityLogRepository systemActivityLogRepository;

    @Autowired
    public SystemLogService(SystemActivityLogRepository systemActivityLogRepository) {
        this.systemActivityLogRepository = systemActivityLogRepository;
    }

    public List<SystemActivityLog> getLogsForVoter(String voterUsername) {
        return systemActivityLogRepository.findByVoterUsernameOrderByTimestampDesc(voterUsername);
    }

    public List<SystemActivityLog> getAllRecentLogs() {
        return systemActivityLogRepository.findTop20ByOrderByTimestampDesc();
    }

    public SystemActivityLog recordLog(SystemActivityLog log) {
        return systemActivityLogRepository.save(log);
    }
}
