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
import org.craftercms.studio.api.v2.dal.publish.PublishItem;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;

import static org.craftercms.studio.api.v2.dal.ItemState.*;
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

    public long getSuccessOnBits() {
        return targetStrategy.getSuccessOnBits();
    }

    public long getFailedOnBits() {
        return targetStrategy.getFailedOnBits();
    }

    public long getCompletedWithErrorsOnBits() {
        return targetStrategy.getCompletedWithErrorsOnBits();
    }

    public long getId() {
        return getPackage().getId();
    }

    public Site getSite() {
        return getPackage().getSite();
    }

    public void setPublishedCommitId(String commitId) {
        targetStrategy.setPublishedCommitId(publishPackage, commitId);
    }

    public PublishPackage.PackageType getPackageType() {
        return getPackage().getPackageType();
    }

    public long getSuccessOnMask() {
        return targetStrategy.getSuccessOnMask();
    }

    public long getSuccessOffMask() {
        return targetStrategy.getSuccessOffMask();
    }

    public long getFailureOffMask() {
        return USER_LOCKED.value + IN_WORKFLOW.value + SCHEDULED.value;
    }

    public long getItemSuccessState() {
        return targetStrategy.getItemSuccessState();
    }

    /**
     * Stategy to access the target specific fields of the {@link PublishPackage}
     */
    private interface TargetStrategy {

        long getSuccessOnBits();

        long getFailedOnBits();

        void setPublishedCommitId(PublishPackage publishPackage, String commitId);

        long getCompletedWithErrorsOnBits();

        long getSuccessOnMask();

        long getSuccessOffMask();

        long getItemSuccessState();
    }

    /**
     * Strategy for the live target
     */
    private static class LiveStrategy implements TargetStrategy {
        @Override
        public long getSuccessOnBits() {
            return LIVE_SUCCESS.value;
        }

        @Override
        public long getFailedOnBits() {
            return LIVE_FAILED.value;
        }

        @Override
        public long getCompletedWithErrorsOnBits() {
            return LIVE_COMPLETED_WITH_ERRORS.value;
        }

        @Override
        public void setPublishedCommitId(final PublishPackage publishPackage, final String commitId) {
            publishPackage.setPublishedLiveCommitId(commitId);
        }

        @Override
        public long getSuccessOnMask() {
            return LIVE.value;
        }

        @Override
        public long getSuccessOffMask() {
            return NEW.value + MODIFIED.value + USER_LOCKED.value + IN_WORKFLOW.value + SCHEDULED.value;
        }

        @Override
        public long getItemSuccessState() {
            return PublishItem.PublishState.LIVE_SUCCESS.value;
        }
    }

    /**
     * Strategy for the staging target
     */
    private static class StagingStrategy implements TargetStrategy {
        @Override
        public long getSuccessOnBits() {
            return STAGING_SUCCESS.value;
        }

        @Override
        public long getFailedOnBits() {
            return STAGING_FAILED.value;
        }

        @Override
        public long getCompletedWithErrorsOnBits() {
            return STAGING_COMPLETED_WITH_ERRORS.value;
        }

        @Override
        public void setPublishedCommitId(final PublishPackage publishPackage, final String commitId) {
            publishPackage.setPublishedStagingCommitId(commitId);
        }

        @Override
        public long getSuccessOnMask() {
            return STAGED.value;
        }

        @Override
        public long getSuccessOffMask() {
            return USER_LOCKED.value + IN_WORKFLOW.value + SCHEDULED.value;
        }

        @Override
        public long getItemSuccessState() {
            return PublishItem.PublishState.STAGING_SUCCESS.value;
        }
    }
}
