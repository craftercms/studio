/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.studio.impl.v2.publish;

import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;

import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.PublishState.LIVE_COMPLETED;
import static org.craftercms.studio.api.v2.dal.publish.PublishItem.PublishState.STAGING_COMPLETED;
import static org.craftercms.studio.api.v2.dal.publish.PublishPackage.PackageState.*;

/**
 * Wraps a {@link PublishPackage} and provide target-aware access to package properties
 */
public class PublishPackageTO {

    private final PublishPackage publishPackage;
    private static final TargetStrategy LIVE_STRATEGY = new LiveStrategy();
    private static final TargetStrategy STAGING_STRATEGY = new StagingStrategy();
    private final TargetStrategy targetStrategy;

    public PublishPackageTO(final PublishPackage publishPackage, final boolean isLiveTarget) {
        this.publishPackage = publishPackage;
        targetStrategy = isLiveTarget ? LIVE_STRATEGY : STAGING_STRATEGY;
    }

    public PublishPackage getPackage() {
        return publishPackage;
    }

    public void setSuccess() {
        targetStrategy.setSuccess(publishPackage);
    }

    public void setFailed() {
        targetStrategy.setFailed(publishPackage);
    }

    public long getId() {
        return getPackage().getId();
    }

    public Site getSite() {
        return getPackage().getSite();
    }

    @SuppressWarnings("unused")
    public long getPackageState() {
        return getPackage().getPackageState();
    }

    public void setPublishedCommitId(String commitId) {
        targetStrategy.setPublishedCommitId(publishPackage, commitId);
    }

    public PublishPackage.PackageType getPackageType() {
        return getPackage().getPackageType();
    }

    public long getCompletedOnMask() {
        return targetStrategy.getCompletedOnMask();
    }

    public long getCompletedOffMask() {
        return targetStrategy.getCompletedOffMask();
    }

    public long getItemSuccessState() {
        return targetStrategy.getItemSuccessState();
    }

    public void setCompletedWithErrors() {
        targetStrategy.setCompletedWithErrors(publishPackage);
    }

    /**
     * Stategy to access the target specific fields of the {@link PublishPackage}
     */
    private interface TargetStrategy {
        void setSuccess(final PublishPackage publishPackage);

        void setFailed(final PublishPackage publishPackage);

        void setPublishedCommitId(PublishPackage publishPackage, String commitId);

        void setCompletedWithErrors(PublishPackage publishPackage);

        long getCompletedOnMask();

        long getCompletedOffMask();

        long getItemSuccessState();
    }

    /**
     * Strategy for the live target
     */
    private static class LiveStrategy implements TargetStrategy {
        @Override
        public void setSuccess(final PublishPackage publishPackage) {
            publishPackage.setPackageState(publishPackage.getPackageState() | LIVE_SUCCESS.value);
        }

        @Override
        public void setFailed(final PublishPackage publishPackage) {
            publishPackage.setPackageState(publishPackage.getPackageState() | LIVE_FAILED.value);
        }

        @Override
        public void setCompletedWithErrors(PublishPackage publishPackage) {
            publishPackage.setPackageState(publishPackage.getPackageState() | LIVE_COMPLETED_WITH_ERRORS.value);
        }

        @Override
        public void setPublishedCommitId(final PublishPackage publishPackage, final String commitId) {
            publishPackage.setPublishedLiveCommitId(commitId);
        }

        @Override
        public long getCompletedOnMask() {
            return LIVE.value;
        }

        @Override
        public long getCompletedOffMask() {
            return NEW.value + MODIFIED.value + USER_LOCKED.value + IN_WORKFLOW.value + SCHEDULED.value;
        }

        @Override
        public long getItemSuccessState() {
            return LIVE_COMPLETED.value;
        }
    }

    /**
     * Strategy for the staging target
     */
    private static class StagingStrategy implements TargetStrategy {
        @Override
        public void setSuccess(final PublishPackage publishPackage) {
            publishPackage.setPackageState(publishPackage.getPackageState() | STAGING_SUCCESS.value);
        }

        @Override
        public void setFailed(final PublishPackage publishPackage) {
            publishPackage.setPackageState(publishPackage.getPackageState() | STAGING_FAILED.value);
        }

        @Override
        public void setCompletedWithErrors(PublishPackage publishPackage) {
            publishPackage.setPackageState(publishPackage.getPackageState() | STAGING_COMPLETED_WITH_ERRORS.value);
        }

        @Override
        public void setPublishedCommitId(final PublishPackage publishPackage, final String commitId) {
            publishPackage.setPublishedStagingCommitId(commitId);
        }

        @Override
        public long getCompletedOnMask() {
            return STAGED.value;
        }

        @Override
        public long getCompletedOffMask() {
            return USER_LOCKED.value + IN_WORKFLOW.value + SCHEDULED.value;
        }

        @Override
        public long getItemSuccessState() {
            return STAGING_COMPLETED.value;
        }
    }
}
