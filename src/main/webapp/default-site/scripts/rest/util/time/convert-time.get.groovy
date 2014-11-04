import java.util.Date
import java.util.TimeZone
import java.text.SimpleDateFormat


def result = [:]
def time = params.time
def sourceTZ = params.srcTimezone
def destTZ = params.destTimezone
def format = params.dateFormat
def DATE_TIME_FORMAT = "yyyyMMdd-HH:mm:ss";

format = (!"".equals(format)) ? format : DATE_TIME_FORMAT;
SimpleDateFormat sdf = new SimpleDateFormat(format);
Date specifiedTime;

if (sourceTZ != null && !"".equals(sourceTZ)) {
	sdf.setTimeZone(TimeZone.getTimeZone(sourceTZ));
}
else {
	sdf.setTimeZone(TimeZone.getDefault()); // default to server's
}

// timezone
specifiedTime = sdf.parse(time);

// switch timezone
if (destTZ != null && !"".equals(destTZ)) {
	sdf.setTimeZone(TimeZone.getTimeZone(destTZ));
}
else {
	sdf.setTimeZone(TimeZone.getDefault()); // default to server's
}

// timezone
def convertedTimezone = sdf.format(specifiedTime);



result.originalTime = time;
result.dateFormat = format;
result.srcTimezone = sourceTZ;
result.destTimezone = destTZ;
result.convertedTimezone = convertedTimezone;

return result;