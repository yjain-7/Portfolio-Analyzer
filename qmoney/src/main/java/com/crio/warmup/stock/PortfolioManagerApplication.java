
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static RestTemplate restTemplate=new RestTemplate();
  public static PortfolioManager portfolioManager=PortfolioManagerFactory.getPortfolioManager(restTemplate);

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    List<String> symbolList = new ArrayList<>();
    File inputFile = resolveFileFromResources(args[0]);
    PortfolioTrade[] trades =
        PortfolioManagerApplication.getObjectMapper().readValue(inputFile, PortfolioTrade[].class);
    for (PortfolioTrade trade : trades) {
      symbolList.add(trade.getSymbol());
    }
    return symbolList;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
        .toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 =
        "/home/crio-user/workspace/jainyash4292-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@815b41f";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "19";


    return Arrays.asList(
        new String[] {valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);
    List<TotalReturnsDto> sort = mainReadQutoesHelper(args, trades);
    Collections.sort(sort, new Comparator<TotalReturnsDto>() {
      @Override
      public int compare(TotalReturnsDto t1, TotalReturnsDto t2) {
        return (int) (t1.getClosingPrice().compareTo(t2.getClosingPrice()));
      }
    });
    List<String> stocks = new ArrayList<>();
    for (TotalReturnsDto trd : sort) {
      stocks.add(trd.getSymbol());
    }

    return stocks;
  }

  public static List<TotalReturnsDto> mainReadQutoesHelper(String[] args,List<PortfolioTrade> trades) throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> test = new ArrayList<>();
    LocalDate localDate = LocalDate.parse(args[1]);
    for (PortfolioTrade t : trades) {

      String url = prepareUrl(t, localDate, "4508c3bcaf49e2ab264b712d123eb75780dfc3d5");
      TiingoCandle[] results = restTemplate.getForObject(url, TiingoCandle[].class);
      System.out.println(results.toString());
      if (results != null) {
        test.add(new TotalReturnsDto(t.getSymbol(), results[results.length - 1].getClose()));
      }
    }
    return test;
  }

  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
    ObjectMapper om = getObjectMapper();
    List<PortfolioTrade> trades =
        Arrays.asList(om.readValue(resolveFileFromResources(filename), PortfolioTrade[].class));
    return trades;
  }

  // TODO:
  // Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    // return Collections.emptyList();
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate="
        + trade.getPurchaseDate().toString() + "&endDate=" + endDate.toString() + "&token=" + token;
    return url;

  }

  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size()-1).getClose();
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    String url = prepareUrl(trade, endDate , token);
    TiingoCandle[] results = restTemplate.getForObject(url, TiingoCandle[].class);
    List<Candle> result = Arrays.asList(results);
    // return Collections.emptyList();
    return result;
  }
  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.
  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)throws IOException, URISyntaxException {
    ArrayList<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    List<PortfolioTrade> trades =  readTradesFromJson(args[0]);
    for(PortfolioTrade trade : trades){
      LocalDate endDate = LocalDate.parse(args[1]);
      List<Candle> candle = fetchCandles(trade, endDate, PortfolioManagerApplication.getToken()); 
      double buyPrice = getOpeningPriceOnStartDate(candle);
      double sellPrice = getClosingPriceOnEndDate(candle);
      AnnualizedReturn value = calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
      annualizedReturns.add(value);
    }
    Collections.sort(annualizedReturns, new Comparator<AnnualizedReturn>() {
      @Override
      public int compare(AnnualizedReturn c1, AnnualizedReturn c2) {
        return (int) (c2.getAnnualizedReturn().compareTo(c1.getAnnualizedReturn()));
      }
    });
    // return Collections.emptyList();
    return annualizedReturns;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,PortfolioTrade trade, Double buyPrice, Double sellPrice) {
     // double total_years = ChronoUnit.YEARS.between(trade.getPurchaseDate(), endDate);
     double total_years = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate)/365.24;
     double total_returns = (sellPrice - buyPrice)/buyPrice;
     // double annualized_returns = Math.pow((1+total_returns), (1/total_years)) - 1;
     double annualized_returns = Math.pow((1+total_returns), (1/total_years)) - 1;
     return new AnnualizedReturn(trade.getSymbol(), annualized_returns ,total_returns);
  }

  // public static void main(String[] args) throws Exception {
  //   Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
  //   ThreadContext.put("runId", UUID.randomUUID().toString());
  //   printJsonObject(mainCalculateSingleReturn(args));

  // }
  public static String getToken() {
    return "4508c3bcaf49e2ab264b712d123eb75780dfc3d5";
  }
// https://api.tiingo.com/tiingo/daily/GOOGL/prices?&endDate=2019-12-20&token=4508c3bcaf49e2ab264b712d123eb75780dfc3d5


  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades=objectMapper.readValue(contents,PortfolioTrade[].class);
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }

  private static String readFileAsString(String file) throws IOException{
    Path fileName= Path.of("C:\\Users\\HP\\Desktop\\gfg.txt");
    String str = Files.readString(fileName);
    return str;
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());
  }
}

