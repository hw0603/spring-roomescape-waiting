package roomescape.controller.page;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import roomescape.auth.AuthConstants;
import roomescape.controller.DataInitializedControllerTest;
import roomescape.service.auth.dto.LoginRequest;

class AdminPageControllerTest extends DataInitializedControllerTest {
    private String token;

    @BeforeEach
    void init() {
        token = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin@email.com", "admin123"))
                .when().post("/login")
                .then().log().all().extract().cookie(AuthConstants.AUTH_COOKIE_NAME);
    }

    @DisplayName("Admin Page 홈화면 접근 성공 테스트")
    @Test
    void responseAdminPage() {
        Response response = RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, token)
                .when().get("/admin")
                .then().log().all().extract().response();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("Admin Reservation Page 접근 성공 테스트")
    @Test
    void responseAdminReservationPage() {
        Response response = RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, token)
                .when().get("/admin/reservation")
                .then().log().all().extract().response();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("Admin Reservation Time Page 접근 성공 테스트")
    @Test
    void responseReservationTimePage() {
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, token)
                .when().get("/admin/time")
                .then().log().all()
                .assertThat().statusCode(HttpStatus.OK.value());
    }

    @DisplayName("Admin Theme Page 접근 성공 테스트")
    @Test
    void responseThemePage() {
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, token)
                .when().get("/admin/theme")
                .then().log().all()
                .assertThat().statusCode(HttpStatus.OK.value());
    }
}
