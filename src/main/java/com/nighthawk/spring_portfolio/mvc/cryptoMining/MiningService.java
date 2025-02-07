package com.nighthawk.spring_portfolio.mvc.cryptoMining;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import java.util.Map;

@Service
public class MiningService {
    @Autowired
    private MiningUserRepository miningUserRepository;
    
    @Autowired
    private GPURepository gpuRepository;

    private static final double costPerKWh = 0.12; // Example value, adjust as needed

    @Scheduled(fixedRate = 60000) // Every minute
    @Transactional
    public void processMining() {
        List<MiningUser> activeMiners = miningUserRepository.findAll().stream()
            .filter(MiningUser::isMining)
            .collect(Collectors.toList());
            
        System.out.println("Processing mining for " + activeMiners.size() + " active miners");
            
        for (MiningUser miner : activeMiners) {
            double hashrate = miner.getCurrentHashrate();
            
            double btcMined = hashrate * 0.00000001;
            int newShares = (int)(hashrate * 0.1);
            
            miner.setPendingBalance(miner.getPendingBalance() + btcMined);
            miner.setShares(miner.getShares() + newShares);
            
            miningUserRepository.save(miner);
            
            System.out.println("Mining Update for user: " + miner.getPerson().getEmail());
            System.out.println("Active GPUs: " + miner.getActiveGPUs().size());
            System.out.println("Total Hashrate: " + hashrate + " MH/s");
            System.out.println("BTC Mined this minute: " + btcMined);
            System.out.println("New Pending Balance: " + miner.getPendingBalance());
            System.out.println("Total Shares: " + miner.getShares());
        }
    }

    @Scheduled(fixedRate = 3600000) // Runs every hour (3600000 ms)... change the time to test
    @Transactional
    public void processPendingBalances() {
        List<MiningUser> miners = miningUserRepository.findAll();
        
        for (MiningUser miner : miners) {
            double pending = miner.getPendingBalance();
            if (pending > 0) {
                // Transfer pending balance to main balance
                miner.setBtcBalance(miner.getBtcBalance() + pending);
                miner.setPendingBalance(0.0);
                
                System.out.println("Balance Update for user: " + miner.getPerson().getEmail());
                System.out.println("Transferred pending balance: " + pending);
                System.out.println("New BTC Balance: " + miner.getBtcBalance());
                
                miningUserRepository.save(miner);
            }
        }
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
        double totalRevenue = 0.0;
        double totalPowerCost = 0.0;
    
        for (GPU gpu : miner.getActiveGPUs()) {
            double dailyRevenue = (gpu.getHashRate() * 86400) * 0.00000001; // Adjust as needed
            double dailyPowerCost = (gpu.getPowerConsumption() * 24) * costPerKWh; // Adjust costPerKWh as needed
    
            totalRevenue += dailyRevenue;
            totalPowerCost += dailyPowerCost;
        }
    
        miner.setDailyRevenue(totalRevenue);
        miner.setPowerCost(totalPowerCost);
    }
}