package org.isrpo.services;

import org.isrpo.repositories.DrinkStatisticsRepository;
import org.isrpo.tools.ConsoleLogger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for scheduled cleaning drink statistics that are older 5 years
 */
@Service
public class DrinkStatisticsCleanService {
    private final DrinkStatisticsRepository drinkStatisticsRepository;

    public DrinkStatisticsCleanService(DrinkStatisticsRepository drinkStatisticsRepository) {
        this.drinkStatisticsRepository = drinkStatisticsRepository;
    }


    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupOldStatistics() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusYears(5);
        ConsoleLogger.log("Статистика старше 5 лет удалена", ConsoleLogger.LogLevel.WARNING);
        drinkStatisticsRepository.deleteAllByCreatedAtBefore(cutoffDate);
    }
}
