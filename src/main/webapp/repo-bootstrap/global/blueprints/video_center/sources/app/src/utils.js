import moment from 'moment';
import 'moment-timezone';

// Scrolls page to top
export function pageScrollTop() {
    const mainContainer = document.querySelector(".app-content__main");
    mainContainer.scrollTop = 0;   
}

// Returns an object with formatted date parts - Using Crafter Studio Date
export function formatDate(studioDate) {
    var date = new Date(parseInt(studioDate, 10)),
        clientTimezone = moment.tz(moment.tz.guess()).format('z'),
        dateFormatted = {
            month: moment(date).format('MMM'),
            weekDay: moment(date).format('dddd'),
            monthDay: moment(date).format('Do'),
            year: moment(date).format('YYYY'),
            time: moment(date).format('LT'),
            timezone: clientTimezone,
            dateObj: date,
            calendar: moment(date).calendar()
        }

    return dateFormatted;
}