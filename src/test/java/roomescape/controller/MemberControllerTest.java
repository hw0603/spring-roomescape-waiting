package roomescape.controller;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.AuthConstants;
import roomescape.service.auth.dto.LoginRequest;

class MemberControllerTest extends DataInitializedControllerTest {
    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("admin@email.com", "admin123"))
                .when().post("/login")
                .then().log().all().extract().cookie(AuthConstants.AUTH_COOKIE_NAME);
    }

    @DisplayName("모든 사용자 조회 성공 테스트 - 사용자 총 3명")
    @Test
    void findAllMembers() {
        // when&then
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, adminToken)
                .when().get("/members")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(2));
    }

    @DisplayName("사용자 예약/예약 대기 목록 조회 테스트")
    @Test
    void findMemberReservations() {
        // when & then
        RestAssured.given().log().all()
                .cookie(AuthConstants.AUTH_COOKIE_NAME, adminToken)
                .when().get("/members/reservations")
                .then().log().all()
                .assertThat().statusCode(200);
    }
}
