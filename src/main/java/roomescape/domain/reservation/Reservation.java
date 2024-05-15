package roomescape.domain.reservation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.member.Member;

@Entity
public class Reservation {
    private static final long NO_ID = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private Member member;

    @ManyToOne(cascade = CascadeType.ALL)
    private Schedule schedule;

    @ManyToOne(cascade = CascadeType.ALL)
    private Theme theme;

    public Reservation() {
    }

    public Reservation(long id, Member member, Schedule schedule, Theme theme) {
        this.id = id;
        this.member = member;
        this.schedule = schedule;
        this.theme = theme;
    }

    public Reservation(long id, Reservation reservation) {
        this(id, reservation.member, reservation.schedule, reservation.theme);
    }

    public Reservation(long id, String date, Member member, ReservationTime reservationTime,
                       final Theme theme) {
        this(id, member, new Schedule(new ReservationDate(date), reservationTime), theme);
    }

    public Reservation(String reservationDate, Member member, ReservationTime reservationTime, Theme theme) {
        this(NO_ID, member, new Schedule(new ReservationDate(reservationDate), reservationTime), theme);
    }

    public long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public LocalTime getTime() {
        return schedule.getTime();
    }

    public ReservationTime getReservationTime() {
        return schedule.getReservationTime();
    }

    public Theme getTheme() {
        return theme;
    }
}
