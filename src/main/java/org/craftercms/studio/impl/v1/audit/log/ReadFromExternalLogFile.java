package org.craftercms.studio.impl.v1.audit.log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.craftercms.studio.api.v1.dal.AuditFeed;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

/**
 * @author mhashmath
 *
 */
public class ReadFromExternalLogFile {

	private static final Logger logger = LoggerFactory.getLogger(ReadFromExternalLogFile.class);

	private List<Path> fileNameList = new ArrayList<Path>();
	private static List<Path> pathList = new ArrayList<Path>();
	private static List<Integer> lineCounterList = new ArrayList<Integer>();

	public int getAuditLogCountFromFile(String site, String user, List<String> actions, String logPath, String logFileName) {

		int totalLineCount = 0;
		int previousTotal = 0;
		listAllFiles(site, logPath, logFileName);

		for (Path path : fileNameList) {
			try (FileReader fileReader = new FileReader(path.toString());
					BufferedReader bufferedReader = new BufferedReader(fileReader);) {

				totalLineCount = getLineCount(site, user, actions, bufferedReader, totalLineCount);

				if (totalLineCount != previousTotal) {
					pathList.add(path);
					lineCounterList.add(totalLineCount);
					previousTotal = totalLineCount;
				}

			} catch (Exception e) {
				logger.error("Erro while getAuditLogCountFromFile " + e);
			}
		}

		return totalLineCount;

	}

	private void listAllFiles(String site, String logPath, String logFileName) {

		if (!fileNameList.isEmpty()) {
			fileNameList.clear();
			pathList.clear();
			lineCounterList.clear();
		}
		try (Stream<Path> paths = Files.list(Paths.get(logPath + "/" + site))) {
			paths.sorted().forEach(filePath -> {

				if (filePath.getFileName().toString().startsWith(logFileName)) {
					fileNameList.add(filePath);
				}
			});

			if (!fileNameList.isEmpty() && fileNameList.size() > 1) {
				fileNameList.add(fileNameList.get(0));
				fileNameList.remove(0);
				Collections.reverse(fileNameList);
			}

		} catch (Exception e) {
			logger.error("Error while getting list of files " + e);
		}

	}

	private int getLineCount(String site, String user, List<String> actions, BufferedReader bufferedReader,
			int lineCounter) throws IOException {
		String line;

		while ((line = bufferedReader.readLine()) != null) {
			if (!actions.isEmpty()) {
				for (String actionType : actions) {
					Pattern p = Pattern.compile(actionType + "*.*json, " + user + "*.*Z, " + site,
							Pattern.CASE_INSENSITIVE);

					Matcher m = p.matcher(line);
					if (m.find()) {
						lineCounter++;
					}
				}
			} else {
				Pattern p = Pattern.compile("json, " + user + "*.*Z, " + site, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(line);
				if (m.find()) {
					lineCounter++;
				}
			}
		}

		return lineCounter;
	}

	public List<AuditFeed> getAuditLogFromFile(String site, String user, List<String> actions, int start, int number) {

		int limit = 0;
		int searchResult = -1;

		if (!lineCounterList.isEmpty()) {
			searchResult = searchFileIndex(lineCounterList, start + 1);
		}

		if (searchResult != 0 && searchResult != -1) {
			limit = lineCounterList.get(searchResult - 1);
		}

		AuditFeed auditFeedFilter = null;
		List<AuditFeed> auditFilteredList = new ArrayList<>();
		List<AuditFeed> tempAuditFilteredList = new ArrayList<>();

		if (searchResult != -1) {

			searchLoop: {
				for (int i = searchResult; i < pathList.size(); i++) {

					tempAuditFilteredList.clear();
					limit = lineCounterList.get(i);
					
					try (FileReader fileReader = new FileReader(pathList.get(i).toString());
							BufferedReader reader = new BufferedReader(fileReader);) {

						String line = null;

						fileReaderLopp: {
						for (line = null; (line = reader.readLine()) != null;) {

							if (!actions.isEmpty()) {
								for (String actionType : actions) {
									Pattern p = Pattern.compile(actionType + "*.*json, " + user + "*.*Z, " + site, Pattern.CASE_INSENSITIVE);

									Matcher m = p.matcher(line);
									if (m.find()) {

										if (limit > start && limit <= (number + start)) {
											String logValues[] = line.split(", ");
											auditFeedFilter = insertSearchContents(logValues, auditFeedFilter);
											tempAuditFilteredList.add(auditFeedFilter);
										} else if (limit <= start && limit <= (number + start)) {
											break fileReaderLopp;
										}
										limit--;
									}

								}
							} else {
								Pattern p = Pattern.compile("json, " + user + "*.*Z, " + site, Pattern.CASE_INSENSITIVE);
								Matcher m = p.matcher(line);
								if (m.find()) {
									if (limit > start && limit <= (number + start)) {
										String logValues[] = line.split(", ");
										auditFeedFilter = insertSearchContents(logValues, auditFeedFilter);
										tempAuditFilteredList.add(auditFeedFilter);
									} else if (limit <= start && limit <= (number + start)) {
										break fileReaderLopp;
									}
									limit--;

								}
							}

							if (auditFilteredList.size() == number || tempAuditFilteredList.size() == number) {
								Collections.reverse(tempAuditFilteredList);
								auditFilteredList.addAll(tempAuditFilteredList);
								reader.close();
								fileReader.close();
								break searchLoop;
							}

						}
					}

						Collections.reverse(tempAuditFilteredList);
						auditFilteredList.addAll(tempAuditFilteredList);
						limit = lineCounterList.get(i);

						if (auditFilteredList.size() == number) {
							reader.close();
							fileReader.close();
							break searchLoop;
						}						
						
					} catch (Exception e) {
						logger.error("Error while getAuditLogFromFile " + e);
					}

				}
			}
		}

		return auditFilteredList;
	}

	private int searchFileIndex(List<Integer> lineCounter, int target) {

		while (lineCounter.size() > 2) {
			int end = lineCounter.size() - 1;
			int start = 0;
			int mid = (start + end) / 2;

			if (target <= lineCounter.get(mid)) {
				lineCounter = lineCounter.subList(start, (mid + 1));
			} else {
				lineCounter = lineCounter.subList(mid, (end + 1));
			}
		}

		if (lineCounter.get(0) >= target) {
			return lineCounterList.indexOf(lineCounter.get(0));
		} else if (lineCounter.get(0) < target && lineCounter.get(1) >= target) {
			return lineCounterList.indexOf(lineCounter.get(1));
		}

		return -1;
	}

	private AuditFeed insertSearchContents(String[] logValues, AuditFeed auditFeed) {
		auditFeed = new AuditFeed();
		auditFeed.setType(logValues[0]);
		auditFeed.setSummary(logValues[1]);
		auditFeed.setSummaryFormat(logValues[2]);
		auditFeed.setUserId(logValues[3]);
		auditFeed.setCreationDate(ZonedDateTime.parse(logValues[4]));
		auditFeed.setModifiedDate(ZonedDateTime.parse(logValues[5]));
		auditFeed.setSiteNetwork(logValues[6]);
		auditFeed.setContentId(logValues[7]);
		auditFeed.setContentType(logValues[8]);
		auditFeed.setSource(logValues[9]);
		return auditFeed;
	}
}
