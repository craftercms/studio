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

package org.craftercms.studio.impl.v2.utils;

/**
 * A special ring buffer implementation that allows unlimited writes
 * keeping only a preset size of buffer, followed by reading of what
 * was written in the order it was written ignoring what was overwritten
 * when writing beyond the preset size. This means you always read the
 * least recently written item.
 *
 * This data structure cannot be written to once reading has begun.
 *
 * @param <T> type of objects to store
 */
public class RingBuffer<T> {
    protected T[] ringBuffer;
    protected int writeCursor = 0;
    protected int readCursor = 0;
    protected int size;

    public RingBuffer(int size) {
        this.size = size;
        ringBuffer = (T[]) new Object[size];
    }

    public void write(T element) {
        ringBuffer[(writeCursor % size)] = element;
        writeCursor++;

        if (writeCursor > size) {
            readCursor++;
        }
    }

    public T read() {
        T element = null;

        if (readCursor < writeCursor) {
            element = ringBuffer[(readCursor % size)];
            readCursor++;
        }

        return element;
    }
}