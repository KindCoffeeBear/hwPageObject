package ru.netology.web.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import ru.netology.web.data.DataHelper;
import ru.netology.web.page.DashboardPage;
import ru.netology.web.page.LoginPage;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    DataHelper.CardInfo firstCardInfo;
    DataHelper.CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;


    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);
        Configuration.browserCapabilities = options;
        open("http://localhost:9999");
        var authInfo = DataHelper.getAuthInfo();
        var loginPage = new LoginPage();
        var verificarionPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCodeFor(authInfo);
        dashboardPage = verificarionPage.validVerify(verificationCode);
        firstCardInfo = DataHelper.getFirstCardInfo();
        secondCardInfo = DataHelper.getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo.getId());
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo.getId());
    }

    @Test
    void shouldTransferMoneyFromFirstToCards() {
        var amount = DataHelper.generateValidAmount(firstCardBalance);
        var expectedBalanceFirstCard = firstCardBalance - amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        var replenishmentPage = dashboardPage.selectCardToReplenishment(secondCardInfo.getId());
        dashboardPage = replenishmentPage.makeValidReplenishment(String.valueOf(amount), firstCardInfo);
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo.getId());
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo.getId());
        assertAll(()->assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard),
                ()->assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard));
    }

    @Test
    void shouldGetErrorMassageIfAmountMoreBalance() {
        var amount = DataHelper.generateInvalidAmount(secondCardBalance);
        var replenishmentPage = dashboardPage.selectCardToReplenishment(firstCardInfo.getId());
        replenishmentPage.makeReplenishment(String.valueOf(amount), secondCardInfo);
        replenishmentPage.findErrorMessage("Ошибка! На балансе недостаточно средств");
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo.getId());
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo.getId());
        assertAll(()->assertEquals(firstCardBalance, actualBalanceFirstCard),
                ()->assertEquals(secondCardBalance, actualBalanceSecondCard));
    }
}
