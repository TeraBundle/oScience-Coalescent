/*
 * Copyright 2010-2014 Susanta Tewari. <statsusant@gmail.com>
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
 */

package commons.core;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.0.0
 * @version 1.0.0
 * @author Susanta Tewari
 * @history Created on Jun 26, 2012.
 */
@SuppressWarnings("JavaDoc")
public final class ValueTypes {

    /**
     * To prevent instantiation.
     */
    private ValueTypes() {}

    /**
     * Wrapper around {@code List<byte[]>}. The passed data reference is stored, no new allocation
     * is done.
     */
    public static class ByteArrayList {

        /** Field description */
        private final List<byte[]> data;

        /**
         *
         * @param data list of byte arrays
         */
        public ByteArrayList(final List<byte[]> data) {
            this.data = data;
        }

        /**
         * Method description
         *
         *
         * @return
         */
        public List<byte[]> getData() {
            return data;
        }
    }

    /**
     * Label-Character mapping.
     */
    public static class CharacterMap {

        private Map<String, Character> map = new HashMap<>();

        public void addCharacter(String label, Character character) {
            map.put(label, character);
        }

        public Map<String, Character> getMap() {
            return map;
        }
    }

    /**
     * Label-Color mapping.
     */
    public static class ColorMap {

        private Map<String, Color> map = new HashMap<>();

        public ColorMap() {}

        public ColorMap(Map<String, Color> defaultMap) {
            this.map = defaultMap;
        }

        public void addColor(String label, Color color) {
            map.put(label, color);
        }

        public Map<String, Color> getMap() {
            return map;
        }
    }

    /**
     * Wrapper around {@code List<String>}. The passed data reference is stored, no new allocation
     * is done.
     */
    public static class StringList {

        private final List<String> data;
        private String title = "";

        /**
         * @deprecated use {@link #StringList(java.util.List, String)} instead
         * @param list list of strings (empty strings are ignored)
         */
        @Deprecated
        public StringList(final List<String> list) {

            data = new ArrayList<>(list.size());

            for (final String val : list) {

                if (!val.isEmpty()) {
                    data.add(val);
                }
            }
        }

        public StringList(List<String> data, String title) {

            this.data  = new ArrayList<>(data);
            this.title = title;
        }

        /**
         * Method description
         *
         *
         * @return
         */
        public List<String> getData() {
            return data;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    /**
     * Wrapper around {@code List<List<String>>}. The passed data reference is stored, no new allocation
     * is done.
     */
    public static class StringList2 {

        private List<StringList> stringLists;

        private StringList2() {}

        /**
         * @param data list of string lists
         * @deprecated use {@link #ofLists(java.util.List)} instead
         */
        @Deprecated
        public StringList2(final List<List<String>> data) {

            this.stringLists = new ArrayList<>();

            int counter = 0;

            for (List<String> strings : data) {

                final StringList stringList = new StringList(strings, "List-" + counter++);

                stringLists.add(stringList);
            }
        }

        public static StringList2 ofLists(final List<StringList> lists) {

            StringList2 result = new StringList2();

            result.stringLists = new ArrayList<>(lists);

            return result;
        }

        /**
         * @return labels of the underlying lists
         */
        public List<String> getListLabels() {

            final ArrayList<String> result = new ArrayList<>();

            for (StringList stringList : stringLists) {
                result.add(stringList.getTitle());
            }

            return result;
        }

        public StringList getList(String label) {

            for (StringList stringList : stringLists) {

                final String title = stringList.getTitle();

                if (title.equals(label)) return stringList;
            }

            throw new IllegalArgumentException("not found: " + label);
        }

        /**
         * @return
         */
        public List<List<String>> getData() {

            List<List<String>> result = new ArrayList<>();

            for (StringList list : stringLists) {
                result.add(list.getData());
            }

            return result;
        }

        public List<StringList> toStringLists() {
            return new ArrayList<>(stringLists);
        }
    }
}
