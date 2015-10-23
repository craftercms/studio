/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.studio.impl.v1.service.deployment.job;

import org.craftercms.studio.api.v1.dal.PublishToTarget;
import org.craftercms.studio.api.v1.job.Job;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.deployment.*;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.service.transaction.TransactionService;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import org.craftercms.studio.api.v1.to.PublishingChannelConfigTO;
import org.craftercms.studio.api.v1.to.PublishingChannelGroupConfigTO;
import org.craftercms.studio.impl.v1.job.RepositoryJob;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PublishContentToDeploymentTarget extends RepositoryJob {

    private static final Logger logger = LoggerFactory.getLogger(PublishContentToDeploymentTarget.class);

    protected static final ReentrantLock singleWorkerLock = new ReentrantLock();

    protected static Map<String, Map<String, Integer>> _publishingFailureCounters = new HashMap<String, Map<String, Integer>>();

    public static Map<String, Map<String, Integer>> getPublishingRetryCounters() {
        return _publishingFailureCounters;
    }

    public static Map<String, Integer> getPublishingRetryCounters(String site) {
        return _publishingFailureCounters.get(site);
    }

    private static boolean stopSignaled = false;
    private static boolean running = false;

    public static synchronized void signalToStop(boolean toStop) {
        stopSignaled = toStop;
    }

    public synchronized static boolean isRunning() {
        return running;
    }

    public synchronized static void setRunning(boolean isRunning){
        running = isRunning;
    }

    public void executeAsSignedInUser() {
        if (masterPublishingNode && !stopSignaled) {
            setRunning(true);
            if (singleWorkerLock.tryLock()) {
                try {
                    processJobs();
                } catch(Exception err) {
                    logger.error("unable to execute job", err);
                } finally {
                    singleWorkerLock.unlock();
                }
            }
            setRunning(false);
        }
    }

    public void processJobs() {


        try {
                Set<String> siteNames = siteService.getAllAvailableSites();
                //tx.commit();
                if (siteNames != null && siteNames.size() > 0) {
                    for (String site : siteNames) {
                        try {
                            logger.debug("Starting publishing for site \"{0}\"", site);
                            //tx = _transactionService.getTransaction();
                            //tx.begin();
                            Set<DeploymentEndpointConfigTO> targets = getAllTargetsForSite(site);
                            //tx.commit();

                            for (DeploymentEndpointConfigTO target : targets) {
                                logger.debug("Starting publishing on target \"{0}\", site \"{1}\"", target.getName(), site);
                                if (target.getEnvironments() == null || target.getEnvironments().isEmpty()) continue;
                                if (publishingManager.checkConnection(target)) {
                                    //tx = _transactionService.getTransaction();
                                    //tx.begin();

                                    logger.debug("Getting target version (target: \"{0}\", site: \"{1}\"", target.getName(), site);
                                    long targetVersion = publishingManager.getTargetVersion(target, site);

                                    logger.debug("Target version: \"{0}\" (target: \"{1}\", site: \"{2}\"", targetVersion, target.getName(), site);
                                    if (targetVersion != -1) {
                                        List<PublishToTarget> syncItems = publishingManager.getItemsToSync(site, targetVersion, target.getEnvironments());
                                        if (syncItems != null && syncItems.size() > 0) {
                                            logger.info("publishing \"{0}\" item(s) to \"{1}\" for site \"{2}\"", syncItems.size(), target.getName(), site);

                                            logger.debug("Filtering out items before sending to deployment agent");
                                            List<PublishToTarget> filteredItems = filterItems(syncItems, target);


                                            try {
                                                if (filteredItems != null && filteredItems.size() > 0) {
                                                    logger.debug("Sending \"{0}\" items to target \"{1}\", site \"{2}\"", filteredItems.size(), target.getName(), site);
                                                    publishingManager.deployItemsToTarget(site, filteredItems, target);
                                                }

                                                long newVersion = getDeployedVersion(syncItems);
                                                logger.debug("Setting new version for target (target: \"{0}\", site \"{1}\", version \"{2}\"", target.getName(), site, newVersion);
                                                publishingManager.setTargetVersion(target, newVersion, site);

                                                logger.debug("Inserting deployment history for \"{0}\" items on target \"{1}\", site \"{2}\"", filteredItems.size(), target.getName(), site);
                                                publishingManager.insertDeploymentHistory(target, filteredItems, new Date());
                                            } catch (UploadFailedException err) {
                                                Map<String, Integer> counters = _publishingFailureCounters.get(err.getSite());
                                                if (counters == null) {
                                                    counters = new HashMap<String, Integer>();
                                                }
                                                Integer count = counters.get(err.getTarget());
                                                if (count == null) {
                                                    count = 0;
                                                } else {
                                                    count++;
                                                }
                                                if (count > maxTolerableRetries) {
                                                    // TODO: Send notification - big red alert!
                                                    logger.error("Uploading content failed for site \"{0}\", target \"{1}\", URL \"{2}\"", err, err.getSite(), err.getTarget(), err.getUrl());
                                                } else {
                                                    logger.warn("Uploading content failed for site \"{0}\", target \"{1}\", URL \"{2}\"", err.getSite(), err.getTarget(), err.getUrl());
                                                }
                                                counters.put(err.getTarget(), count);
                                                _publishingFailureCounters.put(err.getSite(), counters);
                                                //tx.rollback();
                                                continue;
                                            }

                                        }
                                        Map<String, Integer> counters = _publishingFailureCounters.get(site);
                                        if (counters == null) {
                                            counters = new HashMap<String, Integer>();
                                        }
                                        counters.put(target.getName(), 0);
                                        _publishingFailureCounters.put(site, counters);
                                    } else {
                                        // we can talk to the agent but there is something wrong
                                        // for example the features we need are not supported
                                        logger.error("cannot negotiate a version for deployment agent \"{0}\" for site \"{1}\"", target.getName(), site);
                                    }
                                    //tx.commit();
                                } else {
                                    // TODO: update target status
                                    logger.warn("cannot connect to deployment agent \"{0}\" for site \"{1}\"", target.getName(), site);
                                }
                                logger.debug("Finished publishing on target \"{0}\", site \"{1}\"", target.getName(), site);
                            }
                            logger.debug("Finished publishing for site \"{0}\"", site);
                        } catch (ContentNotFoundForPublishingException err) {
                            Map<String, Integer> counters = _publishingFailureCounters.get(err.getSite());
                            if (counters == null) {
                                counters = new HashMap<String, Integer>();
                            }
                            Integer count = counters.get(err.getTarget());
                            if (count == null) {
                                count = 0;
                            } else {
                                count++;
                            }
                            if (count > maxTolerableRetries) {
                                // TODO: Send notification - big red alert!
                                logger.error("Content not found for publishing site \"{0}\", target \"{1}\", path \"{2}\"", err, err.getSite(), err.getTarget(), err.getPath());
                            } else {
                                logger.warn("Content not found for publishing site \"{0}\", target \"{1}\", path \"{2}\"", err.getSite(), err.getTarget(), err.getPath());
                            }
                            counters.put(err.getTarget(), count);
                            _publishingFailureCounters.put(err.getSite(), counters);
                            logger.info("Continue executing deployment for other sites.");
                            //tx.rollback();
                        } catch (Exception err) {
                            logger.error("error while processing items to be published for site: " + site, err);
                            logger.info("Continue executing deployment for other sites.");
                            //tx.rollback();
                        }
                    }
                }
        }
        catch(Exception err) {
            logger.error("error while processing items to be published", err);
        }
    }

    protected Set<DeploymentEndpointConfigTO> getAllTargetsForSite(String site) {
        Map<String, PublishingChannelGroupConfigTO> groupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
        Set<DeploymentEndpointConfigTO> targets = new HashSet<DeploymentEndpointConfigTO>();
        Map<String, DeploymentEndpointConfigTO> targetMap = new HashMap<String, DeploymentEndpointConfigTO>();
        if (groupConfigTOs != null && groupConfigTOs.size() > 0) {
            for (PublishingChannelGroupConfigTO groupConfigTO : groupConfigTOs.values()) {
                List<PublishingChannelConfigTO> channelConfigTOs = groupConfigTO.getChannels();
                if (channelConfigTOs != null && channelConfigTOs.size() > 0) {
                    for (PublishingChannelConfigTO channelConfigTO : channelConfigTOs) {
                        DeploymentEndpointConfigTO endpoint = siteService.getDeploymentEndpoint(site, channelConfigTO.getName());
                        if (endpoint != null) {
                            DeploymentEndpointConfigTO targetItem = targetMap.get(endpoint.getName());
                            if (targetItem == null) {
                                targetItem = new DeploymentEndpointConfigTO();
                                targetItem.setName(endpoint.getName());
                                targetItem.setTarget(endpoint.getTarget());
                                targetItem.setType(endpoint.getType());
                                targetItem.setServerUrl(endpoint.getServerUrl());
                                targetItem.setStatusUrl(endpoint.getStatusUrl());
                                targetItem.setVersionUrl(endpoint.getVersionUrl());
                                targetItem.setPassword(endpoint.getPassword());
                                targetItem.setExcludePattern(endpoint.getExcludePattern());
                                targetItem.setIncludePattern(endpoint.getIncludePattern());
                                targetItem.setBucketSize(endpoint.getBucketSize());
                                targetItem.setSiteId(endpoint.getSiteId());
                                targetItem.setSendMetadata(endpoint.isSendMetadata());
                                targetItem.setOrder(endpoint.getOrder());
                                targets.add(targetItem);

                                targetMap.put(endpoint.getName(), targetItem);
                            }
                            targetItem.addEnvironment(groupConfigTO.getName());
                        }
                    }
                }
            }
        }
        return targets;
    }

    protected long getDeployedVersion(List<PublishToTarget> syncItems) {
        Collections.sort(syncItems, new VersionComparator());
        PublishToTarget item = syncItems.get(0);
        return item.getVersion();
    }

    class VersionComparator implements Comparator<PublishToTarget> {

        @Override
        public int compare(PublishToTarget publishToTargetItem, PublishToTarget publishToTargetItem2) {
            long result = publishToTargetItem.getVersion() - publishToTargetItem2.getVersion();
            if (result > 0) {
                return -1;
            } else if (result < 0) {
                return 1;
            }
            return 0;
        }
    }

    protected List<PublishToTarget> filterItems(List<PublishToTarget> syncItems, DeploymentEndpointConfigTO target) {
        List<String> includePaths = target.getIncludePattern();
        List<String> excludePaths = target.getExcludePattern();
        List<PublishToTarget> filteredItems = new ArrayList<PublishToTarget>();
        for (PublishToTarget item : syncItems) {
            boolean exclude = false;
            Pattern regexPattern;
            if (includePaths != null) {
                for (String includePath : includePaths) {
                    regexPattern = Pattern.compile(includePath);
                    Matcher m = regexPattern.matcher(item.getPath());
                    if (m.matches()) {
                        exclude = false;
                    }
                }
            }
            if (excludePaths != null) {
                for (String excludePath : excludePaths) {
                    regexPattern = Pattern.compile(excludePath);
                    Matcher m = regexPattern.matcher(item.getPath());
                    if (m.matches()) {
                        exclude = true;
                    }
                }
            }
            if (!exclude) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    public Integer getMaxTolerableRetries() { return maxTolerableRetries; }
    public void setMaxTolerableRetries(Integer maxTolerableRetries) { this.maxTolerableRetries = maxTolerableRetries; }

    public boolean isMasterPublishingNode() { return masterPublishingNode; }
    public void setMasterPublishingNode(boolean masterPublishingNode) { this.masterPublishingNode = masterPublishingNode; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public PublishingManager getPublishingManager() { return publishingManager; }
    public void setPublishingManager(PublishingManager publishingManager) { this.publishingManager = publishingManager; }

    protected Integer maxTolerableRetries;
    protected boolean masterPublishingNode;
    protected SiteService siteService;
    protected PublishingManager publishingManager;
}
