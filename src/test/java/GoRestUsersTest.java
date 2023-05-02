import POJO.User;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class GoRestUsersTest {

    public String createRandomName() {
        return RandomStringUtils.randomAlphabetic(7);
    }

    public String createRandomEmail() {
        return RandomStringUtils.randomAlphabetic(8).toLowerCase() + "@techno.com";
    }

    RequestSpecification requestSpec;
    ResponseSpecification responseSpec;
    @BeforeClass
    public void setup(){
        baseURI = "https://gorest.co.in/public/v2/users";
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addHeader("Authorization", "Bearer 1dc687571e3bb20bbe9d1269c1d4007470c23ae43cc2e54b15ed41bd90d3db5a")
                .build();

        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .log(LogDetail.BODY)
                .build();
    }

    @Test(enabled = false)
    public void createANewUser() {
        given() // preparation (headers, parameters, ...)
//                .header("Authorization", "Bearer 1dc687571e3bb20bbe9d1269c1d4007470c23ae43cc2e54b15ed41bd90d3db5a")
//                .contentType(ContentType.JSON)
                .spec(requestSpec)
                .body("{ \"name\":\"" + createRandomName() + "\", \"email\":\"" + createRandomEmail() + "\", \"gender\":\"male\", \"status\":\"active\" }")
                .log().uri()
                .log().body()

                .when()
//                .post("https://gorest.co.in/public/v2/users")
                .post("")

                .then()
//                .log().body()
                .spec(responseSpec)
                .statusCode(201);
//                .contentType(ContentType.JSON);
    }

    @Test(enabled = false)
    public void createAUserWithMaps(){
        // Map<String, Objects> user = new HashMap<>();
        Map<String, String> user = new HashMap<>();
        user.put("name", createRandomName());
        user.put("email", createRandomEmail());
        user.put("gender", "male");
        user.put("status", "active");

        given() // preparation (headers, parameters, ...)
//                .header("Authorization", "Bearer 1dc687571e3bb20bbe9d1269c1d4007470c23ae43cc2e54b15ed41bd90d3db5a")
//                .contentType(ContentType.JSON)
                .spec(requestSpec)
                .body(user)
                .log().uri()
                .log().body()

                .when()
                .post("")

                .then()
//                .log().body()
                .spec(responseSpec)
                .statusCode(201);
//                .contentType(ContentType.JSON);
    }

    Response response;
    String email;
    int userId;
    User user;
    @Test
    public void createAUserWithObjects(){

        user = new User();
        user.setName(createRandomName());
        user.setEmail(createRandomEmail());
        user.setGender("male");
        user.setStatus("active");

        response = given() // preparation (headers, parameters, ...)
//                .header("Authorization", "Bearer 1dc687571e3bb20bbe9d1269c1d4007470c23ae43cc2e54b15ed41bd90d3db5a")
//                .contentType(ContentType.JSON)
                .spec(requestSpec)
                .body(user)
                .log().uri()
                .log().body()

                .when()
                .post("")

                .then()
//                .log().body()
                .spec(responseSpec)
                .statusCode(201)
                .extract().response();
//                .contentType(ContentType.JSON);
    }

    /** Write create user negative test */

    @Test(dependsOnMethods = "createAUserWithObjects", priority = 1)
    public void createUserNegativeTest(){

        email = response.path("email");

        given()
                .spec(requestSpec)
//                .body("{ \"name\":\"" + createRandomName() + "\", \"email\":\"" + response.path("email") + "\", \"gender\":\"male\", \"status\":\"active\" }")
                .body("{ \"name\":\"" + createRandomName() + "\", \"email\":\"" + email + "\", \"gender\":\"male\", \"status\":\"active\" }")
                .log().uri()
                .log().body()

                .when()
                .post("")

                .then()
                .spec(responseSpec)
                .statusCode(422)
                .body("[0].message", equalTo("has already been taken"));

    }

    /** get the user you created in createAUserWithObjects test */
    @Test(dependsOnMethods = "createAUserWithObjects", priority = 2)
    public void getUserById(){
//        userId = response.path("id");
        given()
                .spec(requestSpec)
                .pathParam("userId", response.path("id")) // write this at the end of the url
                .when()
//                .get("/" + userId)
                .get("/{userId}")

                .then()
                .spec(responseSpec)
                .statusCode(200)
                .body("email", equalTo(response.path("email")))
                .body("id", equalTo(response.path("id")))
                .body("name", equalTo(response.path("name")));
    }

    /** update the user you created in createAUserWithObjects */
    @Test(dependsOnMethods = "createAUserWithObjects", priority = 3)
    public void updateUser(){
        user.setName(createRandomName());
        given()
                .spec(requestSpec)
                .body(user)
                .pathParam("userId", response.path("id"))
                .when()
                .put("/{userId}")

                .then()
                .spec(responseSpec)
                .statusCode(200);
    }

    /** delete the user we created in createAUserWithObjects */
    @Test(dependsOnMethods = "createAUserWithObjects", priority = 4)
    public void deleteUser(){
        given()
                .spec(requestSpec)
                .body(user)
                .pathParam("userId", response.path("id"))
                .when()
                .delete("/{userId}")

                .then()
                .statusCode(204);
    }

    /** create delete user negative test */
    @Test(dependsOnMethods = {"createAUserWithObjects", "deleteUser"}, priority = 5)
    public void deleteUserNegativeTest(){
        given()
                .spec(requestSpec)
                .body(user)
                .pathParam("userId", response.path("id"))
                .when()
                .delete("/{userId}")

                .then()
                .statusCode(404);
    }

    @Test
    public void getUsers(){
        Response response = given()
                .spec(requestSpec)
                .when()
                .get("")

                .then()
                .spec(responseSpec)
                .statusCode(200)
                .extract().response();

        int userId0 = response.jsonPath().getInt("[0].id");
        int userId2 = response.jsonPath().getInt("[2].id");
        List<User> usersList = response.jsonPath().getList("", User.class);

        System.out.println("userId0 = " + userId0);
        System.out.println("userId2 = " + userId2);
        System.out.println("usersList = " + usersList);
    }
}
