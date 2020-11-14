import blok2.Blok2Application;
import blok2.controllers.BuildingController;
import blok2.controllers.LocationController;
import blok2.daos.IAuthorityDao;
import blok2.daos.IBuildingDao;
import blok2.daos.ICalendarPeriodDao;
import blok2.daos.TestSharedMethods;
import blok2.helpers.EmailService;
import blok2.model.Authority;
import blok2.model.Building;
import blok2.model.calendar.CalendarPeriod;
import blok2.model.reservables.Location;
import config.CustomFlywayConfig;
import org.flywaydb.test.FlywayTestExecutionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Import(CustomFlywayConfig.class)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Blok2Application.class)
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        FlywayTestExecutionListener.class
})
public class Mail {

    @Autowired
    private EmailService service;
    @Autowired
    private IAuthorityDao authorityDao;
    @Autowired
    private IBuildingDao buildingDao;
    @Autowired
    private LocationController locationDao;
    @Autowired
    private ICalendarPeriodDao calendarPeriodDao;

    @Test
    public void sendMail() throws SQLException, IOException, MessagingException {
        Authority authority = TestSharedMethods.insertTestAuthority(authorityDao);

        Building testBuilding = buildingDao.addBuilding(TestSharedMethods.testBuilding());

        Location testLocation = TestSharedMethods.testLocation(authority.clone(), testBuilding);
        locationDao.addLocation(testLocation);
        CalendarPeriod period = new CalendarPeriod();
        period.setLocation(testLocation);
        period.setStartsAt(LocalDate.now().plusWeeks(3).plusDays(1));
        period.setEndsAt(LocalDate.now().plusWeeks(3).plusDays(10));
        period.setClosingTime(LocalTime.of(16, 0));
        period.setOpeningTime(LocalTime.of(7, 0));
        period.setReservableFrom(LocalDateTime.of(LocalDate.now().plusWeeks(2).plusDays(1), LocalTime.of(7, 0)));
        period.setLockedFrom(LocalDateTime.of(period.getStartsAt().minusWeeks(3), LocalTime.of(0, 0)));
        calendarPeriodDao.addCalendarPeriods(Collections.singletonList(period));


        service.sendCalendarPeriodsMessage("vanerum.tim@gmail.com");
    }
}
