package com.nighthawk.spring_portfolio.mvc.cryptoMining;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import java.util.Map;
import java.util.Date;

// Add these imports
import com.nighthawk.spring_portfolio.mvc.userStocks.UserStocksRepository;
import com.nighthawk.spring_portfolio.mvc.userStocks.userStocksTable;

@Service
@EnableScheduling
public class MiningService {
    @Autowired
    private MiningUserRepository miningUserRepository;
    
    @Autowired
    private GPURepository gpuRepository;

    @Autowired
    private UserStocksRepository userStocksRepo;

    // Fine-tune constants
    public static final double HASH_TO_BTC_RATE = 0.0001; // Current rate
    public static final double DIFFICULTY_FACTOR = 1.0;
    public static final int MINING_INTERVAL = 10000; // 10 seconds
    public static final int BALANCE_TRANSFER_INTERVAL = 60000; // 10 sec
    public static final double ELECTRICITY_RATE = 0.12; // USD per kWh
    public static final double BTC_PRICE = 45000.0;

    @Scheduled(fixedRate = MINING_INTERVAL)
    @Transactional
    public void processMining() {

        
        List<MiningUser> activeMiners = miningUserRepository.findAll().stream()
            .filter(user -> user.isMining() && !user.getActiveGPUs().isEmpty())
            .collect(Collectors.toList());


        activeMiners.forEach(miner -> {
            double hashrate = miner.getCurrentHashrate();
            

            if (hashrate <= 0) {
                return;
            }

            // Calculate mining reward with more detailed logging
            double btcMined = hashrate * HASH_TO_BTC_RATE / 60.0;
            
            // Get current balances for logging
            double oldPending = miner.getPendingBalance();
            double newPending = oldPending + btcMined;
            
            // Update balances and stats
            miner.setPendingBalance(newPending);
            miner.setTotalBtcEarned(miner.getTotalBtcEarned() + btcMined);
            miner.setShares(miner.getShares() + 1);
            miner.setTotalSharesMined(miner.getTotalSharesMined() + 1);
            
            
            // Save changes
            try {
                miningUserRepository.save(miner);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Scheduled(fixedRate = BALANCE_TRANSFER_INTERVAL)
    @Transactional
    public void processPendingBalances() {
        
        List<MiningUser> miners = miningUserRepository.findAll();
        
        miners.forEach(miner -> {
            double pending = miner.getPendingBalance();
            if (pending > 0) {
                try {
                    // First, transfer BTC from pending to confirmed
                    double currentConfirmed = miner.getBtcBalance();
                    double newConfirmed = currentConfirmed + pending;
                    miner.setBtcBalance(newConfirmed);
                    
                    // Calculate USD value
                    double pendingUSD = pending * BTC_PRICE;
                    
                    // Get user's stocks/balance record for USD update
                    userStocksTable userStocks = userStocksRepo.findByEmail(miner.getPerson().getEmail());
                    if (userStocks != null) {
                        double oldBalance = Double.parseDouble(userStocks.getBalance());
                        double newBalance = oldBalance + pendingUSD;
                        
                        // Update USD balance
                        userStocks.setBalance(String.format("%.2f", newBalance));
                        userStocksRepo.save(userStocks);
                    }
                    
                    // Clear pending BTC
                    miner.setPendingBalance(0.0);
                    miningUserRepository.save(miner);
                    

                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Map<String, Object> buyGPU(MiningUser user, Long gpuId) {
        GPU gpu = gpuRepository.findById(gpuId).orElse(null);
        if (gpu == null) {
            return Map.of("success", false, "message", "GPU not found");
        }

        user.addGPU(gpu);
        miningUserRepository.save(user);

        return Map.of(
            "success", true,
            "message", "Successfully purchased " + gpu.getName(),
            "newBalance", user.getBtcBalance()
        );
    }

    public void calculateProfitability(MiningUser miner) {
        double hashrate = miner.getCurrentHashrate();
        
        // Calculate daily earnings (24 hours)
        double dailyBTC = hashrate * HASH_TO_BTC_RATE * 24.0;
        double dailyUSD = dailyBTC * BTC_PRICE;
        
        // Calculate power costs
        double dailyPowerKWH = miner.getActiveGPUs().stream()
            .mapToDouble(gpu -> gpu.getPowerConsumption() * 24.0 / 1000.0)
            .sum();
        double dailyPowerCost = dailyPowerKWH * ELECTRICITY_RATE;
        
        // Set values
        miner.setDailyRevenue(dailyUSD);
        miner.setPowerCost(dailyPowerCost);
        

    }
}