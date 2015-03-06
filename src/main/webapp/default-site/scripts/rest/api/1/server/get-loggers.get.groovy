import scripts.api.ContentServices;

def results = []

def cstudioLogging = applicationContext.get("cstudioLogProvider")

def loggerMap = cstudioLogging.getLoggers()

loggerMap.each{ name, logger -> 
	def entry = [:]
	entry.name = name
	entry.level = logger.getLevel()

	results[results.size()] = entry
}

return results
