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

import static commons.util.ExceptionUtil.throwArgEx;
import static java.lang.Integer.parseInt;

/**
 * @author Susanta Tewari
 * @version 1.2.4
 * @history Created on 11/22/13.
 * @since 1.2.4
 */
public final class Duration {

    private int millis;

    /**
     * @param durationString space separated tokens of format: int[C], valid values of C include h
     *                       (hours), m (minutes), s (seconds) [case ignored]
     * @throws IllegalArgumentException if {@code durationString} is invalid
     */
    public Duration(String durationString) {

        final String[] tokens = durationString.split("(\\s)+");

        throwArgEx(tokens.length == 0, "invalid duration string: " + durationString);

        int total = 0;

        for (String token : tokens) {
            total += parseDuration(token);
        }

        this.millis = total;
    }

    /**
     * @param token duration token; format: int[C], valid values of C include h (hours),
     *              m (minutes), s (seconds) [case ignored]
     * @return duration in milliseconds
     * @throws NumberFormatException if {@code token} is invalid
     * @throws IllegalArgumentException if {@code C} is not one of the above specified values
     * (ignoring case)
     */
    private long parseDuration(String token) {

        final int index = token.length() - 1;
        final String s  = Character.toString(token.toCharArray()[index]);

        if (s.equalsIgnoreCase("h")) return parseInt(token.substring(0, index)) * 60 * 60 * 1000;
        if (s.equalsIgnoreCase("m")) return parseInt(token.substring(0, index)) * 60 * 1000;
        if (s.equalsIgnoreCase("s")) return parseInt(token.substring(0, index)) * 1000;

        throw new IllegalArgumentException("invalid token: " + s);
    }

    public int toMillis() {
        return millis;
    }

    @Override
    public String toString() {

        int seconds, minutes, hours;

        seconds = millis / 1000;
        minutes = seconds / 60;
        hours   = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        hours   = hours % 60;

        StringBuilder builder = new StringBuilder();

        if (hours > 0) builder.append(hours + "h ");
        if (minutes > 0) builder.append(minutes + "m ");
        if (seconds > 0) builder.append(seconds + "s ");

        return builder.toString();
    }

    private Duration() {}
}
