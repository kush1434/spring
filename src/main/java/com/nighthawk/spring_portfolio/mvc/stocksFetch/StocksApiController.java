package com.nighthawk.spring_portfolio.mvc.stocksFetch;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/stocks")
public class StocksApiController {

    @Value("${yahoofinance.quotesquery1v8.enabled:false}")
    private boolean isV8Enabled;

    @GetMapping("/{symbol}")
    public ResponseEntity<?> getStockBySymbol(@PathVariable String symbol) {
        String url = isV8Enabled
                ? "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol
                : "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:100.0) Gecko/20100101 Firefox/100.0");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode() == HttpStatus.OK) {
                    System.out.println("Successfully fetched stock data for: " + symbol);
                    return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
                } else {
                    System.out.println("No stock data returned for symbol: " + symbol);
                    return new ResponseEntity<>("Stock not found for symbol: " + symbol, HttpStatus.NOT_FOUND);
                }

            } catch (HttpClientErrorException.TooManyRequests e) {
                retryCount++;
                System.out.println("Rate limited! Retrying... Attempt: " + retryCount);
                
                try {
                    TimeUnit.SECONDS.sleep((long) Math.pow(2, retryCount)); // Exponential backoff
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return new ResponseEntity<>("Thread interrupted while retrying", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } catch (Exception e) {
                System.out.println("Error occurred while fetching stock data: " + e.getMessage());
                e.printStackTrace();
                return new ResponseEntity<>("Failed to retrieve stock data for " + symbol, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<>("Too many failed attempts to fetch stock data for " + symbol, HttpStatus.TOO_MANY_REQUESTS);
    }
}
