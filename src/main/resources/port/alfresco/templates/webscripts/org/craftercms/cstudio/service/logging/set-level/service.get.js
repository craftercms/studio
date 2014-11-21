var logger = args.logger;
var level = args.level;
var success = "false";

if(logger && logger != "" && level && level != "") {
	cstudioLogging.setLoggerLevel(logger, level);
	success = "true";
}

model.setLoggerState = success;