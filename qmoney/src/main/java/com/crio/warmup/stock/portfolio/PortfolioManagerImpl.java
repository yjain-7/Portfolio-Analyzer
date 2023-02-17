
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.io.IOException;
import java.net.URISyntaxException;
import com.crio.warmup.stock.Collections;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
// import java.util.concurrent.ExecutionException;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;
// import java.util.concurrent.Future;
// import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {
  private RestTemplate restTemplate;
  private StockQuotesService stockQuotesService;

  protected PortfolioManagerImpl(StockQuotesService stockQuotesService){
    this.stockQuotesService = stockQuotesService;
  }
 


  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws JsonProcessingException {
    ArrayList<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    for(PortfolioTrade trade : portfolioTrades){
      List<Candle> candle = getStockQuote(trade.getSymbol(), trade.getPurchaseDate(), endDate); 
      double buyPrice = candle.get(0).getOpen();
      double sellPrice = candle.get(candle.size()-1).getClose();
      AnnualizedReturn value = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
      annualizedReturns.add(value);
    }
    return annualizedReturns.stream().sorted(getComparator()).collect(Collectors.toList());
    // return annualizedReturns;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    double total_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)/365.24;
    double total_returns = (sellPrice - buyPrice)/buyPrice;
    double annualized_returns = Math.pow((1+total_returns), (1/total_years)) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualized_returns ,total_returns);
 }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)throws JsonProcessingException {
    // RestTemplate restTemplate = new RestTemplate();
    // String url = buildUri(symbol, from, to);
    // TiingoCandle[] results = restTemplate.getForObject(url, TiingoCandle[].class);
    // List<Candle> result = Arrays.asList(results);
    // return result;
      return stockQuotesService.getStockQuote(symbol, from, to);
  }


  public static String buildUri(String symbol, LocalDate from, LocalDate to) {
    String url = "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate="
        + from + "&endDate=" + to + "&token=" + "4508c3bcaf49e2ab264b712d123eb75780dfc3d5";
    return url;

  }





  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}
