package Campus;

import Campus.Models.Country;
import io.restassured.http.ContentType;
import io.restassured.http.Cookies;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;

public class CountryTest {

    Cookies cookies;

    @BeforeClass
    public void login(){

        baseURI = "https://test.mersys.io";
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", "turkeyts");
        credentials.put("password", "TechnoStudy123");
        credentials.put("rememberMe", "true");


        cookies = given()
                .body(credentials)
                .contentType(ContentType.JSON)

                .when()
                .post("/auth/login")

                .then()
                .statusCode(200)
                .log().body()
                .extract().response().getDetailedCookies(); // extract cookies from the response
    }

    public String randomCountryName(){
        return RandomStringUtils.randomAlphabetic(8);
    }

    public String randomCountryCode(){
        return RandomStringUtils.randomAlphabetic(3);
    }

    Country country;
    Response response;
    @Test
    public void createCountry(){

        country = new Country();
        country.setName(randomCountryName());
        country.setCode(randomCountryCode());

        response = given()
                .body(country)
                .contentType(ContentType.JSON)
                .cookies(cookies)

                .when()
                .post("/school-service/api/countries")

                .then()
                .statusCode(201)
                .log().body()
                .extract().response();
    }

    /** write create country negative test */

    @Test(dependsOnMethods = "createCountry", priority = 1)
    public void createCountryNegativeTest(){
        given()
                .body(country)
                .contentType(ContentType.JSON)
                .cookies(cookies)

                .when()
                .post("/school-service/api/countries")

                .then()
                .statusCode(400)
                .log().body();
    }

    /** update the country we created */
    @Test(dependsOnMethods = "createCountry", priority = 2)
    public void updateCountry(){
        country.setId(response.jsonPath().getString("id"));
        country.setName(randomCountryName());
        given()
                .body(country)
                .contentType(ContentType.JSON)
                .cookies(cookies)

                .when()
                .put("/school-service/api/countries")

                .then()
                .statusCode(200)
                .log().body();
    }

    /** delete country we created */

    @Test(dependsOnMethods = "createCountry", priority = 3)
    public void deleteCountry(){

        given()
                .cookies(cookies)
                .pathParam("countryId", response.path("id"))

                .when()
                .delete("/school-service/api/countries/{countryId}")

                .then()
                .statusCode(201);
    }

    /** delete country negative test */

    @Test(dependsOnMethods = {"createCountry", "deleteCountry"}, priority = 4)
    public void deleteCountryNegativeTest(){
        given()
                .cookies(cookies)
                .pathParam("countryId", response.path("id"))

                .when()
                .delete("/school-service/api/countries/{countryId}")

                .then()
                .statusCode(400);
    }
}
