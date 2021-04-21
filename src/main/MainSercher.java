package main;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainSercher {
    static final String HOME_PATH = System.getProperty( "user.dir" );
    static final String ROOT_PATH = HOME_PATH + "\\";
    static final SimpleDateFormat DT_FORMAT = new SimpleDateFormat ( "yyyyMMdd HHmmss");
    static final Date TIME = new Date();


    public static void main(String[] args) throws InterruptedException, IOException {
        SearcherProperties properties = getSearcherProperties();
        final int DELAY_TIME = properties.getDelayTime();
/*
		 컴퓨터에 설치된 크롬 버전과 라이브러리 버전을 동하게 맞춰야 됨.
		  - java library: https://www.selenium.dev/  https://www.selenium.dev/downloads/
		  - chrome driver: https://chromedriver.chromium.org/downloads

		  //만약 창을 띄우지 않고 작업하고 싶다면,
		  ChromeOptions options = new ChromeOptions();
		  options.addArguments("headless");
		 */

        System.setProperty("webdriver.chrome.driver", ROOT_PATH + "chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        String baseUrl = "https://www.google.com/search?q=%ED%83%84%EC%86%8C%EB%B0%B0%EC%B6%9C%EA%B6%8C";
        driver.get(baseUrl);


        for(String keyword : properties.getKeywords()) {

            WebElement searchForm = driver.findElements(By.tagName("form")).get(0);
            WebElement searchInputBox = searchForm.findElement(By.tagName("input"));
            searchInputBox.clear();
            searchInputBox.sendKeys(keyword);
            searchForm.submit();

            String currentFileName =  ROOT_PATH  + "result\\" + keyword + "_" + DT_FORMAT.format(TIME)+".txt";

            //뉴스 클릭
            Thread.sleep(DELAY_TIME);
            WebElement btn_news = driver.findElements(By.className("hdtb-mitem")).stream()
                    .filter(
                            webElement -> webElement.getText().contains("뉴스")
                    ).findFirst().orElse(null);

            btn_news.click();

            Thread.sleep(DELAY_TIME);
            WebElement btn_tool = driver.findElement(By.id("hdtb-tls"));

            // System.out.println(">>check: " + btn_tool.getAttribute("aria-expanded"));
            if(btn_tool.getAttribute("aria-expanded").equals("false")) {
                //도구 클릭
                btn_tool.click();

                //모든 날짜 클릭
                Thread.sleep(DELAY_TIME);
                WebElement hdtbMenus = driver.findElement(By.id("hdtbMenus")).findElements(By.tagName("span")).get(2);
                hdtbMenus.click();

                //지난 1일 클릭
                Thread.sleep(DELAY_TIME);
                WebElement btn_oneDay = driver.findElement(By.id("lb")).findElements(By.tagName("g-menu-item")).get(2);
                btn_oneDay.click();
            }

            //하단 페이징 엘리먼트 얻음
            List<WebElement> searchPages = driver.findElements(By.xpath("//div[@role='navigation']/span/table/tbody/tr/td"));

            int curMaxPage = searchPages.size() > properties.getSearchMaxPageNumber()
                    ? properties.getSearchMaxPageNumber()
                    : searchPages.size();
            for (int i = 1; i <= curMaxPage; i++) {
                //1번째 페이지부터 10번째 페이지까지 반복적으로 리스트 내용을 저장함
                Thread.sleep(1000);

                List<WebElement> curSearchList = driver.findElements(By.cssSelector(".g"));

                if(curSearchList.size() > 0) {
                    curSearchList.forEach(webElement -> {
                        String[] split = webElement.getText().split("\n");

                        if (webElement.getText() != null && webElement.getText().trim().length() > 1) {
                            String contents = null;

                            if (webElement.getText().startsWith("웹 검색결과")) {
                                contents = split[2] + "\n" + webElement.findElement(By.tagName("a")).getAttribute("href") + "\n" + split[4] + "\n\n";
                            } else {
                                contents = split[0] + "\n" + webElement.findElement(By.tagName("a")).getAttribute("href") + "\n" + split[2] + "\n\n";
                            }

                            saveContentToFile(contents, currentFileName);
                        }
                    });
                }else {
                    curSearchList = driver.findElements(By.tagName("g-card"));
                    curSearchList.forEach(webElement -> {
                        String[] split = webElement.getText().split("\n");

                        if (webElement.getText() != null && webElement.getText().trim().length() > 1) {
                            String contents = null;

                            contents = split[1] + " - " + split[0] + "\n" + webElement.findElement(By.tagName("a")).getAttribute("href") + "\n" + split[3] + " - " + split[2] + "\n\n";

                            saveContentToFile(contents, currentFileName);
                        }
                    });
                }



                Thread.sleep(DELAY_TIME);

                if((i+1) <= curMaxPage) {
                    driver.findElement(By.xpath("//div[@role='navigation']/span/table/tbody/tr/td/a[@aria-label='Page " + (i + 1) + "']")).click();
                }
            }

            Thread.sleep(DELAY_TIME);
        }
        

    }

    public static SearcherProperties getSearcherProperties() {
        Properties properties = new Properties();
        try (   FileInputStream inputStream = new FileInputStream(ROOT_PATH + "setting.properties");
                InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        {

            properties.load(streamReader);

            return new SearcherProperties(
                    Stream.of(Objects.requireNonNull(properties.getProperty("keywords")).split(","))
                        .map(String::trim)
                        .collect(Collectors.toList()),
                    Integer.parseInt(Objects.requireNonNull(properties.getProperty("searchMaxPageNumber"))),
                    Integer.parseInt(Objects.requireNonNull(properties.getProperty("delayTime")))
            );

        }catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 파일 저장 메서드
     * @param contents 저장할 내용
     */
    public static void saveContentToFile(String contents, String fileName)  {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(contents);
            bw.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
