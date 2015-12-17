import scripts.api.ContentServices;

def result = [:]

def cstudioLogging = applicationContext.get("cstudioLogProvider")
def loggerMap = cstudioLogging.setLoggerLevel(params.logger, params.level)

return result 