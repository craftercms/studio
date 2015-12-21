/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.craftercms.studio.impl.v1.repository.git;

import org.craftercms.studio.impl.v1.repository.disk.DiskContentRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class GitContentRepository extends DiskContentRepository {

    @Override
    public boolean writeContent(String path, InputStream content) {
        throw new RuntimeException("Not Implemented");
        //return super.writeContent(path, content);
    }

    @Override
    public void lockItem(String site, String path) {
        try {
            Repository repo = new FileRepositoryBuilder()
                    .setGitDir(new File(this.getRootPath()))
                    .build();
            TreeWalk tw = new TreeWalk(repo);
            ObjectId tree = repo.resolve("HEAD^{tree}");
            tw.addTree(tree); // tree ‘0’
            tw.setRecursive(true);
            tw.setFilter(PathFilter.create(path));
            FileTreeIterator iterator = tw.getTree(0, FileTreeIterator.class);
            iterator.getEntryFile()
            while(tw.next()) {
                ObjectId id = tw.getObjectId(0);
                tw.getPathString();
                repo.getFS();
            }
            tw.close();
            FS fs = FS.detect();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
