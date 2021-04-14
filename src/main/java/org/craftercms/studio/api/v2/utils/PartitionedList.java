/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class PartitionedList<T> extends AbstractList<List<T>> {

    private final List<T> list;
    private final int partitionSize;

    public PartitionedList(List<T> list, int partitionSize) {
        this.list = list;
        this.partitionSize = partitionSize;
    }

    @Override
    public List<T> get(int i) {
        int start = i * partitionSize;
        int end = Math.min(start + partitionSize, list.size());
        if (start > end) {
            throw new IndexOutOfBoundsException("Index " + i + " is out of the list range <0," + (size() - 1) + ">");
        }

        return new ArrayList<>(list.subList(start, end));
    }

    @Override
    public int size() {
        return (int) Math.ceil((double) list.size() / (double) partitionSize);
    }
}
