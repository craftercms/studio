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
package org.craftercms.cstudio.alfresco.util.impl;

import java.util.*;

public class CachingAwareList<T> extends CachingAwareObjectBase implements List<T> {

    private List<T> actualList;

    public CachingAwareList() {
        actualList = new ArrayList<T>();
    }

    public CachingAwareList(int initialSize) {
        actualList = new ArrayList<T>(initialSize);
    }

    public CachingAwareList(List<T> actualList) {
        if (actualList == null) {
            throw new IllegalArgumentException("The actual list argument should not be null");
        }

        this.actualList = actualList;
    }

    public List<T> getActualList() {
        return actualList;
    }

    public void setActualList(List<T> actualList) {
        this.actualList = actualList;
    }

    @Override
    public int size() {
        return actualList.size();
    }

    @Override
    public boolean isEmpty() {
        return actualList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return actualList.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return actualList.iterator();
    }

    @Override
    public Object[] toArray() {
        return actualList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return actualList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return actualList.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return actualList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return actualList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return actualList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return actualList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return actualList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return actualList.retainAll(c);
    }

    @Override
    public void clear() {
        actualList.clear();
    }

    @Override
    public T get(int index) {
        return actualList.get(index);
    }

    @Override
    public T set(int index, T element) {
        return actualList.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        actualList.add(index, element);
    }

    @Override
    public T remove(int index) {
        return actualList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return actualList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return actualList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return actualList.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return actualList.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return actualList.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        return actualList.equals(o);
    }

    @Override
    public int hashCode() {
        return actualList.hashCode();
    }

    @Override
    public String toString() {
        return actualList.toString();
    }
}
