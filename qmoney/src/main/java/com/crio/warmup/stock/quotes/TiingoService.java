
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {
  private RestTemplate restTemplate=new RestTemplate();
  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement getStockQuote method below that was also declared in the interface.

  // Note:
  // 1. You can move the code from PortfolioManagerImpl#getStockQuote inside newly created method.
  // 2. Run the tests using command below and make sure it passes.
  //    ./gradlew test --tests TiingoServiceTest
  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)throws JsonProcessingException {

    // Check TiingoServiceTest for more visibility
    
    // Don't use this method
    // String url = buildUri(symbol, from, to);
    // TiingoCandle[] results = restTemplate.getForObject(url, TiingoCandle[].class);

    //Use this instead 
    
    String url=buildUri(symbol, from, to);
    String result=restTemplate.getForObject(url,String.class);

    ObjectMapper objMapper=new ObjectMapper();
    objMapper.registerModule(new JavaTimeModule());

    Candle[] candleResult=objMapper.readValue(result, TiingoCandle[].class);
    return Arrays.asList(candleResult);

  }
  //CHECKSTYLE:OFF
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Write a method to create appropriate url to call the Tiingo API.
  public String buildUri(String symbol, LocalDate from, LocalDate to) {

    String url = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?" + "startDate="
    + from + "&endDate=" + to + "&token=" +"4508c3bcaf49e2ab264b712d123eb75780dfc3d5";

    return url;
  }
}
