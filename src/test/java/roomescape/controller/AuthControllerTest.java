package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.AuthConstants;
import roomescape.service.auth.dto.LoginRequest;
import roomescape.service.auth.dto.SignUpRequest;

class AuthControllerTest extends DataInitializedControllerTest {
    @DisplayName("로그인 성공 테스트")
    @Test
    void login() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest("lini", "lini@email.com", "lini123"))
                .when().post("/signup")
                .then().log().all();

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("lini@email.com", "lini123"))
                .when().post("/login")
                .then().log().all()
                .assertThat().statusCode(200);
    }

    @DisplayName("로그아웃 성공 테스트")
    @Test
    void logout() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest("lini", "lini@email.com", "lini123"))
                .when().post("/signup")
                .then().log().all();

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().post("/logout")
                .then().log().all()
                .assertThat().statusCode(200);
    }

    @DisplayName("인증 조회 성공 테스트")
    @Test
    void check() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest("lini", "lini@email.com", "lini123"))
                .when().post("/signup")
                .then().log().all();

        String token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("lini@email.com", "lini123"))
                .when().post("/login")
                .then().log().all().extract().cookie(AuthConstants.AUTH_COOKIE_NAME);

        //when&then
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, token)
                .when().get("/login/check")
                .then().log().all()
                .assertThat().statusCode(200).body("name", is("lini"));
    }

    @DisplayName("회원가입 성공 테스트")
    @Test
    void signUp() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new SignUpRequest("lini", "lini@email.com", "lini123"))
                .when().post("/signup")
                .then().log().all()
                .assertThat().statusCode(201);
    }
}
