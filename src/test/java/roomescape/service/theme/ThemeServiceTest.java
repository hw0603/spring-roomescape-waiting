package roomescape.service.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.Fixture;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.service.ServiceTestBase;
import roomescape.service.theme.dto.ThemeRequest;
import roomescape.service.theme.dto.ThemeResponse;

class ThemeServiceTest extends ServiceTestBase {
    @Autowired
    private ThemeService themeService;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("테마를 생성한다.")
    @Test
    void create() {
        // given
        ThemeRequest themeRequest = Fixture.themeRequest;

        // when
        ThemeResponse themeResponse = themeService.create(themeRequest);

        // then
        assertThat(themeResponse.id()).isNotZero();
    }

    @DisplayName("테마를 생성한다.")
    @Test
    void cannotCreateByDuplicatedName() {
        // given
        themeRepository.save(Fixture.theme);
        ThemeRequest themeRequest = Fixture.themeRequest;

        // when&then
        assertThatThrownBy(() -> themeService.create(themeRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 존재하는 테마 이름입니다.");
    }

    @DisplayName("모든 테마를 조회한다.")
    @Test
    void findAll() {
        //given
        createTheme("레벨2 탈출");

        //when
        List<ThemeResponse> responses = themeService.findAll();

        //then
        assertThat(responses).hasSize(1);
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteById() {
        //given
        Theme theme = createTheme("레벨2 탈출");

        //when
        themeService.deleteById(theme.getId());

        //then
        assertThat(themeService.findAll()).isEmpty();
    }

    @DisplayName("예약이 존재하는 테마를 삭제하면 예외가 발생한다.")
    @Test
    void cannotDeleteByReservation() {
        // given
        Theme theme = createTheme("레벨2 탈출");
        Member member = memberRepository.save(Fixture.member);
        ReservationTime reservationTime = reservationTimeRepository.save(Fixture.reservationTime);
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);

        // when&then
        long themeId = theme.getId();
        assertThatThrownBy(() -> themeService.deleteById(themeId))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("해당 테마로 예약이 존재해서 삭제할 수 없습니다.");
    }

    @DisplayName("인기 테마를 조회한다.")
    @Test
    void findPopularThemes() {
        // given
        Theme theme1 = createTheme("레벨1 탈출");
        Theme theme2 = createTheme("레벨2 탈출");
        Theme theme3 = createTheme("레벨3 탈출");

        ReservationTime reservationTime = reservationTimeRepository.save(Fixture.reservationTime);
        Member member = memberRepository.save(Fixture.member);
        Schedule schedule1 = new Schedule(ReservationDate.of(LocalDate.now().minusDays(1)), reservationTime);
        Schedule schedule2 = new Schedule(ReservationDate.of(LocalDate.now().minusDays(7)), reservationTime);
        Schedule schedule3 = new Schedule(ReservationDate.of(LocalDate.now().minusDays(8)), reservationTime);

        reservationRepository.save(new Reservation(member, schedule1, theme1, ReservationStatus.RESERVED));
        reservationRepository.save(new Reservation(member, schedule2, theme2, ReservationStatus.RESERVED));
        reservationRepository.save(new Reservation(member, schedule3, theme3, ReservationStatus.RESERVED));

        // when
        List<ThemeResponse> result = themeService.findPopularThemes();

        // then
        assertThat(result).hasSize(2);
    }

    private Theme createTheme(String name) {
        Theme theme = new Theme(name, "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        return themeRepository.save(theme);
    }
}
