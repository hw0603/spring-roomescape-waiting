package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.ReservationWaitingRepository;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.service.member.dto.MemberReservationResponse;
import roomescape.service.reservation.dto.AdminReservationRequest;
import roomescape.service.reservation.dto.ReservationFilterRequest;
import roomescape.service.reservation.dto.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql("/truncate-with-guests.sql")
class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;
    @Autowired
    private ReservationWaitingRepository reservationWaitingRepository;

    @BeforeEach
    void setUp() {
        reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        member = memberRepository.save(new Member("lini", "lini@email.com", "lini123", Role.MEMBER));
    }

    @DisplayName("새로운 예약을 저장한다.")
    @Test
    void create() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(date, member.getId(),
                reservationTime.getId(), theme.getId());

        //when
        ReservationResponse result = reservationService.create(adminReservationRequest);

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.time().id()).isEqualTo(reservationTime.getId()),
                () -> assertThat(result.theme().id()).isEqualTo(theme.getId())
        );
    }

    @DisplayName("모든 예약 내역을 조회한다.")
    @Test
    void findAll() {
        //given
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);

        //when
        List<ReservationResponse> reservations = reservationService.findAll();

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("사용자 조건으로 예약 내역을 조회한다.")
    @Test
    void findByMember() {
        //given
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);
        ReservationFilterRequest reservationFilterRequest = new ReservationFilterRequest(member.getId(), null, null,
                null);

        //when
        List<ReservationResponse> reservations = reservationService.findByCondition(reservationFilterRequest);

        //then
        assertThat(reservations).hasSize(1);
    }

    @DisplayName("사용자와 테마 조건으로 예약 내역을 조회한다.")
    @Test
    void findByMemberAndTheme() {
        //given
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);
        long notMemberThemeId = theme.getId() + 1;
        ReservationFilterRequest reservationFilterRequest = new ReservationFilterRequest(member.getId(),
                notMemberThemeId, null, null);

        //when
        List<ReservationResponse> reservations = reservationService.findByCondition(reservationFilterRequest);

        //then
        assertThat(reservations).isEmpty();
    }

    @DisplayName("id로 사용자의 예약과 예약 대기 목록을 조회한다.")
    @Test
    void findReservationsOf() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));
        Theme theme = themeRepository.save(
                new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.",
                        "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg")
        );
        Member member = memberRepository.save(new Member("pedro", "pedro@email.com", "pedro123", Role.MEMBER));
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.now()), reservationTime);
        reservationRepository.save(
                new Reservation(member, schedule, theme, ReservationStatus.RESERVED)
        );
        reservationWaitingRepository.save(new ReservationWaiting(member, theme, schedule));

        // when
        List<MemberReservationResponse> reservations = reservationService.findReservationsOf(4L);

        // then
        assertThat(reservations).hasSize(2);
    }

    @DisplayName("id로 예약을 삭제한다.")
    @Test
    void deleteById() {
        //given
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        Reservation target = reservationRepository.save(reservation);

        //when
        reservationService.deleteById(target.getId());

        //then
        assertThat(reservationService.findAll()).isEmpty();
    }

    @DisplayName("예약 삭제 시 해당 일정과 테마에 대기가 등록돼 있다면 첫 번째 대기를 예약으로 승격한다.")
    @Test
    @Transactional  // TODO: 제거 시 테스트 깨짐
    void convertWaitingToReservationWhenReservationCanceled() {
        // given
        Schedule schedule = new Schedule(ReservationDate.of(LocalDate.MAX), reservationTime);
        ReservationWaiting waiting = new ReservationWaiting(member, theme, schedule);
        ReservationWaiting savedWaiting = reservationWaitingRepository.save(waiting);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        Reservation target = reservationRepository.save(reservation);

        // when
        reservationService.deleteById(target.getId());

        // then
        long savedWaitingId = savedWaiting.getId();
        long memberId = member.getId();
        SoftAssertions assertions = new SoftAssertions();
        assertions.assertThat(reservationWaitingRepository.findById(savedWaitingId)).isNotPresent();
        assertions.assertThat(reservationRepository.findByMemberId(memberId)).hasSize(1);
        assertions.assertAll();
    }

    @DisplayName("해당 테마와 일정으로 예약이 존재하면 예외를 발생시킨다.")
    @Test
    void duplicatedReservation() {
        //given
        LocalDate date = LocalDate.MAX;
        Schedule schedule = new Schedule(ReservationDate.of(date), reservationTime);
        Reservation reservation = new Reservation(member, schedule, theme, ReservationStatus.RESERVED);
        reservationRepository.save(reservation);

        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(date, member.getId(),
                reservationTime.getId(), theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.create(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("선택하신 테마와 일정은 이미 예약이 존재합니다.");
    }

    @DisplayName("존재하지 않는 시간으로 예약을 추가하면 예외를 발생시킨다.")
    @Test
    void cannotCreateByUnknownTime() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(date, member.getId(), 0L,
                theme.getId());

        //when & then
        assertThatThrownBy(() -> reservationService.create(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 시간입니다.");
    }

    @DisplayName("존재하지 않는 테마로 예약을 추가하면 예외를 발생시킨다.")
    @Test
    void cannotCreateByUnknownTheme() {
        //given
        LocalDate date = LocalDate.now().plusDays(1);
        AdminReservationRequest adminReservationRequest = new AdminReservationRequest(date, member.getId(),
                reservationTime.getId(), 0L);

        //when & then
        assertThatThrownBy(() -> reservationService.create(adminReservationRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("더이상 존재하지 않는 테마입니다.");
    }
}
