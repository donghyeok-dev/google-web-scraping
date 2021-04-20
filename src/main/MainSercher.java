package main;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainSercher {
    static final String USER_HOME_PATH = System.getProperty( "user.home" );
    static final String ROOT_PATH = USER_HOME_PATH + "\\ChromeFinder\\";
    static final SimpleDateFormat DT_FORMAT = new SimpleDateFormat ( "yyyyMMdd HHmmss");
    static final Date TIME = new Date();
    static final String FILE_NAME = ROOT_PATH + "구글_최근1일_배출권_검색결과_" + DT_FORMAT.format(TIME)+".txt";

    public static void main(String[] args) throws InterruptedException {

/*
		 컴퓨터에 설치된 크롬 버전과 라이브러리 버전을 동하게 맞춰야 됨.
		  - java library: https://www.selenium.dev/  https://www.selenium.dev/downloads/
		  - chrome driver: https://chromedriver.chromium.org/downloads

		  //만약 창을 띄우지 않고 작업하고 싶다면,
		  ChromeOptions options = new ChromeOptions();
		  options.addArguments("headless");
		 */
        System.out.println("USER_HOME_PATH: " + USER_HOME_PATH);
        System.setProperty("webdriver.chrome.driver", ROOT_PATH + "chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        String baseUrl = "https://www.google.com/search?q=%ED%83%84%EC%86%8C%EB%B0%B0%EC%B6%9C%EA%B6%8C";
        driver.get(baseUrl);

        //도구 클릭
        Thread.sleep(500);
        WebElement btn_tool = driver.findElement(By.id("hdtb-tls"));
        btn_tool.click();

        //모든 날짜 클릭
        Thread.sleep(500);
        WebElement hdtbMenus = driver.findElement(By.id("hdtbMenus")).findElements(By.tagName("span")).get(2);
        hdtbMenus.click();

        //지난 1일 클릭
        Thread.sleep(500);
        WebElement btn_oneDay = driver.findElement(By.id("lb")).findElements(By.tagName("g-menu-item")).get(2);
        btn_oneDay.click();
        
        //하단 페이징 엘리먼트 얻음
        List<WebElement> searchPages = driver.findElements(By.xpath("//div[@role='navigation']/span/table/tbody/tr/td"));

        for(int i=2; i <= 10; i++) {
            //1번째 페이지부터 10번째 페이지까지 반복적으로 리스트 내용을 저장함
            Thread.sleep(1000);

            List<WebElement> curSearchList = driver.findElements(By.cssSelector(".g"));

            curSearchList.forEach(webElement -> {
                String[] split = webElement.getText().split("\n");

                if(webElement.getText() != null && webElement.getText().trim().length() > 1) {
                    String param = null;

                    if(webElement.getText().startsWith("웹 검색결과")) {
                        param = split[2] + "\n" + webElement.findElement(By.tagName("a")).getAttribute("href") + "\n" + split[4] + "\n\n";
                    }else {
                        param = split[0] + "\n" + webElement.findElement(By.tagName("a")).getAttribute("href") + "\n" + split[2] + "\n\n";
                    }

                    saveContentToFile(param);
                }
            });

            driver.findElement(By.xpath("//div[@role='navigation']/span/table/tbody/tr/td/a[@aria-label='Page "+i+"']")).click();
        }
        

    }

    /**
     * 파일 저장 메서드
     * @param text 저장할 내용
     */
    public static void saveContentToFile(String text)  {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(text);
            bw.flush();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
