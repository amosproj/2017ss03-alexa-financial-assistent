package amosalexa.services;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DateUtil {

    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

    /**
     * gets the day of a month
     * @param date date like 2017-8-1
     * @return day of month
     */
    public static int getDayOfMonth(String date) {

        DateTime dateTime;
        try {
            dateTime = new DateTime(date);
        } catch(IllegalArgumentException e){
            return 0;
        }

        return dateTime.dayOfMonth().get();
    }

    /**
     * calculates how many times a transaction is executed (max. 12 month)
     * @param event event date
     * @param future future date
     * @return number of execution
     */
    public static int getDatesBetween(String event, String future){
        LocalDate now = new DateTime().toLocalDate();
        LocalDate futureDate = new DateTime(future).toLocalDate();
        LocalDate eventDate = new DateTime(event).toLocalDate();
        int dates = 0;
        int months = 0;
        while(months < 12){
            if(eventDate.isAfter(now) && eventDate.isBefore(futureDate)){
                dates++;
            }
            eventDate = eventDate.plusMonths(1);
            months++;
        }
        return dates;
    }
}
