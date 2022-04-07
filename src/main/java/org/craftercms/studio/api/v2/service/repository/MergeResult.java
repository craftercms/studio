/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.repository;

import org.eclipse.jgit.api.PullResult;

import java.util.Collection;

/**
 * Holds the result of a merge from a remote repository
 *
 * @author joseross
 * @since 4.0.0
 */
public class MergeResult {

    /**
     * Indicates if the pull was successful
     */
    protected final boolean successful;

    /**
     * Total number of commits merged from the remote repository
     */
    protected final long commitsMerged;

    /**
     * The merge commit id
     */
    protected final String mergeCommitId;

    public MergeResult(boolean successful, long commitsMerged, String mergeCommitId) {
        this.successful = successful;
        this.commitsMerged = commitsMerged;
        this.mergeCommitId = mergeCommitId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public long getCommitsMerged() {
        return commitsMerged;
    }

    public String getMergeCommitId() {
        return mergeCommitId;
    }

    @Override
    public String toString() {
        return "PullResult{" +
                "successful=" + successful +
                ", commitsMerged=" + commitsMerged +
                ", mergeCommitId='" + mergeCommitId + '\'' +
                '}';
    }

    public static MergeResult failed() {
        return new MergeResult(false, 0, null);
    }

    public static MergeResult from(PullResult pullResult, Collection<String> mergedCommits) {
        org.eclipse.jgit.api.MergeResult mergeResult = pullResult.getMergeResult();
        long commitsMerged = 0;
        String mergeCommitId = null;
        // Don't check the values if the status is not MERGED, for some reason JGit keeps returning the previous values
        // even if the status is something like ALREADY_UP_TO_DATE
        if (mergeResult != null &&
            mergeResult.getMergeStatus() == org.eclipse.jgit.api.MergeResult.MergeStatus.MERGED) {
            commitsMerged = mergedCommits.size();
            mergeCommitId = mergeResult.getNewHead().name();
        }
        return new MergeResult(pullResult.isSuccessful(), commitsMerged, mergeCommitId);
    }

}
