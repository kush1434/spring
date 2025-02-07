package com.nighthawk.spring_portfolio.mvc.cryptoMining;

public class TestMiningService {
    public static void main(String[] args) {
        // Create a test mining user
        MiningUser testUser = new MiningUser();
        
        // Test initial wallet
        System.out.println("\n=== Initial Wallet Test ===");
        System.out.println("Initial BTC Balance: " + testUser.getBtcBalance());
        System.out.println("Initial Pending Balance: " + testUser.getPendingBalance());

        // Add some GPUs
        GPU testGPU = new GPU();
        testGPU.setName("Test GPU");
        testGPU.setHashRate(60.55);
        testGPU.setPowerConsumption(200);
        testGPU.setTemp(70);
        testGPU.setPrice(1000.0);
        
        testUser.addGPU(testGPU);
        testUser.setMining(true);
        
        // Test mining calculations
        System.out.println("\n=== Mining Setup Test ===");
        System.out.println("Hashrate: " + testUser.getCurrentHashrate() + " MH/s");
        System.out.println("Power Consumption: " + testUser.getPowerConsumption() + " W");
        System.out.println("Average Temperature: " + testUser.getAverageTemperature() + " °C");
        System.out.println("Active GPUs: " + testUser.getActiveGPUs().size());

        // Simulate mining for 1 minute
        System.out.println("\n=== Mining Simulation Test ===");
        double hashrate = testUser.getCurrentHashrate();
        double btcMined = hashrate * 0.00000001; // Same calculation as in MiningService
        int newShares = (int)(hashrate * 0.1);
        
        testUser.setPendingBalance(testUser.getPendingBalance() + btcMined);
        testUser.setShares(testUser.getShares() + newShares);

        System.out.println("BTC Mined in one minute: " + btcMined);
        System.out.println("New Pending Balance: " + testUser.getPendingBalance());
        System.out.println("Total Shares: " + testUser.getShares());

        // Test wallet update
        System.out.println("\n=== Wallet Update Test ===");
        // Simulate moving pending balance to main balance
        double pendingBalance = testUser.getPendingBalance();
        testUser.setBtcBalance(testUser.getBtcBalance() + pendingBalance);
        testUser.setPendingBalance(0.0);
        
        System.out.println("Final BTC Balance: " + testUser.getBtcBalance());
        System.out.println("Final Pending Balance: " + testUser.getPendingBalance());
    }
}